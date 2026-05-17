package ap.be.backend.service;

import java.time.LocalDate;
import java.util.List;

import org.springframework.stereotype.Service;

import ap.be.backend.dto.LeenStatusResponse;
import ap.be.backend.dto.LeeslijstMapDTO;
import ap.be.backend.dto.UitleningDTO;
import ap.be.backend.dto.UitleningSummaryDTO;
import ap.be.backend.entity.Boek;
import ap.be.backend.entity.LeeslijstMap;
import ap.be.backend.entity.Rol;
import ap.be.backend.entity.Uitlening;
import ap.be.backend.entity.User;
import ap.be.backend.exception.BoekNotFoundException;
import ap.be.backend.exception.GebruikerNotFoundException;
import ap.be.backend.exception.LeenVoorwaardeException;
import ap.be.backend.exception.MapNotFoundException;
import ap.be.backend.exception.UitleningNotFoundException;
import ap.be.backend.mappers.LeeslijstMapMapper;
import ap.be.backend.mappers.UitleningMapper;
import ap.be.backend.repository.BoekRepository;
import ap.be.backend.repository.LeeslijstMapRepository;
import ap.be.backend.repository.UitleningRepository;
import ap.be.backend.repository.UserRepository;
import jakarta.transaction.Transactional;

@Service
public class GebruikerService {
    private final UserRepository userRepository;
    private final LeeslijstMapRepository leeslijstMapRepository;
    private final BoekRepository boekRepository;
    private final UitleningRepository uitleningRepository;
    private final InstellingService instellingService;

    public GebruikerService(UserRepository userRepository, LeeslijstMapRepository leeslijstMapRepository,
            BoekRepository boekRepository, UitleningRepository uitleningRepository,
            InstellingService instellingService) {
        this.userRepository = userRepository;
        this.leeslijstMapRepository = leeslijstMapRepository;
        this.boekRepository = boekRepository;
        this.uitleningRepository = uitleningRepository;
        this.instellingService = instellingService;
    }

    public List<LeeslijstMapDTO> getMappenVoorGebruiker(Long userId) {
        return leeslijstMapRepository.findByUser_Id(userId).stream()
                .map(LeeslijstMapMapper::toDTO)
                .toList();
    }

    public List<UitleningDTO> getActieveLeningenVanGebruikerDTO(Long userId) {
        return uitleningRepository.findByUser_IdAndIsIngeleverdFalse(userId).stream()
                .map(UitleningMapper::toDTO)
                .toList();
    }

    public List<UitleningDTO> getAlleAanvragenDTO() {
        return uitleningRepository.findByStatus("AANGEVRAAGD").stream()
                .map(UitleningMapper::toDTO)
                .toList();
    }

    public List<UitleningDTO> getLeengeschiedenisDTO(Long userId) {
        return uitleningRepository.findByUser_IdOrderByUitleenDatumDesc(userId).stream()
                .map(UitleningMapper::toDTO)
                .toList();
    }

    public List<UitleningSummaryDTO> getAlleActieveLeningenSysteemDTO() {
        return uitleningRepository.findByIsIngeleverdFalseOrderByUitleenDatumDesc().stream()
                .map(UitleningMapper::toSummaryDTO)
                .toList();
    }

    public List<UitleningSummaryDTO> getAlleVerlengingAanvragenDTO() {
        return uitleningRepository.findByStatus("VERLENGING_AANGEVRAAGD").stream()
                .map(UitleningMapper::toSummaryDTO)
                .toList();
    }

