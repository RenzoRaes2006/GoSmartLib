package ap.be.backend.seeds;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import ap.be.backend.entity.Rol;
import ap.be.backend.entity.School;
import ap.be.backend.entity.User;
import ap.be.backend.repository.SchoolRepository;
import ap.be.backend.repository.UserRepository;

/**
 * Dev-only: zet 2 scholen klaar en koppelt de DevAuthFilter-user (smartschoolId
 * "12357") als LEERKRACHT van de eerste school. Leerlingen krijgen smartschoolIds
 * die overeenkomen met {@code OneRosterMockClient}, zodat de namen via de live
 * OneRoster-flow opgehaald worden zonder iets te persisteren.
 *
 * Zo kan je {@code GET /api/leerlingen} testen vanaf {@code http://localhost:4200/leerkracht}
 * zonder Smartschool-login.
 */
@Component
@Profile("dev")
@ConditionalOnProperty(name = "gosmartlib.seeds.enabled", havingValue = "true")
@Order(200)
public class SchoolTestSeed implements CommandLineRunner {

    private static final String DEV_LEERKRACHT_ID = "12357";

    private final SchoolRepository schoolRepository;
    private final UserRepository userRepository;

    public SchoolTestSeed(SchoolRepository schoolRepository, UserRepository userRepository) {
        this.schoolRepository = schoolRepository;
        this.userRepository = userRepository;
    }

    @Override
    public void run(String... args) {
        School ap = ensureSchool("AP Hogeschool (test)", "ap-test.smartschool.be", "mock-ap-id", "mock-ap-secret");
        School andere = ensureSchool("Andere School (test)", "andere-test.smartschool.be", "mock-andere-id", "mock-andere-secret");

        // Dev-user wordt LEERKRACHT van AP-test
        User devUser = userRepository.findBySmartschoolId(DEV_LEERKRACHT_ID).orElseGet(() -> {
            User u = new User();
            u.setSmartschoolId(DEV_LEERKRACHT_ID);
            return u;
        });
        devUser.setRol(Rol.LEERKRACHT);
        devUser.setSchool(ap);
        userRepository.save(devUser);

        // Leerlingen van AP-test: sourcedIds matchen OneRosterMockClient (5001-5003)
        ensureLeerling("5001", "2A - Mock", ap);
        ensureLeerling("5002", "1A - Mock", ap);
        ensureLeerling("5003", "1B - Mock", ap);

        // Leerlingen van een andere school — mogen NIET zichtbaar zijn voor de AP-leerkracht
        ensureLeerling("9101", "3A - Andere", andere);
        ensureLeerling("9102", "3B - Andere", andere);
    }

    private School ensureSchool(String naam, String domein, String clientId, String clientSecret) {
        return schoolRepository.findAll().stream()
                .filter(s -> naam.equals(s.getNaam()))
                .findFirst()
                .orElseGet(() -> {
                    School s = new School();
                    s.setNaam(naam);
                    s.setSmartschoolDomein(domein);
                    s.setOauthClientId(clientId);
                    s.setOauthClientSecret(clientSecret);
                    return schoolRepository.save(s);
                });
    }

    private void ensureLeerling(String smartschoolId, String klas, School school) {
        User user = userRepository.findBySmartschoolId(smartschoolId).orElseGet(User::new);
        user.setSmartschoolId(smartschoolId);
        user.setKlas(klas);
        user.setRol(Rol.LEERLING);
        user.setSchool(school);
        userRepository.save(user);
    }
}
