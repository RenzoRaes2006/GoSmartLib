package ap.be.backend.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import ap.be.backend.entity.Boek;
import ap.be.backend.entity.Exemplaar;
import ap.be.backend.entity.ExemplaarStatus;
import ap.be.backend.exception.BoekNietBeschikbaarException;
import ap.be.backend.repository.ExemplaarRepository;

/**
 * Centrale plek voor alles wat met fysieke exemplaren gebeurt: uitlenen,
 * terugnemen, schade markeren en het aantal exemplaren van een boek bijregelen.
 *
 * Gedeeld door {@link ScanService}, {@link GebruikerService}, {@link BoekService}
 * en {@link BoekImportService} zodat de exemplaar-logica op één plek leeft en de
 * concurrency-garanties (pessimistic lock binnen de transactie van de aanroeper)
 * overal identiek zijn.
 */
@Service
public class ExemplaarService {

    private final ExemplaarRepository exemplaarRepository;

    public ExemplaarService(ExemplaarRepository exemplaarRepository) {
        this.exemplaarRepository = exemplaarRepository;
    }

    /**
     * Reserveert het eerste vrije exemplaar van het boek en zet het op UITGELEEND.
     * Draait onder een pessimistic write-lock; moet daarom binnen de transactie van
     * de aanroeper (REQUIRED) lopen zodat lock + statuswijziging atomair zijn.
     *
     * @throws BoekNietBeschikbaarException als er geen BESCHIKBAAR exemplaar is.
     */
    public Exemplaar leenUit(Boek boek) {
        Exemplaar vrij = exemplaarRepository
                .findBeschikbareVoorBoek(boek, ExemplaarStatus.BESCHIKBAAR)
                .stream()
                .findFirst()
                .orElseThrow(() -> new BoekNietBeschikbaarException(
                        "Geen exemplaren beschikbaar voor boek: " + boek.getTitel()));
        vrij.setStatus(ExemplaarStatus.UITGELEEND);
        return exemplaarRepository.save(vrij);
    }

    /** Zet een exemplaar terug op BESCHIKBAAR (gewone retour). */
    public void neemTerug(Exemplaar exemplaar) {
        if (exemplaar == null) {
            return;
        }
        exemplaar.setStatus(ExemplaarStatus.BESCHIKBAAR);
        exemplaarRepository.save(exemplaar);
    }

    /** Markeert een exemplaar als BESCHADIGD (retour met schade). */
    public void markeerBeschadigd(Exemplaar exemplaar) {
        if (exemplaar == null) {
            return;
        }
        exemplaar.setStatus(ExemplaarStatus.BESCHADIGD);
        exemplaarRepository.save(exemplaar);
    }

    /**
     * Zet een beschadigd exemplaar terug op BESCHIKBAAR (na herstel / wanneer de
     * beheerder het schade-rapport verwijdert). No-op als het exemplaar niet
     * beschadigd is — zo wordt een uitgeleend exemplaar nooit per ongeluk vrijgegeven.
     */
    public void herstelBeschadigd(Exemplaar exemplaar) {
        if (exemplaar == null || exemplaar.getStatus() != ExemplaarStatus.BESCHADIGD) {
            return;
        }
        exemplaar.setStatus(ExemplaarStatus.BESCHIKBAAR);
        exemplaarRepository.save(exemplaar);
    }

    public boolean isBeschikbaar(Boek boek) {
        return exemplaarRepository.countByBoekAndStatus(boek, ExemplaarStatus.BESCHIKBAAR) > 0;
    }

    /**
     * Maakt {@code aantal} nieuwe BESCHIKBAAR-exemplaren aan voor een (reeds
     * opgeslagen) boek, met opeenvolgende volgnummers vanaf het huidige maximum + 1.
     */
    @Transactional
    public List<Exemplaar> genereerExemplaren(Boek boek, int aantal) {
        List<Exemplaar> nieuwe = new ArrayList<>();
        if (aantal <= 0) {
            return nieuwe;
        }
        int start = exemplaarRepository.maxVolgnrVoorBoek(boek);
        for (int i = 1; i <= aantal; i++) {
            nieuwe.add(new Exemplaar(boek, start + i, ExemplaarStatus.BESCHIKBAAR));
        }
        return exemplaarRepository.saveAll(nieuwe);
    }

    /**
     * Stemt het aantal BESCHIKBAAR-exemplaren van een boek af op het gewenste aantal
     * dat de beheerder in de boek-beheer UI invult. Uitgeleende en beschadigde
     * exemplaren blijven onaangeroerd — dit gedrag is identiek aan vroeger, toen het
     * (verlaagde) {@code aantalExemplaren}-veld ook enkel de beschikbare voorraad was.
     *
     * <ul>
     *   <li>gewenst &gt; huidig beschikbaar → extra exemplaren bijmaken;</li>
     *   <li>gewenst &lt; huidig beschikbaar → overtollige BESCHIKBAAR-exemplaren
     *       verwijderen, hoogste volgnr eerst;</li>
     *   <li>gewenst gelijk → niets.</li>
     * </ul>
     */
    @Transactional
    public void stelBeschikbaarAantalIn(Boek boek, int gewenstBeschikbaar) {
        int doel = Math.max(0, gewenstBeschikbaar);
        long huidig = exemplaarRepository.countByBoekAndStatus(boek, ExemplaarStatus.BESCHIKBAAR);

        if (doel > huidig) {
            genereerExemplaren(boek, (int) (doel - huidig));
        } else if (doel < huidig) {
            int teVerwijderen = (int) (huidig - doel);
            List<Exemplaar> beschikbaar = exemplaarRepository
                    .findByBoekAndStatusOrderByVolgnrDesc(boek, ExemplaarStatus.BESCHIKBAAR);
            List<Exemplaar> sub = beschikbaar.subList(0, Math.min(teVerwijderen, beschikbaar.size()));
            exemplaarRepository.deleteAll(sub);
        }
    }
}
