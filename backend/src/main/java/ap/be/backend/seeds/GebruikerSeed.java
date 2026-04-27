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

            // voeg 3 - 4 extra gebruikers toe
            Gebruiker gebruiker2 = new Gebruiker();
            gebruiker2.setVoorNaam("Jan");
            gebruiker2.setAchternaam("Jansen");
            gebruiker2.setEmail("jan.jansen@gosmartlib.be");
            gebruiker2.setSchoolId(12346L);
            gebruiker2.setRol(Rol.LEERLING);

            Gebruiker gebruiker3 = new Gebruiker();
            gebruiker3.setVoorNaam("Piet");
            gebruiker3.setAchternaam("Pieters");
            gebruiker3.setEmail("piet.pieters@gosmartlib.be");
            gebruiker3.setSchoolId(12347L);
            gebruiker3.setRol(Rol.LEERLING);

            Gebruiker gebruiker4 = new Gebruiker();
            gebruiker4.setVoorNaam("Klaas");
            gebruiker4.setAchternaam("Klaassen");
            gebruiker4.setEmail("klaas.klaassen@gosmartlib.be");
            gebruiker4.setSchoolId(12348L);
            gebruiker4.setRol(Rol.LEERLING);
            
            Gebruiker opgeslagenGebruiker = gebruikerRepository.save(testGebruiker);

            gebruikerRepository.save(gebruiker2);
            gebruikerRepository.save(gebruiker3);
            gebruikerRepository.save(gebruiker4);

            
            LeeslijstMap favorietenMap = new LeeslijstMap("Favorieten", opgeslagenGebruiker);
            mapRepository.save(favorietenMap);

            System.out.println("Test Gebruiker (ID 1) is succesvol toegevoegd aan de database!");
        }
    }
}