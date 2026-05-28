package ap.be.backend.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Echte OneRoster v1.1 client tegen Smartschool. Volgt dezelfde token- en
 * endpoint-conventies als de bestaande {@link OneRosterService}.
 *
 * Activeer met property `gosmartlib.oneroster.mode=live`.
 */
@Slf4j
@Service
@ConditionalOnProperty(name = "gosmartlib.oneroster.mode", havingValue = "live")
public class OneRosterLiveClient implements OneRosterClient {

    private static final int PAGE_SIZE = 1000;
    private static final int MAX_PAGES = 50; // safety cap

    private final RestClient restClient = RestClient.builder().build();

    /**
     * In-memory cache van de zware OneRoster-fetch per school. PII zit in deze
     * map maar wordt nooit gepersisteerd — bij restart of TTL-verloop wordt er
     * vers opgehaald via OneRoster.
     */
    private final ConcurrentHashMap<String, CacheEntry> dataCache = new ConcurrentHashMap<>();

    @Value("${gosmartlib.oneroster.cache-ttl-seconds:1800}")
    private long cacheTtlSeconds;

    private record CacheEntry(Map<String, Object> data, long expiresAt) {}

    private String haalAccessTokenOp(String domein, String clientId, String clientSecret) {
        String tokenUrl = baseUrl(domein) + "/ims/oneroster/token";

        MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add("grant_type", "client_credentials");
        form.add("client_id", clientId);
        form.add("client_secret", clientSecret);

        @SuppressWarnings("unchecked")
        Map<String, Object> body = restClient.post()
                .uri(tokenUrl)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(form)
                .retrieve()
                .body(Map.class);

        if (body == null || body.get("access_token") == null) {
            throw new IllegalStateException("Geen access_token in respons van " + tokenUrl);
        }
        return String.valueOf(body.get("access_token"));
    }

    @Override
    public boolean testVerbinding(String domein, String clientId, String clientSecret) {
        String token = haalAccessTokenOp(domein, clientId, clientSecret);
        HttpStatusCode status = restClient.get()
                .uri(baseUrl(domein) + "/ims/oneroster/v1p1/schools?limit=1")
                .header("Authorization", "Bearer " + token)
                .retrieve()
                .toBodilessEntity()
                .getStatusCode();
        log.info("OneRoster verbindingstest voor {} → {}", domein, status);
        if (!status.is2xxSuccessful()) {
            throw new IllegalStateException("OneRoster /schools gaf status " + status);
        }
        return true;
    }

    @Override
    public Map<String, Object> haalLiveSchoolDataOp(String domein, String clientId, String clientSecret) {
        String cacheKey = domein + "|" + clientId;
        long nu = System.currentTimeMillis();
        CacheEntry cached = dataCache.get(cacheKey);
        if (cached != null && cached.expiresAt() > nu) {
            return cached.data();
        }

        Map<String, Object> data = fetchVerseData(domein, clientId, clientSecret);
        dataCache.put(cacheKey, new CacheEntry(data, nu + cacheTtlSeconds * 1000L));
        return data;
    }

