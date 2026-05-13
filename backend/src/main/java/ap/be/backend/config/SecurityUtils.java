package ap.be.backend.config;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import ap.be.backend.entity.User;
import ap.be.backend.exception.GebruikerNotFoundException;
import ap.be.backend.repository.UserRepository;

public final class SecurityUtils {

    private SecurityUtils() {
    }

    public static String getCurrentSmartschoolId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || auth.getPrincipal() == null) {
            throw new IllegalStateException("Geen geauthenticeerde gebruiker in de SecurityContext.");
        }
        return auth.getName();
    }

    public static User getCurrentUser(UserRepository repository) {
        String smartschoolId = getCurrentSmartschoolId();
        return repository.findBySmartschoolId(smartschoolId)
                .orElseThrow(() -> new GebruikerNotFoundException(
                        "Geen user gevonden voor smartschoolId: " + smartschoolId));
    }
}
