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
import ap.be.backend.entity.Rol;
import ap.be.backend.entity.User;
import ap.be.backend.repository.UserRepository;
import ap.be.backend.service.OneRosterService;

import java.io.IOException;

/**
 * Wordt aangeroepen door Spring Security zodra een gebruiker succesvol
 * inlogt via Smartschool (OAuth2).
 *
 * Verantwoordelijkheden:
 *   1. Haal de gebruikersgegevens op uit het Smartschool OAuth2-antwoord.
 *   2. Vraag de rol en klas op via OneRoster.
 *   3. Sla de gebruiker op in de database (aanmaken of bijwerken).
 *   4. Genereer een JWT-token en stuur de gebruiker door naar de frontend.
 */
@Component
public class OAuth2LoginSuccessHandler implements AuthenticationSuccessHandler {

    private final JwtService jwtService;
    private final UserRepository userRepository;
    private final OneRosterService oneRosterService;

    @Value("${app.frontend.url}")
    private String frontendBaseUrl;

    public OAuth2LoginSuccessHandler(JwtService jwtService, UserRepository userRepository,
            OneRosterService oneRosterService) {
        this.jwtService = jwtService;
        this.userRepository = userRepository;
        this.oneRosterService = oneRosterService;
    }

    /**
     * Verwerkt een succesvolle Smartschool-login.
     *
     * Stap 1: Lees de smartschoolId, naam en gebruikersnaam uit het
     *         OAuth2-antwoord van Smartschool.
     *
     * Stap 2: Vraag via OneRoster de rol ("student" of "teacher") en de
     *         klasnaam op. Als OneRoster niets teruggeeft, worden "onbekend"
     *         als standaardwaarden gebruikt.
     *
     * Stap 3: Zoek de gebruiker op in de database. Bestaat hij al, dan worden
     *         klas en rol bijgewerkt. Bestaat hij nog niet, dan wordt een nieuw
     *         record aangemaakt. Persoonsgegevens zoals naam en e-mail worden
     *         niet opgeslagen in de database.
     *
     * Stap 4: Maak een JWT-token aan met de gegevens van de gebruiker en
     *         stuur de browser door naar de frontend. De frontend slaat de
     *         token op en gebruikt hem bij volgende API-calls.
     *
     * @param request        het binnenkomende HTTP-verzoek
     * @param response       het uitgaande HTTP-antwoord
     * @param authentication het door Spring gevulde authenticatie-object
     */
    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
            Authentication authentication) throws IOException, ServletException {

        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();

        Object idAttr = oAuth2User.getAttribute("userID");
        String smartschoolId = idAttr == null ? null : idAttr.toString();

        String naam = oAuth2User.getAttribute("name");
        String username = oAuth2User.getAttribute("username");

        String rol = "onbekend";
        String klas = "onbekend";

        OneRosterUser orUser = oneRosterService.getUserByUsername(username);

        if (orUser != null) {
            rol = orUser.getRole();
            klas = oneRosterService.getKlasBySourcedId(orUser.getSourcedId());
        }

        final String klasFinal = klas;
        final Rol rolEnum = mapRol(rol);

        // Bestaande user bijwerken of nieuw aanmaken. Geen persoonsdata opslaan.
        userRepository.findBySmartschoolId(smartschoolId)
                .map(existing -> {
                    existing.setKlas(klasFinal);
                    existing.setRol(rolEnum);
                    return userRepository.save(existing);
                })
                .orElseGet(() -> userRepository.save(new User(smartschoolId, klasFinal, rolEnum)));

        String jwtToken = jwtService.generateToken(smartschoolId, rol, naam, klas);

        response.sendRedirect(frontendBaseUrl + "/login-success?token=" + jwtToken);
    }

    /**
     * Zet een OneRoster-rol om naar het interne Rol-enum.
     *
     * @param oneRosterRole de rol zoals teruggegeven door OneRoster
     * @return het overeenkomstige Rol-enum, of null als de rol onbekend is
     */
    private Rol mapRol(String oneRosterRole) {
        if (oneRosterRole == null) return null;
        return switch (oneRosterRole.toLowerCase()) {
            case "student" -> Rol.LEERLING;
            case "teacher" -> Rol.LEERKRACHT;
            default -> null;
        };
    }
}
