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
 * BESTAND: OneRosterService.java
 *
 * WAT IS ONEROSTER?
 * OneRoster is een externe API van de school die weet:
 *   - welke rol iemand heeft (student / teacher)
 *   - in welke klas iemand zit (bv. "2SOF")
 *
 * WAAROM HEBBEN WE DIT NODIG?
 * Smartschool vertelt ons WIE de gebruiker is (naam, ID),
 * maar NIET betrouwbaar WAT zijn rol is of in welke klas hij zit.
 * OneRoster vult dat gat op.
 *
 * HOE WERKT ONEROSTER AUTHENTICATIE?
 * OneRoster gebruikt "Basic Auth": je stuurt een username en wachtwoord
 * mee bij elk verzoek, versleuteld als Base64.
 *
 * Voorbeeld:
 *   username = "school"
 *   password = "geheim"
 *   base64("school:geheim") = "c2Nob29sOmdlaGVpbQ=="
 *   Authorization header: "Basic c2Nob29sOmdlaGVpbQ=="
 *
 * TWEE FUNCTIES IN DEZE SERVICE:
 *   1. getUserByUsername()   -> Wie is deze gebruiker? (rol + sourcedId)
 *   2. getKlasBySourcedId()  -> In welke klas zit hij?
 ***/
@Service
public class OneRosterService {

    /***
     * De basis-URL van de OneRoster API van de school.
     * Voorbeeld: https://school.oneroster.be/ims/oneroster/v1p1
     * Gelezen uit application.properties: oneroster.base-url
     ***/
    @Value("${oneroster.base-url}")
    private String baseUrl;

    /***
     * De gebruikersnaam voor de OneRoster API (Basic Auth).
     * Gelezen uit application.properties: oneroster.username
     ***/
    @Value("${oneroster.username}")
    private String apiUsername;

    /***
     * Het wachtwoord voor de OneRoster API (Basic Auth).
     * Gelezen uit application.properties: oneroster.password
     ***/
    @Value("${oneroster.password}")
    private String apiPassword;

    /***
     * RestTemplate is de Spring-klasse waarmee we HTTP-verzoeken versturen.
     * Vergelijk het met fetch() in JavaScript, of axios in Angular.
     * Hier maken we één instance aan voor hergebruik.
     ***/
    private final RestTemplate restTemplate = new RestTemplate();

    /***
     * HULPMETHODE: bouw de Authorization header voor OneRoster.
     *
     * HOE WERKT BASIC AUTH?
     * 1. Combineer username en password met een dubbele punt: "username:password"
     * 2. Versleutel die string naar Base64
     * 3. Zet "Basic " ervoor
     *
     * Voorbeeld:
     *   "school:geheim" -> Base64 -> "c2Nob29sOmdlaGVpbQ=="
     *   Header: "Authorization: Basic c2Nob29sOmdlaGVpbQ=="
     *
     * Dit is de standaard manier om in te loggen bij REST API's die Basic Auth gebruiken.
     ***/
    private HttpHeaders buildAuthHeaders() {
        String credentials = apiUsername + ":" + apiPassword;
        String encoded = Base64.getEncoder().encodeToString(credentials.getBytes());

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Basic " + encoded);
        return headers;
    }

