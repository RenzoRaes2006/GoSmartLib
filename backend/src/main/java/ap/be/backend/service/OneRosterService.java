package ap.be.backend.service;

import ap.be.backend.dto.OneRosterUser;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

/***
 * Haalt via de OneRoster API de rol en klas op van een ingelogde gebruiker.
 *
 * Flow:
 *   1. fetchNewAccessToken() — vraag een tijdelijk toegangstoken op
 *   2. getUserByUsername()   — zoek de gebruiker op, geeft rol + sourcedId terug
 *   3. getKlasBySourcedId()  — zoek de klas op via de sourcedId
 ***/
@Service
public class OneRosterService {

    @Value("${oneroster.base-url}")
    private String baseUrl;

    @Value("${oneroster.username}")
    private String clientId;

    @Value("${oneroster.password}")
    private String clientSecret;

    private final RestTemplate restTemplate = new RestTemplate();

    // Cache: bewaar het token zodat we het niet bij elke login opnieuw hoeven op te halen.
    private String cachedAccessToken = null;
    private long tokenExpiresAt = 0;

    /***
     * Geeft een geldig access token terug.
     * Hergebruikt het gecachede token als het nog geldig is, anders haalt het een nieuw op.
     ***/
    private String getAccessToken() {
        boolean tokenIsStillValid = cachedAccessToken != null
                && System.currentTimeMillis() < tokenExpiresAt - 30_000;

        if (tokenIsStillValid) {
            System.out.println("[OneRoster] Gecached token hergebruikt.");
            return cachedAccessToken;
        }

        return fetchNewAccessToken();
    }

    /***
     * Haalt een nieuw access token op bij het OneRoster /token endpoint.
     *
     * Stuurt: client_id + client_secret + grant_type allemaal in de body
     * Krijgt terug: een access_token dat tijdelijk toegang geeft tot de API
     ***/
    private String fetchNewAccessToken() {
        try {
            String tokenUrl = baseUrl + "/token";

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

            // Alle credentials in de body — sommige servers willen dit in plaats van een header
            MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
            body.add("grant_type", "client_credentials");
            body.add("client_id", clientId);
            body.add("client_secret", clientSecret);

            ResponseEntity<Map> response = restTemplate.exchange(
                    tokenUrl, HttpMethod.POST, new HttpEntity<>(body, headers), Map.class);

            String accessToken = (String) response.getBody().get("access_token");
            long expiresIn = Long.parseLong(response.getBody().getOrDefault("expires_in", 3600).toString());

            // Sla het token en de vervaltijd op in de cache
            cachedAccessToken = accessToken;
            tokenExpiresAt = System.currentTimeMillis() + (expiresIn * 1000);

            System.out.println("[OneRoster] Nieuw access token opgehaald (geldig voor " + expiresIn + "s).");
            return accessToken;

        } catch (Exception e) {
            System.err.println("[OneRoster] Fout bij ophalen access token: " + e.getMessage());
            return null;
        }
    }

    /***
     * Stuurt de Bearer token mee als Authorization header bij elk API-verzoek.
     ***/
    private HttpHeaders buildBearerHeaders(String accessToken) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + accessToken);
        return headers;
    }

    /***
     * Zoekt een gebruiker op in OneRoster via zijn Smartschool-username.
     * Geeft een OneRosterUser terug met zijn sourcedId en rol, of null bij mislukking.
     ***/
    public OneRosterUser getUserByUsername(String username) {
        try {
            String accessToken = getAccessToken();
            if (accessToken == null) return null;

            String url = baseUrl + "/users?filter=username='" + username + "'";
            ResponseEntity<Map> response = restTemplate.exchange(
                    url, HttpMethod.GET, new HttpEntity<>(buildBearerHeaders(accessToken)), Map.class);

            @SuppressWarnings("unchecked")
            List<Map<String, Object>> users =
                    (List<Map<String, Object>>) response.getBody().get("users");

            if (users == null || users.isEmpty()) {
                System.out.println("[OneRoster] Geen gebruiker gevonden voor: " + username);
                return null;
            }

            Map<String, Object> user = users.get(0);
            System.out.println("[OneRoster] Gebruiker gevonden: " + user.get("sourcedId") + ", rol: " + user.get("role"));
            return new OneRosterUser((String) user.get("sourcedId"), (String) user.get("role"));

        } catch (Exception e) {
            System.err.println("[OneRoster] Fout bij ophalen gebruiker '" + username + "': " + e.getMessage());
            return null;
        }
    }

    /***
     * Zoekt de klas op van een gebruiker via zijn sourcedId.
     * Geeft de klasnaam terug (bv. "2SOF"), of "onbekend" bij mislukking.
     ***/
    public String getKlasBySourcedId(String sourcedId) {
        try {
            String accessToken = getAccessToken();
            if (accessToken == null) return "onbekend";

            String url = baseUrl + "/users/" + sourcedId + "/classes";
            ResponseEntity<Map> response = restTemplate.exchange(
                    url, HttpMethod.GET, new HttpEntity<>(buildBearerHeaders(accessToken)), Map.class);

            @SuppressWarnings("unchecked")
            List<Map<String, Object>> classes =
                    (List<Map<String, Object>>) response.getBody().get("classes");

            if (classes == null || classes.isEmpty()) {
                System.out.println("[OneRoster] Geen klas gevonden voor sourcedId: " + sourcedId);
                return "onbekend";
            }

            String klasnaam = (String) classes.get(0).get("title");
            System.out.println("[OneRoster] Klas gevonden: " + klasnaam);
            return klasnaam;

        } catch (Exception e) {
            System.err.println("[OneRoster] Fout bij ophalen klas voor '" + sourcedId + "': " + e.getMessage());
            return "onbekend";
        }
    }
}
