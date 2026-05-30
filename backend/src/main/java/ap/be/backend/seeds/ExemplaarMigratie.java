package ap.be.backend.seeds;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import ap.be.backend.entity.Boek;
import ap.be.backend.entity.Exemplaar;
import ap.be.backend.entity.ExemplaarStatus;
import ap.be.backend.entity.Uitlening;
import ap.be.backend.service.ExemplaarService;
import jakarta.persistence.EntityManager;

/**
 * Eenmalige data-migratie naar het Exemplaar-model voor databases die nog het
 * oude {@code boeken.aantal_exemplaren}-veld en de directe {@code uitleningen.boek_id}
 * FK gebruiken.
 *
 * <p>Per boek worden fysieke {@link Exemplaar}-rijen aangemaakt:
 * <ul>
 *   <li>{@code aantal_exemplaren} (de oude beschikbare voorraad) blijven BESCHIKBAAR;</li>
 *   <li>per open uitlening komt er één extra exemplaar dat op UITGELEEND staat en aan
 *       die uitlening gekoppeld wordt — zo blijft de beschikbare voorraad <em>exact</em>
 *       gelijk vóór en na de migratie;</li>
 *   <li>ingeleverde (historische) uitleningen worden deterministisch round-robin aan een
 *       exemplaar van datzelfde boek gehangen (gesorteerd op uitleendatum, dus reproduceerbaar).
 *       Hun status blijft BESCHIKBAAR — we kunnen het echte fysieke exemplaar niet kennen en
 *       willen de voorraad niet retroactief verlagen. Eventuele historische schade wordt dus
 *       niet als BESCHADIGD gemarkeerd; enkel nieuwe schade-retours doen dat voortaan.</li>
 * </ul>
 *
 * <p>Daarna worden de legacy-kolommen weggewerkt: {@code uitleningen.boek_id} (incl. FK) en
 * {@code boeken.aantal_exemplaren} worden gedropt en {@code uitleningen.exemplaar_id} op
 * NOT NULL gezet. Het droppen van {@code boek_id} is <strong>niet optioneel</strong>: die kolom
 * is NOT NULL en wordt door Hibernate niet meer gevuld, dus zonder drop zou elke nieuwe
 * uitlening falen.
 *
 * <p>Volledig idempotent: zodra {@code boeken.aantal_exemplaren} weg is, slaat de migratie
 * zichzelf over. Loopt enkel buiten het test-profiel (tests starten met een vers H2-schema).
 * Mag verwijderd worden zodra alle deployments de migratie minstens één keer hebben gedraaid.
 */