    private Map<String, Object> fetchVerseData(String domein, String clientId, String clientSecret) {
        String token = haalAccessTokenOp(domein, clientId, clientSecret);
        String base = baseUrl(domein) + "/ims/oneroster/v1p1";

        // 1. Klassen — bouw ook een lookup sourcedId → titel
        List<Map<String, Object>> klassenRaw = haalAllePaginas(base + "/classes", token, "classes");
        Map<String, String> klasTitelById = new HashMap<>();
        List<Map<String, Object>> klassen = new ArrayList<>();
        for (Map<String, Object> c : klassenRaw) {
            String id = String.valueOf(c.getOrDefault("sourcedId", ""));
            String titel = String.valueOf(c.getOrDefault("title", c.getOrDefault("classCode", "")));
            klasTitelById.put(id, titel);
            klassen.add(Map.of("id", id, "naam", titel));
        }

        // 2. Enrollments — bouw lookup userSourcedId → classSourcedId (eerste student-enrollment per user)
        Map<String, String> klasIdByUser = bouwKlasIdByUser(base, token);
        log.info("OneRoster enrollments: {} student-koppelingen gevonden", klasIdByUser.size());

        // 3. Leerlingen — verrijk met klas-titel via de lookups
        List<Map<String, Object>> leerlingen = haalAllePaginas(base + "/students", token, "users").stream()
                .map(u -> {
                    String sourcedId = String.valueOf(u.getOrDefault("sourcedId", ""));
                    String classId = klasIdByUser.get(sourcedId);
                    String klas = classId == null ? "" : klasTitelById.getOrDefault(classId, "");
                    return Map.<String, Object>of(
                            "id", sourcedId,
                            "voorNaam", u.getOrDefault("givenName", ""),
                            "achternaam", u.getOrDefault("familyName", ""),
                            "klas", klas,
                            "gebruikersnaam", u.getOrDefault("username", ""));
                })
                .toList();

        // 4. Leerkrachten — klas niet relevant
        List<Map<String, Object>> leerkrachten = haalAllePaginas(base + "/teachers", token, "users").stream()
                .map(u -> Map.<String, Object>of(
                        "id", u.getOrDefault("sourcedId", ""),
                        "voorNaam", u.getOrDefault("givenName", ""),
                        "achternaam", u.getOrDefault("familyName", ""),
                        "klas", "",
                        "gebruikersnaam", u.getOrDefault("username", "")))
                .toList();

        log.info("OneRoster live data voor {}: {} klassen, {} leerlingen, {} leerkrachten",
                domein, klassen.size(), leerlingen.size(), leerkrachten.size());
        return Map.of(
                "klassen", klassen,
                "leerlingen", leerlingen,
                "leerkrachten", leerkrachten
        );
    }

    private Map<String, String> bouwKlasIdByUser(String base, String token) {
        Map<String, String> result = new LinkedHashMap<>();
        for (Map<String, Object> e : haalAllePaginas(base + "/enrollments", token, "enrollments")) {
            String rol = String.valueOf(e.getOrDefault("role", ""));
            if (!"student".equalsIgnoreCase(rol)) continue;

            String userId = nestedSourcedId(e.get("user"));
            String classId = nestedSourcedId(e.get("class"));
            if (userId == null || classId == null) continue;

            result.putIfAbsent(userId, classId);
        }
        return result;
    }

    private String nestedSourcedId(Object ref) {
        if (ref instanceof Map<?, ?> m) {
            Object id = m.get("sourcedId");
            if (id != null) return id.toString();
        }
        return null;
    }

    private List<Map<String, Object>> haalAllePaginas(String basisUrl, String token, String veldNaam) {
        List<Map<String, Object>> alles = new ArrayList<>();
        int offset = 0;
        for (int page = 0; page < MAX_PAGES; page++) {
            String url = basisUrl + (basisUrl.contains("?") ? "&" : "?")
                    + "limit=" + PAGE_SIZE + "&offset=" + offset;
            List<Map<String, Object>> batch = haalLijstOp(url, token, veldNaam);
            if (batch.isEmpty()) break;
            alles.addAll(batch);
            if (batch.size() < PAGE_SIZE) break;
            offset += PAGE_SIZE;
        }
        return alles;
    }

    private List<Map<String, Object>> haalLijstOp(String url, String token, String veldNaam) {
        Map<String, Object> body = restClient.get()
                .uri(url)
                .header("Authorization", "Bearer " + token)
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .body(new ParameterizedTypeReference<Map<String, Object>>() {});
        if (body == null) return List.of();
        Object data = body.get(veldNaam);
        if (data instanceof List<?> lst) {
            List<Map<String, Object>> result = new ArrayList<>();
            for (Object item : lst) {
                if (item instanceof Map<?, ?> m) {
                    @SuppressWarnings("unchecked")
                    Map<String, Object> typed = (Map<String, Object>) m;
                    result.add(typed);
                }
            }
            return result;
        }
        return List.of();
    }

    private String baseUrl(String domein) {
        if (domein == null) return "";
        String d = domein.trim().replaceAll("/+$", "");
        if (d.startsWith("http://") || d.startsWith("https://")) return d;
        return "https://" + d;
    }
}
