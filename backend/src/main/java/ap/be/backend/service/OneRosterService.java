package ap.be.backend.service;

import ap.be.backend.dto.OneRosterUser;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Base64;
import java.util.List;
import java.util.Map;

/***
 * Haalt via de OneRoster API de rol en klas op van een ingelogde gebruiker.
 *
 * Authenticatie: Basic Auth — client_id en client_secret worden bij elk verzoek meegestuurd.
 *
 * Flow:
 *   1. getUserByUsername()   — zoek de gebruiker op, geeft rol + sourcedId terug
 *   2. getKlasBySourcedId()  — zoek de klas op via de sourcedId
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

    /***
     * Bouwt een Authorization header met Basic Auth: base64(client_id:client_secret).
     * Wordt meegestuurd bij elk verzoek naar de OneRoster API.
     ***/
    private HttpHeaders buildAuthHeaders() {
        String encoded = Base64.getEncoder()
                .encodeToString((clientId + ":" + clientSecret).getBytes());
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Basic " + encoded);
        return headers;
    }

    /***
     * Zoekt een gebruiker op in OneRoster via zijn Smartschool-username.
     * Geeft een OneRosterUser terug met zijn sourcedId en rol, of null bij mislukking.
     ***/
    public OneRosterUser getUserByUsername(String username) {
        try {
            String url = baseUrl + "/users?filter=username='" + username + "'";
            System.out.println("[OneRoster] Gebruiker ophalen via: " + url);

            ResponseEntity<Map> response = restTemplate.exchange(
                    url, HttpMethod.GET, new HttpEntity<>(buildAuthHeaders()), Map.class);

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
            String url = baseUrl + "/users/" + sourcedId + "/classes";
            System.out.println("[OneRoster] Klas ophalen via: " + url);

            ResponseEntity<Map> response = restTemplate.exchange(
                    url, HttpMethod.GET, new HttpEntity<>(buildAuthHeaders()), Map.class);

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
