package ap.be.backend.seeds;

import java.util.List;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import ap.be.backend.entity.Gebruiker;
import ap.be.backend.repository.GebruikerRepository;

@Component
public class LeerlingenSeed implements CommandLineRunner {
    private final GebruikerRepository repo;

    public LeerlingenSeed(GebruikerRepository repo) {
        this.repo = repo;
    }

    @Override
    public void run(String... args) {
        if (repo.count() == 0) {
            repo.saveAll(List.of(
                    new Gebruiker("Amira", "El Idrissi", "2a"),
                    new Gebruiker("Amira", "Sels", "2b"),
                    new Gebruiker("Bram", "Peeters", "2a"),
                    new Gebruiker("Céline", "Dupont", "1a"),
                    new Gebruiker("Daan", "Janssens", "2a"),
                    new Gebruiker("Eva", "De Smet", "2a"),
                    new Gebruiker("Fatih", "Yilmaz", "2a"),
                    new Gebruiker("Gitte", "Vermeulen", "2a"),
                    new Gebruiker("Lotte", "De Vos", "1a"),
                    new Gebruiker("Noah", "Maes", "1b"),
                    new Gebruiker("Lars", "Peeters", "3a"),
                    new Gebruiker("Mila", "Claes", "2b"),
                    new Gebruiker("Finn", "Willems", "1a"),
                    new Gebruiker("Elena", "Desmet", "3a"),
                    new Gebruiker("Arthur", "Goossens", "2a"),
                    new Gebruiker("Emma", "Wouters", "1b"),
                    new Gebruiker("Lucas", "Mertens", "2b"),
                    new Gebruiker("Olivia", "Jacobs", "3a")));
        }
    }
}
