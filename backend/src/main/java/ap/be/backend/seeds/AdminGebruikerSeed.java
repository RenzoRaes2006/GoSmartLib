package ap.be.backend.seeds;

import ap.be.backend.entity.AdminGebruiker;
import ap.be.backend.repository.AdminGebruikerRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@Order(1)
@Profile("!test")
public class AdminGebruikerSeed implements CommandLineRunner {

    private final AdminGebruikerRepository adminGebruikerRepository;
    private final PasswordEncoder passwordEncoder;
    private final String initieelWachtwoord;

    public AdminGebruikerSeed(AdminGebruikerRepository adminGebruikerRepository,
                              PasswordEncoder passwordEncoder,
                              @Value("${ADMIN_INITIAL_PASSWORD:}") String initieelWachtwoord) {
        this.adminGebruikerRepository = adminGebruikerRepository;
        this.passwordEncoder = passwordEncoder;
        this.initieelWachtwoord = initieelWachtwoord;
    }

    @Override
    public void run(String... args) {
        if (adminGebruikerRepository.findByGebruikersnaam("admin").isPresent()) {
            return;
        }
        if (initieelWachtwoord == null || initieelWachtwoord.isBlank()) {
            log.warn("Geen ADMIN_INITIAL_PASSWORD gezet — admin-seed overgeslagen. Voeg de admin handmatig toe in de DB.");
            return;
        }
        AdminGebruiker admin = new AdminGebruiker(
                "admin",
                passwordEncoder.encode(initieelWachtwoord),
                "Beheerder"
        );
        adminGebruikerRepository.save(admin);
    }
}
