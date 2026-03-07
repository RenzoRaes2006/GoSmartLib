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
        if (gebruikerRepository.count() == 0) {

            Gebruiker testGebruiker = new Gebruiker();
            testGebruiker.setVoorNaam("Test");
            testGebruiker.setAchternaam("Gebruiker");
            testGebruiker.setEmail("test@gosmartlib.be");
            testGebruiker.setSchoolId(12345L);
            testGebruiker.setRol(Rol.LEERLING);

            Gebruiker opgeslagenGebruiker = gebruikerRepository.save(testGebruiker);

            LeeslijstMap favorietenMap = new LeeslijstMap("Favorieten", opgeslagenGebruiker);
            mapRepository.save(favorietenMap);

            System.out.println("Test Gebruiker (ID 1) is succesvol toegevoegd aan de database!");
        }
    }
}