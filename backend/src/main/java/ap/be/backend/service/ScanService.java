package ap.be.backend.service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

import ap.be.backend.dto.RetourSchadeRequestDTO;
import ap.be.backend.dto.ScanBatchItemResultDTO;
import ap.be.backend.dto.ScanBatchRequestDTO;
import ap.be.backend.dto.ScanBatchResponseDTO;
import ap.be.backend.entity.Boek;
import ap.be.backend.entity.Exemplaar;
import ap.be.backend.entity.Uitlening;
import ap.be.backend.entity.User;
import ap.be.backend.repository.UitleningRepository;
import jakarta.transaction.Transactional;

@Service
public class ScanService {
    private final GebruikerService gebruikerService;
    private final BoekService boekService;
    private final UitleningRepository uitleningRepository;
    private final BoekSchadeService boekSchadeService;
    private final ExemplaarService exemplaarService;

    public ScanService(GebruikerService gebruikerService, BoekService boekService,
            UitleningRepository uitleningRepository,
            BoekSchadeService boekSchadeService,
            ExemplaarService exemplaarService) {
        this.gebruikerService = gebruikerService;
        this.boekService = boekService;
        this.uitleningRepository = uitleningRepository;
        this.boekSchadeService = boekSchadeService;
        this.exemplaarService = exemplaarService;
    }

    /**
     * Verwerkt een retour-batch waarin per boek aangegeven kan worden of het
     * beschadigd is en met welke opmerking. Voor elk beschadigd boek wordt een
     * BoekSchade-record aangemaakt; het boek zelf wordt gewoon ingeleverd.
     */
    @Transactional
    public ScanBatchResponseDTO verwerkRetourMetSchade(RetourSchadeRequestDTO request) {
        User user = gebruikerService.getUserById(request.getGebruikerId());
        List<ScanBatchItemResultDTO> resultaten = new ArrayList<>();

        for (RetourSchadeRequestDTO.Item item : request.getItems()) {
            String isbn = item.getIsbn();
            try {
                Boek boek = boekService.getBoekByIsbn(isbn);
                if (boek == null) {
                    resultaten.add(new ScanBatchItemResultDTO(isbn, null, "FOUT", "Boek niet gevonden."));
                    continue;
                }
                Uitlening bestaande = uitleningRepository
                        .findFirstByUserAndExemplaar_BoekAndIsIngeleverdFalseOrderByUitleenDatumAsc(user, boek)
                        .orElse(null);
                if (bestaande == null) {
                    resultaten.add(new ScanBatchItemResultDTO(isbn, boek.getTitel(), "FOUT",
                            "Geen actieve uitlening gevonden om terug te brengen."));
                    continue;
                }
                bestaande.setIngeleverd(true);
                bestaande.setInleverDatum(LocalDate.now());
                uitleningRepository.save(bestaande);

                Exemplaar exemplaar = bestaande.getExemplaar();
                if (item.isBeschadigd()) {
                    // Schade haalt het exemplaar uit circulatie (BESCHADIGD) i.p.v. het
                    // terug op BESCHIKBAAR te zetten.
                    exemplaarService.markeerBeschadigd(exemplaar);
                    boekSchadeService.registreer(bestaande, item.getOpmerking());
                } else {
                    exemplaarService.neemTerug(exemplaar);
                }

                resultaten.add(new ScanBatchItemResultDTO(isbn, boek.getTitel(), "OK", null));
            } catch (Exception e) {
                resultaten.add(new ScanBatchItemResultDTO(isbn, null, "FOUT",
                        "Onverwachte fout: " + e.getMessage()));
            }
        }
        return new ScanBatchResponseDTO("RETOUR", resultaten);
    }

    @Transactional
    public ScanBatchResponseDTO verwerkBatch(ScanBatchRequestDTO request) {
        User user = gebruikerService.getUserById(request.getGebruikerId());
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
                        .findFirstByUserAndExemplaar_BoekAndIsIngeleverdFalseOrderByUitleenDatumAsc(user, boek)
                        .orElse(null);

                if ("UITLENING".equals(actie)) {
                    if (bestaande != null) {
                        resultaten.add(new ScanBatchItemResultDTO(isbn, boek.getTitel(), "FOUT",
                                "Je hebt dit boek al geleend."));
                        continue;
                    }
                    if (!exemplaarService.isBeschikbaar(boek)) {
                        resultaten.add(new ScanBatchItemResultDTO(isbn, boek.getTitel(), "FOUT",
                                "Geen exemplaren beschikbaar."));
                        continue;
                    }
                    Exemplaar exemplaar = exemplaarService.leenUit(boek);

                    Uitlening nieuw = new Uitlening();
                    nieuw.setUser(user);
                    nieuw.setExemplaar(exemplaar);
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
                    uitleningRepository.save(bestaande);
                    exemplaarService.neemTerug(bestaande.getExemplaar());

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
