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
import ap.be.backend.exception.BoekNotFoundException;
import ap.be.backend.exception.GebruikerNotFoundException;
import ap.be.backend.exception.LeenVoorwaardeException;
import ap.be.backend.exception.MapNotFoundException;
import ap.be.backend.exception.UitleningNotFoundException;
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
                .orElseThrow(() -> new GebruikerNotFoundException("Gebruiker met ID " + id + " niet gevonden"));
    }

    public void voegBoekToeAanMap(Long gebruikerId, String mapNaam, Long boekId, String emoji) {
        Gebruiker gebruiker = getGebruikerById(gebruikerId);
        Boek boek = boekRepository.findById(boekId)
                .orElseThrow(() -> new BoekNotFoundException("Boek met ID " + boekId + " niet gevonden"));

        LeeslijstMap map = leeslijstMapRepository.findByGebruiker_GebruikerIDAndNaam(gebruikerId, mapNaam)
                .orElseGet(() -> {
                    LeeslijstMap nieuweMap = new LeeslijstMap(mapNaam, gebruiker);
                    nieuweMap.setEmoji(emoji);
                    return leeslijstMapRepository.save(nieuweMap);
                });

        map.voegBoekToe(boek);
        leeslijstMapRepository.save(map);
    }

    public void verwijderBoekUitMap(Long gebruikerId, String mapNaam, Long boekId) {
        LeeslijstMap map = leeslijstMapRepository.findByGebruiker_GebruikerIDAndNaam(gebruikerId, mapNaam)
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

    public LeenStatusResponse checkLeenStatus(Long gebruikerId, Long boekId) {
        Boek boek = boekRepository.findById(boekId).orElse(null);
        if (boek == null)
            return new LeenStatusResponse(false, "Boek niet gevonden");

        // is boek op voorraad in de bib
        if (boek.getAantalExemplaren() <= 0) {
            return new LeenStatusResponse(false, "Boek is momenteel niet beschikbaar.");

        }

        // Heeft gebruiker dit zelfde boek al geleend of is al in aanvraag
        if (uitleningRepository.existsByGebruiker_GebruikerIDAndBoek_IdAndIsIngeleverdFalse(gebruikerId, boekId)) {
            return new LeenStatusResponse(false, "Je hebt dit boek al geleend of in aanvraag.");
        }

        // heeft de gebruiker boeken die te laat zijn
        if (uitleningRepository.existsByGebruiker_GebruikerIDAndIsIngeleverdFalseAndInleverDatumBefore(gebruikerId,
                LocalDate.now())) {
            return new LeenStatusResponse(false, "Je hebt nog boeken die te laat zijn. Lever deze eerst in!");
        }
        // De limiet check

        List<Uitlening> actieveLeningen = uitleningRepository
                .findByGebruiker_GebruikerIDAndIsIngeleverdFalse(gebruikerId);
        if (actieveLeningen.size() >= 3) {
            return new LeenStatusResponse(false, "Je hebt je limiet van 3 boeken bereikt (inclusief aanvragen).");
        }

        // boek lenen
        return new LeenStatusResponse(true, "Beschikbaar om te reserveren");
    }

    public synchronized void bevestigUitlening(Long gebruikerId, Long boekId) {
        LeenStatusResponse status = checkLeenStatus(gebruikerId, boekId);
        if (!status.isMagLenen()) {
            throw new LeenVoorwaardeException(status.getReden());
        }

        Gebruiker gebruiker = gebruikerRepository.findById(gebruikerId)
                .orElseThrow(() -> new GebruikerNotFoundException("Gebruiker niet gevonden"));
        Boek boek = boekRepository.findById(boekId)
                .orElseThrow(() -> new BoekNotFoundException("Boek niet gevonden"));

        // Decrease stock in the library
        boek.setAantalExemplaren(boek.getAantalExemplaren() - 1);
        boekRepository.save(boek);

        // Set loan period to 1 month from today
        LocalDate vandaag = LocalDate.now();
        LocalDate inleverDatum = vandaag.plusMonths(1);

        // Create and save the new loan request
        Uitlening nieuweUitlening = new Uitlening(gebruiker, boek, vandaag, inleverDatum);
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
        uitlening.setUitleenDatum(vandaag);
        uitlening.setInleverDatum(vandaag.plusMonths(1));

        uitleningRepository.save(uitlening);
    }

    public void weigerAanvraag(Long uitleningId) {
        Uitlening uitlening = uitleningRepository.findById(uitleningId)
                .orElseThrow(() -> new UitleningNotFoundException("Uitlening niet gevonden"));

        uitlening.setStatus("GEWEIGERD");
        uitlening.setIngeleverd(true); // Zodat het niet meer meetelt voor zijn limiet van 3

        Boek boek = uitlening.getBoek();
        boek.setAantalExemplaren(boek.getAantalExemplaren() + 1);
        boekRepository.save(boek);

        uitleningRepository.save(uitlening);
    }

    public List<Uitlening> getActieveLeningenVanGebruiker(Long gebruikerId) {
        return uitleningRepository.findByGebruiker_GebruikerIDAndIsIngeleverdFalse(gebruikerId);
    }

    public void verwijderLening(Long uitleningId) {
        // find the loan in the database
        Uitlening uitlening = uitleningRepository.findById(uitleningId)
                .orElseThrow(() -> new UitleningNotFoundException("Uitlening niet gevonden"));

        // restore the book stock before deleting the loan
        Boek boek = uitlening.getBoek();
        boek.setAantalExemplaren(boek.getAantalExemplaren() + 1);
        boekRepository.save(boek);

        // delete the loan completely from the database
        uitleningRepository.delete(uitlening);
    }

    public List<Uitlening> getLeengeschiedenisVanGebruiker(Long gebruikerId) {
        // Haalt alle leningen op, inclusief ingeleverde en afgewezen boeken
        return uitleningRepository.findByGebruiker_GebruikerIDOrderByUitleenDatumDesc(gebruikerId);
    }

    // Fetch all active loans across the entire system for the librarian
    public List<Uitlening> getAlleActieveLeningenSysteem() {
        return uitleningRepository.findByIsIngeleverdFalseOrderByUitleenDatumDesc();
    }
}