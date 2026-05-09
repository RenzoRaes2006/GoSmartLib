package ap.be.backend.seeds;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import ap.be.backend.entity.LeeslijstMap;
import ap.be.backend.entity.Rol;
import ap.be.backend.entity.User;
import ap.be.backend.repository.LeeslijstMapRepository;
import ap.be.backend.repository.UserRepository;

@Component
public class GebruikerSeed implements CommandLineRunner {
    private final UserRepository userRepository;
    private final LeeslijstMapRepository mapRepository;

    public GebruikerSeed(UserRepository userRepository, LeeslijstMapRepository mapRepository) {
        this.userRepository = userRepository;
        this.mapRepository = mapRepository;
    }

    @Override
    public void run(String... args) throws Exception {
        User test = ensureLeerling("12345");
        ensureLeerling("12346");
        ensureLeerling("12347");
        ensureLeerling("12348");
        ensureLeerling("12349");
        ensureLeerling("12350");
        ensureLeerling("12351");
        ensureLeerling("12352");
        ensureLeerling("12353");
        ensureLeerling("12354");
        ensureLeerling("12355");
        ensureLeerling("12356");
        ensureLeerling("12357");
        ensureLeerling("12358");
        ensureLeerling("12359");
        ensureLeerling("12360");

        if (test != null && mapRepository.findByUser_Id(test.getId()).isEmpty()) {
            mapRepository.save(new LeeslijstMap("Favorieten", test));
        }
    }

    private User ensureLeerling(String smartschoolId) {
        return userRepository.findBySmartschoolId(smartschoolId).orElseGet(() -> {
            User u = new User(smartschoolId, null, Rol.LEERLING);
            return userRepository.save(u);
        });
    }
}
