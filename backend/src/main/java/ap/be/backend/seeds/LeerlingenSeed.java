package ap.be.backend.seeds;

import java.util.List;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import ap.be.backend.entity.Rol;
import ap.be.backend.entity.User;
import ap.be.backend.repository.UserRepository;

@Component
@Profile("dev")
public class LeerlingenSeed implements CommandLineRunner {
    private final UserRepository repo;

    public LeerlingenSeed(UserRepository repo) {
        this.repo = repo;
    }

    @Override
    public void run(String... args) {
        if (repo.count() == 0) {
            repo.saveAll(List.of(
                    new User("amira.elidrissi", "2a", Rol.LEERLING),
                    new User("amira.sels", "2b", Rol.LEERLING),
                    new User("bram.peeters", "2a", Rol.LEERLING),
                    new User("celine.dupont", "1a", Rol.LEERLING),
                    new User("daan.janssens", "2a", Rol.LEERLING),
                    new User("eva.desmet", "2a", Rol.LEERLING),
                    new User("fatih.yilmaz", "2a", Rol.LEERLING),
                    new User("gitte.vermeulen", "2a", Rol.LEERLING),
                    new User("lotte.devos", "1a", Rol.LEERLING),
                    new User("noah.maes", "1b", Rol.LEERLING),
                    new User("lars.peeters", "3a", Rol.LEERLING),
                    new User("mila.claes", "2b", Rol.LEERLING),
                    new User("finn.willems", "1a", Rol.LEERLING),
                    new User("elena.desmet", "3a", Rol.LEERLING),
                    new User("arthur.goossens", "2a", Rol.LEERLING),
                    new User("emma.wouters", "1b", Rol.LEERLING),
                    new User("lucas.mertens", "2b", Rol.LEERLING),
                    new User("olivia.jacobs", "3a", Rol.LEERLING)));
        }
    }
}
