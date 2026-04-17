package ap.be.backend.service;

import java.time.LocalDate;

import org.springframework.stereotype.Service;

import ap.be.backend.dto.ScanRequestDTO;
import ap.be.backend.dto.ScanResponseDTO;
import ap.be.backend.entity.Boek;
import ap.be.backend.entity.Gebruiker;
import ap.be.backend.entity.Uitlening;
import ap.be.backend.exception.BoekNotFoundException;
import ap.be.backend.exception.BoekNietBeschikbaarException;
import ap.be.backend.repository.BoekRepository;
import ap.be.backend.repository.UitleningRepository;
import jakarta.transaction.Transactional;

@Service
public class ScanService {
    private final GebruikerService gebruikerService;
    private final BoekService boekService;
    private final BoekRepository boekRepository;
    private final UitleningRepository uitleningRepository;

    public ScanService(GebruikerService gebruikerService, BoekService boekService,
            BoekRepository boekRepository,
            UitleningRepository uitleningRepository) {
        this.gebruikerService = gebruikerService;
        this.boekService = boekService;
        this.boekRepository = boekRepository;
        this.uitleningRepository = uitleningRepository;
    }

    @Transactional
    public ScanResponseDTO verwerkScan(ScanRequestDTO scanRequest) {
        Gebruiker gebruiker = gebruikerService.getGebruikerById(scanRequest.getGebruikerId());

        String isbn = scanRequest.getIsbn();
        Boek boek = boekService.getBoekByIsbn(isbn);
        if (boek == null) {
            throw new BoekNotFoundException("Boek met ISBN " + isbn + " niet gevonden.");
        }

        Uitlening uitlening = uitleningRepository
                .findFirstByGebruikerAndBoekAndIsIngeleverdFalseOrderByUitleenDatumAsc(gebruiker, boek)
                .orElse(null);

        if (uitlening != null) {
            uitlening.setIngeleverd(true);
            uitlening.setInleverDatum(LocalDate.now());
            boek.setAantalExemplaren(boek.getAantalExemplaren() + 1);
            uitleningRepository.save(uitlening);
            boekRepository.save(boek);
            return new ScanResponseDTO("RETOUR", uitlening, boek);
        }

        if (boek.getAantalExemplaren() <= 0) {
            throw new BoekNietBeschikbaarException("Boek met ISBN " + isbn + " is momenteel niet beschikbaar.");
        }

        boek.setAantalExemplaren(boek.getAantalExemplaren() - 1);
        boekRepository.save(boek);

        Uitlening nieuweUitlening = new Uitlening();
        nieuweUitlening.setGebruiker(gebruiker);
        nieuweUitlening.setBoek(boek);
        nieuweUitlening.setUitleenDatum(LocalDate.now());
        nieuweUitlening.setInleverDatum(LocalDate.now().plusMonths(1));
        nieuweUitlening.setIngeleverd(false);
        nieuweUitlening.setStatus("ACTIEF");

        uitleningRepository.save(nieuweUitlening);
        return new ScanResponseDTO("UITLENING", nieuweUitlening, boek);
    }
}
