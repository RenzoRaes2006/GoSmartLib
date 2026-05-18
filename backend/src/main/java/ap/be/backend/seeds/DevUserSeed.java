package ap.be.backend.seeds;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import ap.be.backend.repository.UserRepository;

/**
 * Zorgt dat de fake dev-gebruiker (smartschoolId "12357", gebruikt door
 * DevAuthFilter) een klas heeft voor lokale tests met klas-leeslijsten.
 *
 * Runt na de andere seeds (LeerlingenSeed/GebruikerSeed) en update de
 * bestaande user enkel als de klas nog leeg is.
 */
@Component
@Profile("dev")
@Order(100)
public class DevUserSeed implements CommandLineRunner {

    private static final String DEV_SMARTSCHOOL_ID = "12357";
    private static final String DEV_KLAS = "DEV1";

    private final UserRepository userRepository;

    public DevUserSeed(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public void run(String... args) {
        userRepository.findBySmartschoolId(DEV_SMARTSCHOOL_ID).ifPresent(user -> {
            if (user.getKlas() == null || user.getKlas().isBlank()) {
                user.setKlas(DEV_KLAS);
                userRepository.save(user);
            }
        });
    }
}