    public User getUserById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new GebruikerNotFoundException("User met ID " + id + " niet gevonden"));
    }

    public void voegBoekToeAanMap(Long userId, String mapNaam, Long boekId, String emoji) {
        User user = getUserById(userId);
        Boek boek = boekRepository.findById(boekId)
                .orElseThrow(() -> new BoekNotFoundException("Boek met ID " + boekId + " niet gevonden"));

        LeeslijstMap map = leeslijstMapRepository.findByUser_IdAndNaam(userId, mapNaam)
                .orElseGet(() -> {
                    LeeslijstMap nieuweMap = new LeeslijstMap(mapNaam, user);
                    nieuweMap.setEmoji(emoji);
                    return leeslijstMapRepository.save(nieuweMap);
                });

        map.voegBoekToe(boek);
        leeslijstMapRepository.save(map);
    }

    public void verwijderBoekUitMap(Long userId, String mapNaam, Long boekId) {
        LeeslijstMap map = leeslijstMapRepository.findByUser_IdAndNaam(userId, mapNaam)
                .orElseThrow(() -> new MapNotFoundException("Map '" + mapNaam + "' niet gevonden"));

        Boek boek = boekRepository.findById(boekId)
                .orElseThrow(() -> new BoekNotFoundException("Boek niet gevonden"));

        map.getBoeken().remove(boek);

        if (map.getBoeken().isEmpty() && !map.getNaam().equalsIgnoreCase("Favorieten")) {
            leeslijstMapRepository.delete(map);
        } else {
            leeslijstMapRepository.save(map);
        }
    }

    public LeenStatusResponse checkLeenStatus(Long userId, Long boekId) {
        Boek boek = boekRepository.findById(boekId).orElse(null);
        if (boek == null)
            return new LeenStatusResponse(false, "Boek niet gevonden");

        if (boek.getAantalExemplaren() <= 0) {
            return new LeenStatusResponse(false, "Boek is momenteel niet beschikbaar.");
        }

        if (uitleningRepository.existsByUser_IdAndBoek_IdAndIsIngeleverdFalse(userId, boekId)) {
            return new LeenStatusResponse(false, "Je hebt dit boek al geleend of in aanvraag.");
        }

        if (uitleningRepository.existsByUser_IdAndIsIngeleverdFalseAndInleverDatumBefore(userId, LocalDate.now())) {
            return new LeenStatusResponse(false, "Je hebt nog boeken die te laat zijn. Lever deze eerst in!");
        }

        int maxLimiet = instellingService.getLeenLimiet();

        if (maxLimiet > 0) {
            List<Uitlening> actieveLeningen = uitleningRepository.findByUser_IdAndIsIngeleverdFalse(userId);
            if (actieveLeningen.size() >= maxLimiet) {
                return new LeenStatusResponse(false,
                        "Je hebt je limiet van " + maxLimiet + " boeken bereikt (inclusief aanvragen).");
            }
        }

        return new LeenStatusResponse(true, "Beschikbaar om te reserveren");
    }

    public synchronized void bevestigUitlening(Long userId, Long boekId) {
        LeenStatusResponse status = checkLeenStatus(userId, boekId);
        if (!status.isMagLenen()) {
            throw new LeenVoorwaardeException(status.getReden());
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new GebruikerNotFoundException("User niet gevonden"));
        Boek boek = boekRepository.findById(boekId)
                .orElseThrow(() -> new BoekNotFoundException("Boek niet gevonden"));

        boek.setAantalExemplaren(boek.getAantalExemplaren() - 1);
        boekRepository.save(boek);

        LocalDate vandaag = LocalDate.now();
        int extraDagen = instellingService.getInlevertermijnDagen();
        LocalDate inleverDatum = vandaag.plusDays(extraDagen);

        Uitlening nieuweUitlening = new Uitlening(user, boek, vandaag, inleverDatum);
        uitleningRepository.save(nieuweUitlening);
    }

    public List<Uitlening> getAlleAanvragen() {
        return uitleningRepository.findByStatus("AANGEVRAAGD");
    }

    public void keurAanvraagGoed(Long uitleningId) {
        Uitlening uitlening = uitleningRepository.findById(uitleningId)
                .orElseThrow(() -> new UitleningNotFoundException("Uitlening niet gevonden"));

        uitlening.setStatus("GOEDGEKEURD");

        LocalDate vandaag = LocalDate.now();
        int extraDagen = instellingService.getInlevertermijnDagen();
        uitlening.setUitleenDatum(vandaag);
        uitlening.setInleverDatum(vandaag.plusDays(extraDagen));

        uitleningRepository.save(uitlening);
    }

    public void weigerAanvraag(Long uitleningId) {
        Uitlening uitlening = uitleningRepository.findById(uitleningId)
                .orElseThrow(() -> new UitleningNotFoundException("Uitlening niet gevonden"));

        uitlening.setStatus("GEWEIGERD");
        uitlening.setIngeleverd(true);

        Boek boek = uitlening.getBoek();
        boek.setAantalExemplaren(boek.getAantalExemplaren() + 1);
        boekRepository.save(boek);

        uitleningRepository.save(uitlening);
    }

    public List<Uitlening> getActieveLeningenVanGebruiker(Long userId) {
        return uitleningRepository.findByUser_IdAndIsIngeleverdFalse(userId);
    }

    public void verwijderLening(Long uitleningId) {
        Uitlening uitlening = uitleningRepository.findById(uitleningId)
                .orElseThrow(() -> new UitleningNotFoundException("Uitlening niet gevonden"));

        Boek boek = uitlening.getBoek();
        boek.setAantalExemplaren(boek.getAantalExemplaren() + 1);
        boekRepository.save(boek);

        uitleningRepository.delete(uitlening);
    }

    public List<Uitlening> getLeengeschiedenisVanGebruiker(Long userId) {
        return uitleningRepository.findByUser_IdOrderByUitleenDatumDesc(userId);
    }

    public void verlengUitlening(Long uitleningId) {
        Uitlening uitlening = uitleningRepository.findById(uitleningId)
                .orElseThrow(() -> new UitleningNotFoundException("Uitlening niet gevonden"));

        if (uitlening.isIngeleverd()) {
            throw new LeenVoorwaardeException("Dit boek is al ingeleverd.");
        }
        if (uitlening.isVerlengd()) {
            throw new LeenVoorwaardeException("Je hebt dit boek al een keer verlengd.");
        }
        if ("AANGEVRAAGD".equals(uitlening.getStatus())) {
            throw new LeenVoorwaardeException("Je kunt een boek dat nog in aanvraag is niet verlengen.");
        }
        if ("VERLENGING_AANGEVRAAGD".equals(uitlening.getStatus())) {
            throw new LeenVoorwaardeException("Verlenging is al aangevraagd.");
        }

        uitlening.setStatus("VERLENGING_AANGEVRAAGD");
        uitleningRepository.save(uitlening);
    }

    public List<Uitlening> getAlleVerlengingAanvragen() {
        return uitleningRepository.findByStatus("VERLENGING_AANGEVRAAGD");
    }

    public void keurVerlengingGoed(Long uitleningId) {
        Uitlening uitlening = uitleningRepository.findById(uitleningId)
                .orElseThrow(() -> new UitleningNotFoundException("Uitlening niet gevonden"));

        uitlening.setInleverDatum(uitlening.getInleverDatum().plusDays(14));
        uitlening.setVerlengd(true);
        uitlening.setStatus("GOEDGEKEURD");

        uitleningRepository.save(uitlening);
    }

    public void weigerVerlenging(Long uitleningId) {
        Uitlening uitlening = uitleningRepository.findById(uitleningId)
                .orElseThrow(() -> new UitleningNotFoundException("Uitlening niet gevonden"));

        uitlening.setVerlengd(true);
        uitlening.setStatus("GOEDGEKEURD");

        uitleningRepository.save(uitlening);
    }

    public User getLeerlingBySmartschoolId(String smartschoolId) {
        User user = userRepository.findBySmartschoolId(smartschoolId)
                .orElseThrow(() -> new GebruikerNotFoundException(
                        "Leerling met smartschoolId " + smartschoolId + " niet gevonden"));
        if (user.getRol() != Rol.LEERLING) {
            throw new GebruikerNotFoundException("Leerling met smartschoolId " + smartschoolId + " niet gevonden");
        }
        return user;
    }

    public List<Uitlening> getAlleActieveLeningenSysteem() {
        return uitleningRepository.findByIsIngeleverdFalseOrderByUitleenDatumDesc();
    }

    @Transactional
    public void bevestigBulkUitlening(Long userId, List<Long> boekIds) {
        if (boekIds == null || boekIds.isEmpty()) {
            throw new ap.be.backend.exception.InvalidImportDataException(
                    "Er zijn geen boeken geselecteerd om te lenen.");
        }

        User leraar = userRepository.findById(userId)
                .orElseThrow(() -> new ap.be.backend.exception.ResourceNotFoundException(
                        "Leerkracht met ID " + userId + " niet gevonden."));

        for (Long boekId : boekIds) {
            Boek boek = boekRepository.findById(boekId)
                    .orElseThrow(() -> new ap.be.backend.exception.ResourceNotFoundException(
                            "Boek met ID " + boekId + " niet gevonden."));

            if (boek.getAantalExemplaren() <= 0) {
                throw new ap.be.backend.exception.OnvoldoendeVoorraadException(
                        "Niet genoeg voorraad voor: " + boek.getTitel());
            }

            boek.setAantalExemplaren(boek.getAantalExemplaren() - 1);
            boekRepository.save(boek);

            int extraDagen = instellingService.getInlevertermijnDagen();
            LocalDate inleverDatum = LocalDate.now().plusDays(extraDagen);

            Uitlening uitlening = new Uitlening(leraar, boek, LocalDate.now(), inleverDatum);
            uitlening.setStatus("ACTIEF");
            uitlening.setUitleenDatum(LocalDate.now());
            uitlening.setInleverDatum(inleverDatum);

            uitleningRepository.save(uitlening);
        }
    }
}