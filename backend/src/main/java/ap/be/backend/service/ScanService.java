package ap.be.backend.service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

import ap.be.backend.dto.ScanBatchItemResultDTO;
import ap.be.backend.dto.ScanBatchRequestDTO;
import ap.be.backend.dto.ScanBatchResponseDTO;
import ap.be.backend.entity.Boek;
import ap.be.backend.entity.Gebruiker;
import ap.be.backend.entity.Uitlening;
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
    public ScanBatchResponseDTO verwerkBatch(ScanBatchRequestDTO request) {
        Gebruiker gebruiker = gebruikerService.getGebruikerById(request.getGebruikerId());
        String actie = request.getActie();
        List<ScanBatchItemResultDTO> resultaten = new ArrayList<>();

        if (!"UITLENING".equals(actie) && !"RETOUR".equals(actie)) {
            throw new IllegalArgumentException("Onbekende actie: " + actie);
        }

        for (String isbn : request.getIsbns()) {
            try {
                Boek boek = boekService.getBoekByIsbn(isbn);
                if (boek == null) {
                    resultaten.add(new ScanBatchItemResultDTO(isbn, null, "FOUT", "Boek niet gevonden."));
                    continue;
                }

                Uitlening bestaande = uitleningRepository
                        .findFirstByGebruikerAndBoekAndIsIngeleverdFalseOrderByUitleenDatumAsc(gebruiker, boek)
                        .orElse(null);

                if ("UITLENING".equals(actie)) {
                    if (bestaande != null) {
                        resultaten.add(new ScanBatchItemResultDTO(isbn, boek.getTitel(), "FOUT",
                                "Je hebt dit boek al geleend."));
                        continue;
                    }
                    if (boek.getAantalExemplaren() <= 0) {
                        resultaten.add(new ScanBatchItemResultDTO(isbn, boek.getTitel(), "FOUT",
                                "Geen exemplaren beschikbaar."));
                        continue;
                    }
                    boek.setAantalExemplaren(boek.getAantalExemplaren() - 1);
                    boekRepository.save(boek);

                    Uitlening nieuw = new Uitlening();
                    nieuw.setGebruiker(gebruiker);
                    nieuw.setBoek(boek);
                    nieuw.setUitleenDatum(LocalDate.now());
                    nieuw.setInleverDatum(LocalDate.now().plusMonths(1));
                    nieuw.setIngeleverd(false);
                    nieuw.setStatus("ACTIEF");
                    uitleningRepository.save(nieuw);

                    resultaten.add(new ScanBatchItemResultDTO(isbn, boek.getTitel(), "OK", null));
                } else {
                    if (bestaande == null) {
                        resultaten.add(new ScanBatchItemResultDTO(isbn, boek.getTitel(), "FOUT",
                                "Geen actieve uitlening gevonden om terug te brengen."));
                        continue;
                    }
                    bestaande.setIngeleverd(true);
                    bestaande.setInleverDatum(LocalDate.now());
                    boek.setAantalExemplaren(boek.getAantalExemplaren() + 1);
                    uitleningRepository.save(bestaande);
                    boekRepository.save(boek);

                    resultaten.add(new ScanBatchItemResultDTO(isbn, boek.getTitel(), "OK", null));
                }
            } catch (Exception e) {
                resultaten.add(new ScanBatchItemResultDTO(isbn, null, "FOUT",
                        "Onverwachte fout: " + e.getMessage()));
            }
        }

        return new ScanBatchResponseDTO(actie, resultaten);
    }
}
