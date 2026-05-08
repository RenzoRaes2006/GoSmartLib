package ap.be.backend.config;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import ap.be.backend.entity.Gebruiker;
import ap.be.backend.exception.GebruikerNotFoundException;
import ap.be.backend.repository.GebruikerRepository;

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

    public static Gebruiker getCurrentGebruiker(GebruikerRepository repository) {
        String smartschoolId = getCurrentSmartschoolId();
        return repository.findBySmartschoolId(smartschoolId)
                .orElseThrow(() -> new GebruikerNotFoundException(
                        "Geen gebruiker gevonden voor smartschoolId: " + smartschoolId));
    }
}
