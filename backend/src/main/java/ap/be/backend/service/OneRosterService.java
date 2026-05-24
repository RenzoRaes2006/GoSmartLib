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

/**
 * Haalt via de OneRoster API de rol en klas op van een ingelogde gebruiker.
 *
 * OneRoster is een externe API die gegevens over leerlingen, leerkrachten en
 * klassen beheert. Deze service wordt aangeroepen vanuit OAuth2LoginSuccessHandler
 * tijdens het inlogproces om de rol ("student" of "teacher") en de klasnaam
 * van de gebruiker op te halen.
 *
 * Volgorde van aanroepen:
 *   1. getAccessToken / fetchNewAccessToken — haal een tijdelijk API-token op.
 *   2. getUserByUsername — zoek de gebruiker op aan de hand van zijn gebruikersnaam.
 *   3. getKlasBySourcedId — zoek de klas op aan de hand van de gebruikers-ID.
 */
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

    private String cachedAccessToken = null;
    private long tokenExpiresAt = 0;

    // In-memory cache van de volledige sourcedId -> naam map met TTL van 30 minuten.
    // We cachen één grote map (i.p.v. per sourcedId) omdat Smartschool's OneRoster
    // het filter=sourcedId='...' filter niet correct ondersteunt: we moeten dus alle
    // /students + /teachers één keer paginerend ophalen en lokaal mappen.
    // Niets wordt naar de database geschreven; deze map leeft enkel in het RAM en is
    // leeg na een herstart.
    private static final long NAAM_CACHE_TTL_MS = 30L * 60L * 1000L;
    private volatile Map<String, String> alleNamenCache = null;
    private volatile long alleNamenCacheTijd = 0L;

    /**
     * Geeft een geldig access token terug.
     *
     * Hergebruikt het gecachede token als dat nog minstens 30 seconden geldig
     * is. Anders wordt via fetchNewAccessToken een nieuw token opgehaald.
     *
     * @return een geldig access token, of null als het ophalen mislukt
     */
    private String getAccessToken() {
        boolean tokenIsStillValid = cachedAccessToken != null
                && System.currentTimeMillis() < tokenExpiresAt - 30_000;

        if (tokenIsStillValid) {
            return cachedAccessToken;
        }
        return fetchNewAccessToken();
    }

    /**
     * Vraagt een nieuw access token op bij de OneRoster token-endpoint.
     *
     * Gebruikt de client_credentials flow: client_id en client_secret worden
     * meegestuurd in de request-body. Het ontvangen token en zijn vervaltijd
     * worden opgeslagen in de cache zodat niet bij elke login een nieuw token
     * hoeft te worden opgevraagd.
     *
     * @return het nieuwe access token, of null als het ophalen mislukt
     */
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

    /**
     * Bouwt de HTTP-headers op die bij elk OneRoster API-verzoek worden meegestuurd.
     *
     * Stuurt het Bearer-token mee voor authenticatie en vraagt JSON terug als
     * antwoordformaat.
     *
     * @param accessToken het geldige access token
     * @return de ingevulde HttpHeaders
     */
    private HttpHeaders buildBearerHeaders(String accessToken) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + accessToken);
        headers.set("Accept", "application/json");
        return headers;
    }

    /**
     * Zoekt een OneRoster-gebruiker op via zijn Smartschool-gebruikersnaam.
     *
     * Geeft de sourcedId (unieke OneRoster-ID) en de rol van de gebruiker
     * terug. Deze gegevens worden gebruikt om daarna de klas op te halen
     * via getKlasBySourcedId.
     *
     * @param username de Smartschool-gebruikersnaam
     * @return een OneRosterUser met sourcedId en rol, of null als de gebruiker
     *         niet gevonden wordt of het ophalen mislukt
     */
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

    /**
     * Zoekt de klasnaam op van een leerling via zijn OneRoster sourcedId.
     *
     * De OneRoster API van Smartschool biedt geen directe endpoint om klassen
     * per leerling op te halen. Daarom worden drie opeenvolgende API-calls
     * gedaan:
     *
     * Stap 1: Haal de student op via /students/{sourcedId} om de school-sourcedId
     *         te vinden uit de "orgs"-lijst.
     *
     * Stap 2: Haal de inschrijvingen op via /schools/{schoolId}/enrollments,
     *         gefilterd op de sourcedId van de leerling, om de class-sourcedId
     *         te vinden.
     *
     * Stap 3: Haal de klasnaam op via /classes/{classSourcedId}.
     *
     * @param sourcedId de unieke OneRoster-ID van de gebruiker
     * @return de klasnaam (bv. "2SOF"), of "onbekend" als de klas niet
     *         gevonden wordt of het ophalen mislukt
     */
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

            // Stap 2: haal enrollments op voor die school, gefilterd op de leerling
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

            return (String) klas.get("title");

        } catch (Exception e) {
            System.err.println("[OneRoster] Fout bij ophalen klas voor '" + sourcedId + "': " + e.getMessage());
            return "onbekend";
        }
    }

    /**
     * Haalt de volledige naam (givenName + familyName) op van een gebruiker via
     * zijn OneRoster sourcedId. Resultaat wordt in-memory gecached zodat een
     * lijst met dezelfde leerling-koppelingen niet telkens een API-call doet.
     *
     * @param sourcedId de OneRoster sourcedId (in dit project: smartschoolId)
     * @return de volledige naam, of null als ophalen mislukt
     */
    public String getNaamBySourcedId(String sourcedId) {
        if (sourcedId == null || sourcedId.isBlank()) {
            return null;
        }
        return haalAlleNamenOp().get(sourcedId);
    }

    /**
     * Bouwt (of haalt uit cache) de sourcedId -> "Voornaam Achternaam" map voor
     * alle leerlingen en leerkrachten. Pagineert via limit/offset omdat
     * Smartschool's OneRoster geen werkend sourcedId-filter heeft. Cache TTL:
     * 30 minuten, gedeeld door alle aanroepers.
     */
    private synchronized Map<String, String> haalAlleNamenOp() {
        if (alleNamenCache != null
                && System.currentTimeMillis() - alleNamenCacheTijd < NAAM_CACHE_TTL_MS) {
            return alleNamenCache;
        }
        Map<String, String> resultaat = new java.util.HashMap<>();
        laadNamenViaEndpoint("/students", resultaat);
        laadNamenViaEndpoint("/teachers", resultaat);
        alleNamenCache = resultaat;
        alleNamenCacheTijd = System.currentTimeMillis();
        return resultaat;
    }

    private void laadNamenViaEndpoint(String pad, Map<String, String> doel) {
        try {
            String accessToken = getAccessToken();
            if (accessToken == null) return;

            int offset = 0;
            int limit = 100;
            while (true) {
                String url = baseUrl + pad + "?limit=" + limit + "&offset=" + offset;
                ResponseEntity<Map> response = restTemplate.exchange(
                        url, HttpMethod.GET, new HttpEntity<>(buildBearerHeaders(accessToken)), Map.class);

                @SuppressWarnings("unchecked")
                List<Map<String, Object>> users =
                        (List<Map<String, Object>>) response.getBody().get("users");
                if (users == null || users.isEmpty()) break;

                for (Map<String, Object> u : users) {
                    String id = (String) u.get("sourcedId");
                    String naam = bouwVolledigeNaam(
                            (String) u.get("givenName"), (String) u.get("familyName"));
                    if (id != null && naam != null) {
                        doel.put(id, naam);
                    }
                }
                if (users.size() < limit) break;
                offset += limit;
            }
        } catch (Exception e) {
            System.err.println("[OneRoster] Fout bij laden " + pad + ": " + e.getMessage());
        }
    }

    /**
     * Zoekt een gebruiker op via username, maar bevraagt de getypeerde
     * endpoints /students en /teachers in plaats van /users.
     *
     * Reden: /users?filter=username='...' geeft op Smartschool's OneRoster
     * soms een andere sourcedId terug dan /students of /teachers (bv. die van
     * een team-/serviceaccount), waardoor getNaamBySourcedId later een verkeerde
     * naam ophaalt. /students en /teachers leveren exact dezelfde sourcedId als
     * de bulk-import in OneRosterImportService, zodat OAuth-login en import op
     * hetzelfde User-record uitkomen en namen overal consistent zijn.
     *
     * @param username de Smartschool-gebruikersnaam
     * @return een OneRosterUser, of null als noch /students noch /teachers een
     *         match teruggeven
     */
    public OneRosterUser getUserByUsernameViaGetypeerdeEndpoint(String username) {
        OneRosterUser student = zoekViaGetypeerdEndpoint("/students", username, "student");
        if (student != null) return student;
        return zoekViaGetypeerdEndpoint("/teachers", username, "teacher");
    }

    private OneRosterUser zoekViaGetypeerdEndpoint(String pad, String username, String rol) {
        try {
            String accessToken = getAccessToken();
            if (accessToken == null) return null;

            String url = baseUrl + pad + "?filter=username='" + username + "'";
            ResponseEntity<Map> response = restTemplate.exchange(
                    url, HttpMethod.GET, new HttpEntity<>(buildBearerHeaders(accessToken)), Map.class);

            @SuppressWarnings("unchecked")
            List<Map<String, Object>> users =
                    (List<Map<String, Object>>) response.getBody().get("users");
            if (users == null || users.isEmpty()) return null;

            System.out.println("[DEBUG] " + pad + "?filter=username='" + username + "' gaf "
                    + users.size() + " resultaten. Eerste username = " + users.get(0).get("username")
                    + ", sourcedId = " + users.get(0).get("sourcedId"));

            // Filter werkt mogelijk niet aan OneRoster-zijde — filter in code voor de zekerheid.
            for (Map<String, Object> u : users) {
                if (username.equalsIgnoreCase(String.valueOf(u.get("username")))) {
                    return new OneRosterUser((String) u.get("sourcedId"), rol);
                }
            }
            return null;
        } catch (Exception e) {
            System.err.println("[OneRoster] Fout bij ophalen via " + pad + " voor '" + username + "': " + e.getMessage());
            return null;
        }
    }

    private String bouwVolledigeNaam(String givenName, String familyName) {
        boolean heeftGiven = givenName != null && !givenName.isBlank();
        boolean heeftFamily = familyName != null && !familyName.isBlank();
        if (heeftGiven && heeftFamily) return givenName + " " + familyName;
        if (heeftGiven) return givenName;
        if (heeftFamily) return familyName;
        return null;
    }
}
