package ap.be.backend.service;

import ap.be.backend.dto.OneRosterUser;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.util.Base64;
import java.util.List;
import java.util.Map;

/***
 * BESTAND: OneRosterService.java
 *
 * WAT IS ONEROSTER?
 * OneRoster is een externe API van de school die weet:
 *   - welke rol iemand heeft (student / teacher)
 *   - in welke klas iemand zit (bv. "2SOF")
 *
 * HOE WERKT DE AUTHENTICATIE (OAuth2 Client Credentials)?
 * OneRoster gebruikt GEEN gewone username/password per verzoek.
 * In plaats daarvan werkt het in twee stappen:
 *
 *   STAP 1 — Token ophalen:
 *     POST naar /token
 *     Met: client_id + client_secret (als Basic Auth header)
 *     En:  grant_type=client_credentials (in de body)
 *     Terug: een tijdelijk access_token (geldig voor bv. 1 uur)
 *
 *   STAP 2 — API aanroepen met dat token:
 *     GET /users?filter=...
 *     Authorization: Bearer <access_token>
 *     Terug: gebruikersdata als JSON
 *
 * Vergelijk het met een dagpas in een pretpark:
 *   Je betaalt aan de ingang (stap 1) en krijgt een polsbandje (token).
 *   Daarna kan je alle attracties in zonder opnieuw te betalen (stap 2).
 *
 * TWEE PUBLIEKE FUNCTIES:
 *   1. getUserByUsername()   -> Wie is deze gebruiker? (rol + sourcedId)
 *   2. getKlasBySourcedId()  -> In welke klas zit hij?
 ***/
@Service
public class OneRosterService {

    /***
     * De basis-URL van de OneRoster API.
     * Voorbeeld: https://aphogeschool.smartschool.be/ims/oneroster/v1p1
     * Gelezen uit application.properties: oneroster.base-url
     ***/
    @Value("${oneroster.base-url}")
    private String baseUrl;

    /***
     * De client_id: de "gebruikersnaam" waarmee we een token ophalen.
     * Gelezen uit application.properties: oneroster.username
     ***/
    @Value("${oneroster.username}")
    private String clientId;

    /***
     * De client_secret: het "wachtwoord" waarmee we een token ophalen.
     * Gelezen uit application.properties: oneroster.password
     ***/
    @Value("${oneroster.password}")
    private String clientSecret;

    /***
     * RestTemplate: de Spring-klasse om HTTP-verzoeken mee te versturen.
     * Vergelijk met fetch() in JavaScript.
     ***/
    private final RestTemplate restTemplate = new RestTemplate();

    /***
     * TOKEN CACHE: we slaan het opgehaalde token tijdelijk op.
     *
     * WAAROM CACHEN?
     * Een token is typisch 1 uur geldig. Als we bij elke login een nieuw token
     * zouden ophalen, doen we onnodige verzoeken naar de server.
     * Door het token te bewaren en hergebruiken is de login sneller.
     *
     * cachedAccessToken:  het token zelf (lange string)
     * tokenExpiresAt:     het tijdstip (milliseconden) waarop het token vervalt
     ***/
    private String cachedAccessToken = null;
    private long tokenExpiresAt = 0;

    /***
     * HULPMETHODE: geef een geldig access token terug.
     *
     * Controleer eerst of het gecachede token nog geldig is.
     * Als het bijna vervalt (binnen 30 seconden) of er nog geen is:
     * haal dan een nieuw token op via fetchNewAccessToken().
     ***/
    private String getAccessToken() {
        // System.currentTimeMillis() geeft de huidige tijd in milliseconden.
        // We vergelijken met tokenExpiresAt min 30 seconden veiligheidsmarge.
        boolean tokenIsStillValid = cachedAccessToken != null
                && System.currentTimeMillis() < tokenExpiresAt - 30_000;

        if (tokenIsStillValid) {
            System.out.println("[OneRoster] Gecached token hergebruikt.");
            return cachedAccessToken;
        }

        // Token is verlopen of er is nog geen: haal een nieuw op
        return fetchNewAccessToken();
    }

    /***
     * HULPMETHODE: haal een nieuw access token op bij de OneRoster token-endpoint.
     *
     * WAT VERSTUREN WE?
     *   POST {baseUrl}/token
     *   Header: Authorization: Basic base64(client_id:client_secret)
     *   Body:   grant_type=client_credentials  (form-encoded, niet JSON)
     *
     * WAT KRIJGEN WE TERUG?
     *   {
     *     "access_token": "eyJhbGci...",
     *     "token_type":   "Bearer",
     *     "expires_in":   3600        <- geldig voor 3600 seconden = 1 uur
     *   }
     ***/
    private String fetchNewAccessToken() {
        try {
            String tokenUrl = baseUrl + "/token";

            // Basic Auth header: base64(client_id:client_secret)
            String credentials = clientId + ":" + clientSecret;
            String encoded = Base64.getEncoder().encodeToString(credentials.getBytes());

            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Basic " + encoded);
            // De body van een token-verzoek is form-encoded (geen JSON)
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

            // Body: grant_type=client_credentials
            // MultiValueMap is de Spring-manier om form-data te sturen
            MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
            body.add("grant_type", "client_credentials");

            HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(body, headers);

            // Verstuur het POST-verzoek en lees het antwoord
            ResponseEntity<Map> response = restTemplate.exchange(
                    tokenUrl,
                    HttpMethod.POST,
                    request,
                    Map.class
            );

            // Haal het token en de geldigheidsduur uit het antwoord
            String accessToken = (String) response.getBody().get("access_token");
            Object expiresInObj = response.getBody().get("expires_in");

            // expires_in is het aantal seconden dat het token geldig is
            // We rekenen om naar milliseconden en tellen op bij de huidige tijd
            long expiresInSeconds = expiresInObj != null
                    ? Long.parseLong(expiresInObj.toString())
                    : 3600; // standaard 1 uur als de server dit niet meegeeft

            // Sla token en vervaltijd op in de cache
            cachedAccessToken = accessToken;
            tokenExpiresAt = System.currentTimeMillis() + (expiresInSeconds * 1000);

            System.out.println("[OneRoster] Nieuw access token opgehaald (geldig voor " + expiresInSeconds + " seconden).");
            return accessToken;

        } catch (Exception e) {
            System.err.println("[OneRoster] Fout bij ophalen access token: " + e.getMessage());
            return null;
        }
    }

