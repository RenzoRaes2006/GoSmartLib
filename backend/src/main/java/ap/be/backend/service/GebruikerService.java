package ap.be.backend.service;

import java.time.LocalDate;
import java.util.List;

import org.springframework.stereotype.Service;

import ap.be.backend.dto.GebruikerDTO;
import ap.be.backend.dto.LeenStatusResponse;
import ap.be.backend.entity.Boek;
import ap.be.backend.entity.Gebruiker;
import ap.be.backend.entity.LeeslijstMap;
import ap.be.backend.entity.Rol;
import ap.be.backend.entity.Uitlening;
import ap.be.backend.repository.BoekRepository;
import ap.be.backend.repository.GebruikerRepository;
import ap.be.backend.repository.LeeslijstMapRepository;
import ap.be.backend.repository.UitleningRepository;

@Service
public class GebruikerService {
    private final GebruikerRepository gebruikerRepository;
    private final LeeslijstMapRepository leeslijstMapRepository;
    private final BoekRepository boekRepository;
    private final UitleningRepository uitleningRepository;

    public GebruikerService(GebruikerRepository gebruikerRepository, LeeslijstMapRepository leeslijstMapRepository,
            BoekRepository boekRepository, UitleningRepository uitleningRepository) {
        this.gebruikerRepository = gebruikerRepository;
        this.leeslijstMapRepository = leeslijstMapRepository;
        this.boekRepository = boekRepository;
        this.uitleningRepository = uitleningRepository;
    }

    public GebruikerDTO mapToDTO(Gebruiker gebruiker) {
        return new GebruikerDTO(
                gebruiker.getGebruikerID(),
                gebruiker.getSchoolId(),
                gebruiker.getVoorNaam(),
                gebruiker.getAchternaam(),
                gebruiker.getEmail(),
                gebruiker.getRol().toString());
    }

    public Gebruiker maakGebruiker(GebruikerDTO nieuwGebruikerDTO) {

        Gebruiker nieuweGebruiker = new Gebruiker();
        nieuweGebruiker.setSchoolId(nieuwGebruikerDTO.schoolId());
        nieuweGebruiker.setVoorNaam(nieuwGebruikerDTO.voorNaam());
        nieuweGebruiker.setAchternaam(nieuwGebruikerDTO.achternaam());
        nieuweGebruiker.setEmail(nieuwGebruikerDTO.email());
        nieuweGebruiker.setRol(Rol.valueOf(nieuwGebruikerDTO.rol().toUpperCase()));

        Gebruiker opgeslagenGebruiker = gebruikerRepository.save(nieuweGebruiker);

        LeeslijstMap favorietenMap = new LeeslijstMap("Favorieten", opgeslagenGebruiker);
        leeslijstMapRepository.save(favorietenMap);

        return opgeslagenGebruiker;
    }

    public Gebruiker getGebruikerById(Long id) {
        return gebruikerRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Gebruiker met ID " + id + " niet gevonden"));
    }

    public void voegBoekToeAanMap(Long gebruikerId, String mapNaam, Long boekId) {
        Gebruiker gebruiker = getGebruikerById(gebruikerId);
        Boek boek = boekRepository.findById(boekId)
                .orElseThrow(() -> new IllegalArgumentException("Boek met ID " + boekId + " niet gevonden"));

        LeeslijstMap map = leeslijstMapRepository.findByGebruiker_GebruikerIDAndNaam(gebruikerId, mapNaam)
                .orElseGet(() -> {
                    LeeslijstMap nieuweMap = new LeeslijstMap(mapNaam, gebruiker);
                    return leeslijstMapRepository.save(nieuweMap);
                });

        map.voegBoekToe(boek);
        leeslijstMapRepository.save(map);
    }

    public void verwijderBoekUitMap(Long gebruikerId, String mapNaam, Long boekId) {
        LeeslijstMap map = leeslijstMapRepository.findByGebruiker_GebruikerIDAndNaam(gebruikerId, mapNaam)
                .orElseThrow(() -> new IllegalArgumentException("Map niet gevonden"));

        Boek boek = boekRepository.findById(boekId)
                .orElseThrow(() -> new IllegalArgumentException("Boek niet gevonden"));

        map.getBoeken().remove(boek);

        if (map.getBoeken().isEmpty() && !map.getNaam().equalsIgnoreCase("Favorieten")) {
            leeslijstMapRepository.delete(map);
        } else {
            leeslijstMapRepository.save(map);
        }
    }

