package ap.be.backend.seeds;

import ap.be.backend.entity.AdminGebruiker;
import ap.be.backend.repository.AdminGebruikerRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@Order(1)
@Profile("!test")
public class AdminGebruikerSeed implements CommandLineRunner {

    private final AdminGebruikerRepository adminGebruikerRepository;
    private final PasswordEncoder passwordEncoder;

    public AdminGebruikerSeed(AdminGebruikerRepository adminGebruikerRepository, PasswordEncoder passwordEncoder) {
        this.adminGebruikerRepository = adminGebruikerRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        if (adminGebruikerRepository.findByGebruikersnaam("admin").isEmpty()) {
            AdminGebruiker admin = new AdminGebruiker(
                    "admin",
                    passwordEncoder.encode("Admin@2024!"), //moet anders gedaan worden
                    "Beheerder"
            );
            adminGebruikerRepository.save(admin);
        }
    }
}
