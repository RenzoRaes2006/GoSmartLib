package ap.be.backend.seeds;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import ap.be.backend.entity.Gebruiker;
import ap.be.backend.entity.LeeslijstMap;
import ap.be.backend.entity.Rol;
import ap.be.backend.repository.GebruikerRepository;
import ap.be.backend.repository.LeeslijstMapRepository;

@Component
public class GebruikerSeed implements CommandLineRunner {
    private final GebruikerRepository gebruikerRepository;
    private final LeeslijstMapRepository mapRepository;

    public GebruikerSeed(GebruikerRepository gebruikerRepository, LeeslijstMapRepository mapRepository) {
        this.gebruikerRepository = gebruikerRepository;
        this.mapRepository = mapRepository;
    }

    @Override
    public void run(String... args) throws Exception {
        Gebruiker test = ensureLeerling(12345L, "Test", "Gebruiker", "test@gosmartlib.be");
        ensureLeerling(12346L, "Jan", "Jansen", "jan.jansen@gosmartlib.be");
        ensureLeerling(12347L, "Piet", "Pieters", "piet.pieters@gosmartlib.be");
        ensureLeerling(12348L, "Klaas", "Klaassen", "klaas.klaassen@gosmartlib.be");
        ensureLeerling(12349L, "Sofie", "Vermeulen", "sofie.vermeulen@gosmartlib.be");
        ensureLeerling(12350L, "Lucas", "De Smet", "lucas.desmet@gosmartlib.be");
        ensureLeerling(12351L, "Emma", "Peeters", "emma.peeters@gosmartlib.be");
        ensureLeerling(12352L, "Noah", "Maes", "noah.maes@gosmartlib.be");
        ensureLeerling(12353L, "Lien", "Willems", "lien.willems@gosmartlib.be");
        ensureLeerling(12354L, "Daan", "Claes", "daan.claes@gosmartlib.be");
        ensureLeerling(12355L, "Fien", "Hermans", "fien.hermans@gosmartlib.be");
        ensureLeerling(12356L, "Sam", "Wouters", "sam.wouters@gosmartlib.be");
        ensureLeerling(12357L, "Lotte", "Janssens", "lotte.janssens@gosmartlib.be");
        ensureLeerling(12358L, "Milan", "De Vos", "milan.devos@gosmartlib.be");
        ensureLeerling(12359L, "Nora", "Smeets", "nora.smeets@gosmartlib.be");
        ensureLeerling(12360L, "Tuur", "Van Damme", "tuur.vandamme@gosmartlib.be");

        if (test != null && mapRepository.findByGebruiker_GebruikerID(test.getGebruikerID()).isEmpty()) {
            mapRepository.save(new LeeslijstMap("Favorieten", test));
        }
    }

    private Gebruiker ensureLeerling(long schoolId, String voorNaam, String achternaam, String email) {
        return gebruikerRepository.findBySchoolId(schoolId).orElseGet(() -> {
            Gebruiker g = new Gebruiker();
            g.setVoorNaam(voorNaam);
            g.setAchternaam(achternaam);
            g.setEmail(email);
            g.setSchoolId(schoolId);
            g.setRol(Rol.LEERLING);
            return gebruikerRepository.save(g);
        });
    }
}