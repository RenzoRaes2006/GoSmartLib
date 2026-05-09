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

        // Bestaande user updaten met huidige klas/rol uit OneRoster, of nieuwe aanmaken.
        // GEEN persoonsdata opslaan (geen voornaam, achternaam, email).
        userRepository.findBySmartschoolId(smartschoolId)
                .map(existing -> {
                    existing.setKlas(klasFinal);
                    existing.setRol(rolEnum);
                    return userRepository.save(existing);
                })
                .orElseGet(() -> userRepository.save(new User(smartschoolId, klasFinal, rolEnum)));

        String jwtToken = jwtService.generateToken(smartschoolId, rol, naam, klas);

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
