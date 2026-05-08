package ap.be.backend.config;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Value;

import ap.be.backend.dto.OneRosterUser;
import ap.be.backend.entity.Gebruiker;
import ap.be.backend.entity.Rol;
import ap.be.backend.entity.User;
import ap.be.backend.repository.GebruikerRepository;
import ap.be.backend.repository.UserRepository;
import ap.be.backend.service.OneRosterService;

import java.io.IOException;

/***
 * BESTAND: OAuth2LoginSuccessHandler.java
 *
 * WAT DOET DIT BESTAND?
 * Dit is de code die Spring aanroept NADAT Smartschool succesvol heeft bevestigd
 * wie de gebruiker is. Dit is het hart van de login-logica.
 *
 * WANNEER WORDT DIT AANGEROEPEN?
 * Stap 1: Gebruiker klikt "Inloggen"
 * Stap 2: Browser gaat naar Smartschool
 * Stap 3: Gebruiker logt in op Smartschool
 * Stap 4: Smartschool stuurt gebruiker TERUG naar onze backend
 * Stap 5: Spring haalt automatisch de gebruikersdata op bij Smartschool
 * Stap 6: >>> HIER wordt onAuthenticationSuccess() aangeroepen <<<
 *
 * WAT GEBEURT ER IN DIT BESTAND?
 * 1. We lezen de gebruikersdata die Smartschool terugstuurde
 * 2. We kijken of deze gebruiker al in ONZE database staat (zo niet: aanmaken)
 * 3. We maken een JWT-token aan (een versleuteld "paspoort" voor de frontend)
 * 4. We sturen de gebruiker door naar de frontend met de token in de URL
 *
 ***/
@Component
public class OAuth2LoginSuccessHandler implements AuthenticationSuccessHandler {

    private final JwtService jwtService;
    private final UserRepository userRepository;
    private final GebruikerRepository gebruikerRepository;
    private final OneRosterService oneRosterService;

    @Value("${app.frontend.url}")
    private String frontendBaseUrl;

    public OAuth2LoginSuccessHandler(JwtService jwtService, UserRepository userRepository,
            GebruikerRepository gebruikerRepository, OneRosterService oneRosterService) {
        this.jwtService = jwtService;
        this.userRepository = userRepository;
        this.gebruikerRepository = gebruikerRepository;
        this.oneRosterService = oneRosterService;
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
            Authentication authentication) throws IOException, ServletException {

        /***
         * oAuth2User bevat ALLE data die Smartschool over de gebruiker terugstuurde.
         * Voorbeeldinhoud (je zag dit in de console-output):
         *   userID    = "E4LD6Wfpo22xSAjmOUHj2g==" (versleutelde unieke ID)
         *   name      = "Haitam"
         *   surname   = "Samire"
         *   username  = "sof2.haitam.samire"
         *   platform  = "naam van de school"
         *   role      = "student" (of "teacher", maar dit komt van Smartschool zelf)
         ***/
        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();

        /***
         * De "userID" van Smartschool is de unieke identifier per gebruiker.
         * Dit is een versleutelde string, bv: "E4LD6Wfpo22xSAjmOUHj2g=="
         * We slaan dit op als primaire sleutel in onze eigen database.
         ***/
        Object idAttr = oAuth2User.getAttribute("userID");
        String smartschoolId = idAttr == null ? null : idAttr.toString();

        /***
         * Haal de naam en username op uit de Smartschool attributen.
         *   - naam:     weergavenaam, bv. "Haitam"
         *   - username: de Smartschool-loginnaam, bv. "sof2.haitam.samire"
         *               Dit is de sleutel waarmee we de gebruiker opzoeken in OneRoster.
         ***/
        String naam = oAuth2User.getAttribute("name");
        String username = oAuth2User.getAttribute("username");

        /***
         * Stap A — OneRoster: haal de officiële rol en sourcedId op via de username.
         *
         * WHY OneRoster en niet Smartschool?
         * Smartschool geeft ook een "role" attribuut terug, maar dit is niet altijd
         * betrouwbaar. OneRoster is de officiële bron voor rollen en klassen.
         *
         * Als OneRoster niet bereikbaar is (null), vallen we terug op:
         *   - rol  = "onbekend"
         *   - klas = "onbekend"
         * zodat de login niet crasht maar de gebruiker toch binnenkomt.
         ***/
        String rol = "onbekend";
        String klas = "onbekend";

        OneRosterUser orUser = oneRosterService.getUserByUsername(username);

        if (orUser != null) {
            // OneRoster heeft de gebruiker gevonden: gebruik zijn officiële rol
            rol = orUser.getRole();

            /***
             * Stap B — OneRoster: haal de klas op via de sourcedId.
             * sourcedId is de unieke ID van de gebruiker binnen OneRoster.
             * Bv. "abc123" -> klas "2SOF"
             ***/
            klas = oneRosterService.getKlasBySourcedId(orUser.getSourcedId());
        }

        /***
         * Kijk of deze gebruiker al bestaat in ONZE "users" tabel.
         * - findBySmartschoolId() zoekt op de Smartschool ID
         * - Als niet gevonden (orElseGet): maak een nieuw User-record aan en sla op
         * Dit zorgt ervoor dat elke gebruiker die ooit inlogt, eenmalig wordt geregistreerd.
         ***/
        userRepository.findBySmartschoolId(smartschoolId)
                .orElseGet(() -> userRepository.save(new User(smartschoolId)));

        // Maak ook een Gebruiker-record aan zodat reviews/leningen aan deze auth-user gekoppeld kunnen worden.
        // Bewust GEEN voornaam/achternaam/email opslaan (GDPR — display-data komt uit de JWT).
        final String klasFinal = klas;
        final Rol rolEnum = mapRol(rol);
        gebruikerRepository.findBySmartschoolId(smartschoolId)
                .orElseGet(() -> gebruikerRepository.save(new Gebruiker(smartschoolId, klasFinal, rolEnum)));

        /***
         * Maak een JWT-token aan met alle info die we hebben verzameld:
         *   - smartschoolId: wie is de gebruiker (van Smartschool)
         *   - rol:           "student" of "teacher" (van OneRoster)
         *   - naam:          weergavenaam, bv. "Haitam" (van Smartschool)
         *   - klas:          klasnaam, bv. "2SOF" (van OneRoster)
         *
         * De frontend kan dit token decoderen op https://jwt.io om de inhoud te zien.
         ***/
        String jwtToken = jwtService.generateToken(smartschoolId, rol, naam, klas);

        /***
         * Stuur de gebruiker door naar de Angular frontend met de token in de URL.
         * Voorbeeld van de URL:
         *   https://frontend-sp09.cloud-ap.be/login-success?token=eyJhbGci...
         *
         * De Angular component "LoginSuccessComponent" vangt dit op,
         * haalt de token uit de URL, en slaat die op in localStorage.
         ***/
        String redirectUrl = frontendBaseUrl + "/login-success?token=" + jwtToken;

        response.sendRedirect(redirectUrl);
    }

    private Rol mapRol(String oneRosterRole) {
        if (oneRosterRole == null) return null;
        return switch (oneRosterRole.toLowerCase()) {
            case "student" -> Rol.LEERLING;
            case "teacher" -> Rol.LEERKRACHT;
            default -> null;
        };
    }
}