@Component
@Profile("!test")
@Order(60)
public class ExemplaarMigratie implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(ExemplaarMigratie.class);

    private final JdbcTemplate jdbc;
    private final EntityManager em;
    private final ExemplaarService exemplaarService;
    private final TransactionTemplate txTemplate;

    public ExemplaarMigratie(JdbcTemplate jdbc, EntityManager em, ExemplaarService exemplaarService,
            PlatformTransactionManager txManager) {
        this.jdbc = jdbc;
        this.em = em;
        this.exemplaarService = exemplaarService;
        this.txTemplate = new TransactionTemplate(txManager);
    }

    @Override
    public void run(String... args) {
        if (!legacyKolomBestaat()) {
            log.info("ExemplaarMigratie: kolom boeken.aantal_exemplaren bestaat niet (meer) — migratie overgeslagen.");
            return;
        }

        log.info("ExemplaarMigratie: start backfill van exemplaren...");
        txTemplate.executeWithoutResult(status -> backfill());

        Integer zonderExemplaar = jdbc.queryForObject(
                "SELECT COUNT(*) FROM uitleningen WHERE exemplaar_id IS NULL", Integer.class);
        if (zonderExemplaar != null && zonderExemplaar > 0) {
            throw new IllegalStateException("ExemplaarMigratie afgebroken: " + zonderExemplaar
                    + " uitlening(en) zonder exemplaar. Schema wordt niet aangepast.");
        }

        verwijderLegacyKolommen();
        log.info("ExemplaarMigratie: voltooid.");
    }

    private boolean legacyKolomBestaat() {
        Integer cnt = jdbc.queryForObject(
                "SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA = DATABASE() "
                        + "AND TABLE_NAME = 'boeken' AND COLUMN_NAME = 'aantal_exemplaren'",
                Integer.class);
        return cnt != null && cnt > 0;
    }

    @SuppressWarnings("unchecked")
    private void backfill() {
        // Oude beschikbare voorraad per boek.
        Map<Long, Integer> oudAantal = new LinkedHashMap<>();
        List<Object[]> boekRows = em.createNativeQuery(
                "SELECT id, aantal_exemplaren FROM boeken").getResultList();
        for (Object[] r : boekRows) {
            oudAantal.put(((Number) r[0]).longValue(), r[1] == null ? 0 : ((Number) r[1]).intValue());
        }

        // Uitleningen gegroepeerd per (legacy) boek_id, gesplitst in open/ingeleverd,
        // elk gesorteerd op uitleendatum voor een reproduceerbare toewijzing.
        Map<Long, List<Long>> openPerBoek = new LinkedHashMap<>();
        Map<Long, List<Long>> ingeleverdPerBoek = new LinkedHashMap<>();
        List<Object[]> uRows = em.createNativeQuery(
                "SELECT id, boek_id, is_ingeleverd, uitleen_datum FROM uitleningen "
                        + "ORDER BY boek_id, uitleen_datum, id").getResultList();
        for (Object[] r : uRows) {
            Long uId = ((Number) r[0]).longValue();
            Long boekId = ((Number) r[1]).longValue();
            Object ing = r[2];
            boolean ingeleverd = (ing instanceof Boolean b) ? b : (ing != null && ((Number) ing).intValue() != 0);
            (ingeleverd ? ingeleverdPerBoek : openPerBoek)
                    .computeIfAbsent(boekId, k -> new ArrayList<>()).add(uId);
        }

        int totaalExemplaren = 0;
        for (Map.Entry<Long, Integer> e : oudAantal.entrySet()) {
            Long boekId = e.getKey();
            int oud = e.getValue();
            List<Long> open = openPerBoek.getOrDefault(boekId, List.of());
            List<Long> ingeleverd = ingeleverdPerBoek.getOrDefault(boekId, List.of());

            int totaal = oud + open.size();
            // Boek zonder voorraad maar mét historiek: minstens één exemplaar om de
            // ingeleverde uitleningen aan te hangen (anders blijft exemplaar_id NULL).
            if (totaal == 0 && !ingeleverd.isEmpty()) {
                totaal = 1;
            }
            if (totaal == 0) {
                continue; // geen voorraad en geen historiek
            }

            Boek boek = em.getReference(Boek.class, boekId);
            List<Exemplaar> exs = exemplaarService.genereerExemplaren(boek, totaal);
            totaalExemplaren += totaal;

            // Open uitleningen: de exemplaren ná de beschikbare voorraad gaan op UITGELEEND.
            for (int i = 0; i < open.size(); i++) {
                Exemplaar ex = exs.get(oud + i);
                ex.setStatus(ExemplaarStatus.UITGELEEND);
                em.find(Uitlening.class, open.get(i)).setExemplaar(ex);
            }
            // Ingeleverde uitleningen: round-robin over alle exemplaren, status ongewijzigd.
            for (int k = 0; k < ingeleverd.size(); k++) {
                Exemplaar ex = exs.get(k % exs.size());
                em.find(Uitlening.class, ingeleverd.get(k)).setExemplaar(ex);
            }
        }
        log.info("ExemplaarMigratie: {} exemplaren aangemaakt over {} boeken.", totaalExemplaren, oudAantal.size());
    }

    private void verwijderLegacyKolommen() {
        // FK op uitleningen.boek_id eerst droppen, anders kan de kolom niet weg.
        List<String> fkNamen = jdbc.queryForList(
                "SELECT CONSTRAINT_NAME FROM INFORMATION_SCHEMA.KEY_COLUMN_USAGE "
                        + "WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'uitleningen' "
                        + "AND COLUMN_NAME = 'boek_id' AND REFERENCED_TABLE_NAME IS NOT NULL",
                String.class);
        for (String fk : fkNamen) {
            jdbc.execute("ALTER TABLE uitleningen DROP FOREIGN KEY " + fk);
        }
        jdbc.execute("ALTER TABLE uitleningen DROP COLUMN boek_id");
        jdbc.execute("ALTER TABLE uitleningen MODIFY exemplaar_id BIGINT NOT NULL");
        jdbc.execute("ALTER TABLE boeken DROP COLUMN aantal_exemplaren");
        log.info("ExemplaarMigratie: legacy-kolommen verwijderd en exemplaar_id op NOT NULL gezet.");
    }
}
