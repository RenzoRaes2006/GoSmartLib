package ap.be.backend.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * Mock OneRoster client. Standaard actief; alleen vervangen door {@link OneRosterLiveClient}
 * wanneer property `gosmartlib.oneroster.mode=live` is gezet.
 */
@Slf4j
@Service
@ConditionalOnProperty(name = "gosmartlib.oneroster.mode", havingValue = "mock", matchIfMissing = true)
public class OneRosterMockClient implements OneRosterClient {

    @Override
    public boolean testVerbinding(String domein, String clientId, String clientSecret) {
        log.info("MOCK: Test verbinding met {}... Succes!", domein);
        try { Thread.sleep(1000); } catch (InterruptedException ignored) {}
        return domein != null && !domein.contains("fout");
    }

    @Override
    public Map<String, Object> haalLiveSchoolDataOp(String domein, String clientId, String clientSecret) {
        log.info("MOCK: Data ophalen voor {}...", domein);
        try { Thread.sleep(800); } catch (InterruptedException ignored) {}

        List<Map<String, Object>> mockKlassen = List.of(
                Map.of("id", 101, "naam", "1A - Mock"),
                Map.of("id", 102, "naam", "1B - Mock"),
                Map.of("id", 103, "naam", "2A - Mock")
        );

        List<Map<String, Object>> mockLeerlingen = List.of(
                Map.of("id", 5001, "voorNaam", "Amira", "achternaam", "El Idrissi (Mock)", "klas", "2A - Mock"),
                Map.of("id", 5002, "voorNaam", "Bram", "achternaam", "Peeters (Mock)", "klas", "1A - Mock"),
                Map.of("id", 5003, "voorNaam", "Celine", "achternaam", "Dion (Mock)", "klas", "1B - Mock")
        );

        List<Map<String, Object>> mockLeerkrachten = List.of(
                Map.of("id", 9001, "voorNaam", "Karel", "achternaam", "Janssens (Mock)", "klas", ""),
                Map.of("id", 9002, "voorNaam", "Linda", "achternaam", "Verhulst (Mock)", "klas", "")
        );

        return Map.of(
                "klassen", mockKlassen,
                "leerlingen", mockLeerlingen,
                "leerkrachten", mockLeerkrachten
        );
    }
}