    /***
     * HULPMETHODE: bouw een Authorization-header met het Bearer token.
     *
     * Bearer token = "draag dit token mee als bewijs van toegang".
     * De server controleert het token bij elk verzoek.
     *
     * Voorbeeld header: "Authorization: Bearer eyJhbGci..."
     ***/
    private HttpHeaders buildBearerHeaders(String accessToken) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + accessToken);
        return headers;
    }

    /***
     * FUNCTIE 1: Haal de gebruiker op uit OneRoster via zijn Smartschool-username.
     *
     * WAT VERSTUREN WE?
     *   GET {baseUrl}/users?filter=username='sof2.haitam.samire'
     *   Authorization: Bearer <access_token>
     *
     * WAT KRIJGEN WE TERUG?
     *   {
     *     "users": [
     *       { "sourcedId": "abc123", "role": "student", "username": "sof2.haitam.samire" }
     *     ]
     *   }
     *
     * @param username  de Smartschool-username, bv. "sof2.haitam.samire"
     * @return          OneRosterUser met sourcedId + role, of null bij mislukking
     ***/
    public OneRosterUser getUserByUsername(String username) {
        try {
            // Stap 1: zorg dat we een geldig access token hebben
            String accessToken = getAccessToken();
            if (accessToken == null) {
                System.err.println("[OneRoster] Geen access token beschikbaar, kan gebruiker niet ophalen.");
                return null;
            }

            // Stap 2: bouw de URL met het filter
            // OneRoster filter-syntax vereist enkele aanhalingstekens rond de waarde
            String url = baseUrl + "/users?filter=username='" + username + "'";

            HttpEntity<Void> request = new HttpEntity<>(buildBearerHeaders(accessToken));

            ResponseEntity<Map> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    request,
                    Map.class
            );

            @SuppressWarnings("unchecked")
            List<Map<String, Object>> users =
                    (List<Map<String, Object>>) response.getBody().get("users");

            if (users == null || users.isEmpty()) {
                System.out.println("[OneRoster] Geen gebruiker gevonden voor username: " + username);
                return null;
            }

            Map<String, Object> user = users.get(0);
            String sourcedId = (String) user.get("sourcedId");
            String role = (String) user.get("role");

            System.out.println("[OneRoster] Gebruiker gevonden: sourcedId=" + sourcedId + ", role=" + role);
            return new OneRosterUser(sourcedId, role);

        } catch (Exception e) {
            System.err.println("[OneRoster] Fout bij ophalen gebruiker '" + username + "': " + e.getMessage());
            return null;
        }
    }

    /***
     * FUNCTIE 2: Haal de klas op voor een gebruiker via zijn OneRoster sourcedId.
     *
     * WAT VERSTUREN WE?
     *   GET {baseUrl}/users/abc123/classes
     *   Authorization: Bearer <access_token>
     *
     * WAT KRIJGEN WE TERUG?
     *   {
     *     "classes": [
     *       { "title": "2SOF", ... }
     *     ]
     *   }
     *
     * @param sourcedId  de OneRoster-ID van de gebruiker, bv. "abc123"
     * @return           de klasnaam als String, bv. "2SOF", of "onbekend" bij mislukking
     ***/
    public String getKlasBySourcedId(String sourcedId) {
        try {
            String accessToken = getAccessToken();
            if (accessToken == null) {
                System.err.println("[OneRoster] Geen access token beschikbaar, kan klas niet ophalen.");
                return "onbekend";
            }

            String url = baseUrl + "/users/" + sourcedId + "/classes";

            HttpEntity<Void> request = new HttpEntity<>(buildBearerHeaders(accessToken));

            ResponseEntity<Map> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    request,
                    Map.class
            );

            @SuppressWarnings("unchecked")
            List<Map<String, Object>> classes =
                    (List<Map<String, Object>>) response.getBody().get("classes");

            if (classes == null || classes.isEmpty()) {
                System.out.println("[OneRoster] Geen klassen gevonden voor sourcedId: " + sourcedId);
                return "onbekend";
            }

            String klasnaam = (String) classes.get(0).get("title");
            System.out.println("[OneRoster] Klas gevonden: " + klasnaam);
            return klasnaam;

        } catch (Exception e) {
            System.err.println("[OneRoster] Fout bij ophalen klas voor sourcedId '" + sourcedId + "': " + e.getMessage());
            return "onbekend";
        }
    }
}
