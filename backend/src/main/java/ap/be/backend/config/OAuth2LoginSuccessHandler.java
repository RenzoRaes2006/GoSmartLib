package ap.be.backend.config;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Value;

import ap.be.backend.entity.User;
import ap.be.backend.repository.UserRepository;

import java.io.IOException;

@Component
public class OAuth2LoginSuccessHandler implements AuthenticationSuccessHandler {

    @Autowired
    private JwtService jwtService;

    @Autowired
    private UserRepository userRepository;

    @Value("${app.frontend.url}")
    private String frontendBaseUrl;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
            Authentication authentication) throws IOException, ServletException {

        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();

        // hier moet ik nog kijken naar de documentatie van smartschool voor de juiste attribute namen
        String smartschoolId = oAuth2User.getAttribute("id");

        userRepository.findBySmartschoolId(smartschoolId)
                .orElseGet(() -> userRepository.save(new User(smartschoolId)));

        String jwtToken = jwtService.generateToken(smartschoolId);

        String redirectUrl = frontendBaseUrl + "/login-success?token=" + jwtToken;

        response.sendRedirect(redirectUrl);
    }
}