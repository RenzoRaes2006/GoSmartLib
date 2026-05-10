package ap.be.backend.seeds;

import java.util.List;

import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import ap.be.backend.entity.Rol;
import ap.be.backend.entity.User;
import ap.be.backend.repository.UserRepository;

/**
 * Vult de database bij het opstarten met testleerlingen als ze nog niet bestaan.
 *
 * Elk record bevat een smartschoolId en een klas. Bestaande gebruikers worden
 * overgeslagen. Draait als tweede in de volgorde van seeders (@Order(2)).
 */
@Component
@Order(2)
public class LeerlingenSeed implements CommandLineRunner {
    private final UserRepository repo;

    public LeerlingenSeed(UserRepository repo) {
        this.repo = repo;
    }

    @Override
    public void run(String... args) {
        List<String[]> seeds = List.of(
                new String[] { "leerling-001", "2a" },
                new String[] { "leerling-002", "2b" },
                new String[] { "leerling-003", "2a" },
                new String[] { "leerling-004", "1a" },
                new String[] { "leerling-005", "2a" },
                new String[] { "leerling-006", "2a" },
                new String[] { "leerling-007", "2a" },
                new String[] { "leerling-008", "2a" },
                new String[] { "leerling-009", "1a" },
                new String[] { "leerling-010", "1b" },
                new String[] { "leerling-011", "3a" },
                new String[] { "leerling-012", "2b" },
                new String[] { "leerling-013", "1a" },
                new String[] { "leerling-014", "3a" },
                new String[] { "leerling-015", "2a" },
                new String[] { "leerling-016", "1b" },
                new String[] { "leerling-017", "2b" },
                new String[] { "leerling-018", "3a" });

        for (String[] s : seeds) {
            String smartschoolId = s[0];
            String klas = s[1];
            if (repo.findBySmartschoolId(smartschoolId).isEmpty()) {
                repo.save(new User(smartschoolId, klas, Rol.LEERLING));
            }
        }
    }
}