    /***
     * FUNCTIE 1: Haal de gebruiker op uit OneRoster via zijn Smartschool-username.
     *
     * WAT STUURT DIT NAAR ONEROSTER?
     *   GET {baseUrl}/users?filter=username='sof2.haitam.samire'
     *   Authorization: Basic <token>
     *
     * WAT GEEFT ONEROSTER TERUG? (vereenvoudigd)
     *   {
     *     "users": [
     *       { "sourcedId": "abc123", "role": "student", "username": "sof2.haitam.samire" }
     *     ]
     *   }
     *
     * WAT GEEFT DEZE METHODE TERUG?
     *   - Een OneRosterUser object met sourcedId en role
     *   - null als de gebruiker niet gevonden werd of de API niet bereikbaar is
     *
     * @param username  de Smartschool-username, bv. "sof2.haitam.samire"
     * @return          OneRosterUser met sourcedId + role, of null bij mislukking
     ***/
    public OneRosterUser getUserByUsername(String username) {
        try {
            // Bouw de URL op met het filter. De enkele aanhalingstekens rond de username
            // zijn verplicht door de OneRoster filter-syntax.
            String url = baseUrl + "/users?filter=username='" + username + "'";

            // Maak een HTTP-verzoek aan met de Authorization header
            HttpEntity<Void> request = new HttpEntity<>(buildAuthHeaders());

            // Verstuur het GET-verzoek en lees het antwoord als een Map
            // Map<String, Object> = een JSON-object als sleutel-waarde paren
            ResponseEntity<Map> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    request,
                    Map.class
            );

            // Het antwoord bevat een "users" lijst. We casten naar de juiste types.
            // Dit is veilig omdat we weten hoe de OneRoster JSON eruit ziet.
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> users =
                    (List<Map<String, Object>>) response.getBody().get("users");

            // Controleer of er effectief een gebruiker gevonden werd
            if (users == null || users.isEmpty()) {
                System.out.println("[OneRoster] Geen gebruiker gevonden voor username: " + username);
                return null;
            }

            // Pak de eerste (en enige) gebruiker uit de lijst
            Map<String, Object> user = users.get(0);
            String sourcedId = (String) user.get("sourcedId");
            String role = (String) user.get("role");

            System.out.println("[OneRoster] Gebruiker gevonden: sourcedId=" + sourcedId + ", role=" + role);
            return new OneRosterUser(sourcedId, role);

        } catch (Exception e) {
            // Als de API niet bereikbaar is of iets anders misgaat, loggen we de fout
            // en geven we null terug zodat de login niet volledig crasht.
            System.err.println("[OneRoster] Fout bij ophalen gebruiker '" + username + "': " + e.getMessage());
            return null;
        }
    }

    /***
     * FUNCTIE 2: Haal de klas op voor een gebruiker via zijn OneRoster sourcedId.
     *
     * WAT STUURT DIT NAAR ONEROSTER?
     *   GET {baseUrl}/users/abc123/classes
     *   Authorization: Basic <token>
     *
     * WAT GEEFT ONEROSTER TERUG? (vereenvoudigd)
     *   {
     *     "classes": [
     *       { "title": "2SOF", ... },
     *       { "title": "2SOF-gym", ... }   <- meerdere klassen mogelijk
     *     ]
     *   }
     *
     * WAT GEEFT DEZE METHODE TERUG?
     *   - De titel van de eerste klas, bv. "2SOF"
     *   - "onbekend" als de gebruiker geen klassen heeft of de API faalt
     *
     * @param sourcedId  de OneRoster-ID van de gebruiker, bv. "abc123"
     * @return           de klasnaam als String, bv. "2SOF"
     ***/
    public String getKlasBySourcedId(String sourcedId) {
        try {
            // Bouw de URL op: .../users/{sourcedId}/classes
            String url = baseUrl + "/users/" + sourcedId + "/classes";

            // Maak het HTTP-verzoek met de Authorization header
            HttpEntity<Void> request = new HttpEntity<>(buildAuthHeaders());

            // Verstuur en lees het antwoord
            ResponseEntity<Map> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    request,
                    Map.class
            );

            // Het antwoord bevat een "classes" lijst
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> classes =
                    (List<Map<String, Object>>) response.getBody().get("classes");

            // Controleer of er klassen gevonden werden
            if (classes == null || classes.isEmpty()) {
                System.out.println("[OneRoster] Geen klassen gevonden voor sourcedId: " + sourcedId);
                return "onbekend";
            }

            // Pak de titel van de eerste klas
            // (een leerling zit normaal in slechts één klas)
            String klasnaam = (String) classes.get(0).get("title");
            System.out.println("[OneRoster] Klas gevonden: " + klasnaam);
            return klasnaam;

        } catch (Exception e) {
            System.err.println("[OneRoster] Fout bij ophalen klas voor sourcedId '" + sourcedId + "': " + e.getMessage());
            return "onbekend";
        }
    }
}
