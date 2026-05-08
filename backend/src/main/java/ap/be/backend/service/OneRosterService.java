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
 *   1. fetchNewAccessToken()  — vraag een tijdelijk token op via /token
 *   2. getUserByUsername()    — haal gebruiker op met dat token
 *   3. getKlasBySourcedId()   — haal klas op met dat token
 ***/
@Service
public class OneRosterService {

    @Value("${oneroster.base-url}")
    private String baseUrl;

    @Value("${oneroster.token-url}")
    private String tokenUrl;

    @Value("${oneroster.username}")
    private String clientId;

    @Value("${oneroster.password}")
    private String clientSecret;

    private final RestTemplate restTemplate = new RestTemplate();

    // Cache: bewaar het token zodat we het niet bij elke login opnieuw ophalen
    private String cachedAccessToken = null;
    private long tokenExpiresAt = 0;

    /***
     * Geeft een geldig token terug.
     * Hergebruikt het gecachede token als het nog geldig is, anders haalt het een nieuw op.
     ***/
    private String getAccessToken() {
        boolean tokenIsStillValid = cachedAccessToken != null
                && System.currentTimeMillis() < tokenExpiresAt - 30_000;

        if (tokenIsStillValid) {
            return cachedAccessToken;
        }
        return fetchNewAccessToken();
    }

    /***
     * Vraagt een nieuw access token op via POST {baseUrl}/token.
     * Stuurt client_id, client_secret en grant_type in de body (client_secret_post).
     * Slaat het token op in de cache samen met de vervaltijd.
     ***/
    private String fetchNewAccessToken() {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

            MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
            body.add("grant_type", "client_credentials");
            body.add("client_id", clientId);
            body.add("client_secret", clientSecret);

            ResponseEntity<Map> response = restTemplate.exchange(
                    tokenUrl, HttpMethod.POST, new HttpEntity<>(body, headers), Map.class);

            String accessToken = (String) response.getBody().get("access_token");
            long expiresIn = Long.parseLong(response.getBody().getOrDefault("expires_in", 3600).toString());

            cachedAccessToken = accessToken;
            tokenExpiresAt = System.currentTimeMillis() + (expiresIn * 1000);

            return accessToken;

        } catch (Exception e) {
            System.err.println("[OneRoster] Fout bij ophalen access token: " + e.getMessage());
            return null;
        }
    }

    /***
     * Stuurt het Bearer token mee als Authorization header bij elk API-verzoek.
     * Accept: application/json vertelt de server dat we JSON willen terug, geen HTML.
     ***/
    private HttpHeaders buildBearerHeaders(String accessToken) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + accessToken);
        headers.set("Accept", "application/json");
        return headers;
    }

    /***
     * Zoekt een gebruiker op via zijn Smartschool-username.
     * Geeft sourcedId en rol terug, of null bij mislukking.
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
                return null;
            }

            Map<String, Object> user = users.get(0);
            return new OneRosterUser((String) user.get("sourcedId"), (String) user.get("role"));

        } catch (Exception e) {
            System.err.println("[OneRoster] Fout bij ophalen gebruiker '" + username + "': " + e.getMessage());
            return null;
        }
    }

    /***
     * Zoekt de klas op via de sourcedId van de gebruiker.
     * Geeft de klasnaam terug (bv. "2SOF"), of "onbekend" bij mislukking.
     *
     * Flow (3 stappen want /users/{id}/classes bestaat niet in Smartschool):
     *   1. /students/{sourcedId}                         → haal school-sourcedId op uit orgs
     *   2. /schools/{schoolId}/enrollments?filter=...    → haal class-sourcedId op
     *   3. /classes/{classSourcedId}                     → haal klasnaam op
     ***/
    public String getKlasBySourcedId(String sourcedId) {
        try {
            String accessToken = getAccessToken();
            if (accessToken == null) return "onbekend";

            // Stap 1: haal de student op om de school-sourcedId te vinden
            String studentUrl = baseUrl + "/students/" + sourcedId;

            ResponseEntity<Map> studentResponse = restTemplate.exchange(
                    studentUrl, HttpMethod.GET, new HttpEntity<>(buildBearerHeaders(accessToken)), Map.class);

            @SuppressWarnings("unchecked")
            Map<String, Object> student = (Map<String, Object>) studentResponse.getBody().get("user");
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> orgs = (List<Map<String, Object>>) student.get("orgs");

            if (orgs == null || orgs.isEmpty()) {
                return "onbekend";
            }

            String schoolSourcedId = (String) orgs.get(0).get("sourcedId");

            // Stap 2: haal enrollments op voor die school, gefilterd op deze student
            String enrollmentsUrl = baseUrl + "/schools/" + schoolSourcedId
                    + "/enrollments?filter=user.sourcedId='" + sourcedId + "'";

            ResponseEntity<Map> enrollmentsResponse = restTemplate.exchange(
                    enrollmentsUrl, HttpMethod.GET, new HttpEntity<>(buildBearerHeaders(accessToken)), Map.class);

            @SuppressWarnings("unchecked")
            List<Map<String, Object>> enrollments =
                    (List<Map<String, Object>>) enrollmentsResponse.getBody().get("enrollments");

            if (enrollments == null || enrollments.isEmpty()) {
                return "onbekend";
            }

            @SuppressWarnings("unchecked")
            Map<String, Object> classRef = (Map<String, Object>) enrollments.get(0).get("class");
            String classSourcedId = (String) classRef.get("sourcedId");

            // Stap 3: haal de klasnaam op via de class-sourcedId
            String classUrl = baseUrl + "/classes/" + classSourcedId;

            ResponseEntity<Map> classResponse = restTemplate.exchange(
                    classUrl, HttpMethod.GET, new HttpEntity<>(buildBearerHeaders(accessToken)), Map.class);

            @SuppressWarnings("unchecked")
            Map<String, Object> klas = (Map<String, Object>) classResponse.getBody().get("class");
            String klasnaam = (String) klas.get("title");

            return klasnaam;

        } catch (Exception e) {
            System.err.println("[OneRoster] Fout bij ophalen klas voor '" + sourcedId + "': " + e.getMessage());
            return "onbekend";
        }
    }
}