    public LeenStatusResponse checkLeenStatus(Long gebruikerId, Long boekId) {
        Boek boek = boekRepository.findById(boekId).orElse(null);
        if (boek == null)
            return new LeenStatusResponse(false, "Boek niet gevonden");

        // beschikbaar?
        if (boek.getAantalExemplaren() <= 0) {
            return new LeenStatusResponse(false, "Boek is momenteel niet beschikbaar.");
        }
        if (uitleningRepository.existsByGebruiker_GebruikerIDAndStatus(gebruikerId, "AANGEVRAAGD")) {
            return new LeenStatusResponse(false,
                    "Je hebt al een reservering geplaatst. Wacht tot de bibbeheerder deze heeft verwerkt!");
        }
        // boek al geleend door gebruiker?
        if (uitleningRepository.existsByGebruiker_GebruikerIDAndBoek_IdAndIsIngeleverdFalse(gebruikerId, boekId)) {
            return new LeenStatusResponse(false, "Je hebt dit boek al geleend.");
        }

        // heeft gebruiker boeken die te laat zijn?
        if (uitleningRepository.existsByGebruiker_GebruikerIDAndIsIngeleverdFalseAndInleverDatumBefore(gebruikerId,
                LocalDate.now())) {
            return new LeenStatusResponse(false, "Je hebt nog boeken die te laat zijn. Lever deze eerst in!");
        }

        // Max 3 boeken?
        List<Uitlening> actieveLeningen = uitleningRepository
                .findByGebruiker_GebruikerIDAndIsIngeleverdFalse(gebruikerId);
        if (actieveLeningen.size() >= 3) {
            return new LeenStatusResponse(false, "Je hebt het maximum van 3 geleende boeken bereikt.");
        }

        // Als we alle checks overleven, mag de gebruiker lenen!
        return new LeenStatusResponse(true, "Beschikbaar om te lenen");
    }

    public void bevestigUitlening(Long gebruikerId, Long boekId) {
        LeenStatusResponse status = checkLeenStatus(gebruikerId, boekId);
        if (!status.isMagLenen()) {
            throw new IllegalArgumentException(status.getReden());
        }

        Gebruiker gebruiker = gebruikerRepository.findById(gebruikerId).orElseThrow();
        Boek boek = boekRepository.findById(boekId).orElseThrow();

        // Voorraad verminderen
        boek.setAantalExemplaren(boek.getAantalExemplaren() - 1);
        boekRepository.save(boek);

        // Uitleen termijn 1 maand vanaf vandaag
        LocalDate vandaag = LocalDate.now();
        LocalDate inleverDatum = vandaag.plusMonths(1);

        // Aanmaken en opslaan
        Uitlening nieuweUitlening = new Uitlening(gebruiker, boek, vandaag, inleverDatum);
        uitleningRepository.save(nieuweUitlening);
    }

    public List<Uitlening> getAlleAanvragen() {
        return uitleningRepository.findByStatus("AANGEVRAAGD");
    }

    public void keurAanvraagGoed(Long uitleningId) {
        Uitlening uitlening = uitleningRepository.findById(uitleningId)
                .orElseThrow(() -> new IllegalArgumentException("Uitlening niet gevonden"));

        uitlening.setStatus("GOEDGEKEURD");

        LocalDate vandaag = LocalDate.now();
        uitlening.setUitleenDatum(vandaag);
        uitlening.setInleverDatum(vandaag.plusMonths(1));

        uitleningRepository.save(uitlening);
    }

    public void weigerAanvraag(Long uitleningId) {
        Uitlening uitlening = uitleningRepository.findById(uitleningId)
                .orElseThrow(() -> new IllegalArgumentException("Uitlening niet gevonden"));

        uitlening.setStatus("GEWEIGERD");
        uitlening.setIngeleverd(true); // Zodat het niet meer meetelt voor zijn limiet van 3

        Boek boek = uitlening.getBoek();
        boek.setAantalExemplaren(boek.getAantalExemplaren() + 1);
        boekRepository.save(boek);

        uitleningRepository.save(uitlening);
    }
}
