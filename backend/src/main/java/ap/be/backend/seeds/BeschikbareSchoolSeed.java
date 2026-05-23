package ap.be.backend.seeds;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import ap.be.backend.entity.BeschikbareSchool;
import ap.be.backend.repository.BeschikbareSchoolRepository;

@Component
@Profile("!test")
@Order(10)
public class BeschikbareSchoolSeed implements CommandLineRunner {

    private static final String[][] SCHOLEN = {
        { "AP Hogeschool Antwerpen", "Noorderplaats 2, 2000 Antwerpen" },
        { "GO! Atheneum Antwerpen", "Franklin Rooseveltplaats 11, 2060 Antwerpen" },
        { "GO! Koninklijk Lyceum Antwerpen", "Kasteelpleinstraat 31, 2000 Antwerpen" },
        { "GO! Kunsthumaniora Antwerpen", "Karel Oomsstraat 24, 2018 Antwerpen" },
        { "GO! Topsportschool Antwerpen", "Edegemsesteenweg 100, 2610 Wilrijk" },
        { "GO! Spectrumschool (Campus Ruggeveld)", "Ruggeveldlaan 496, 2100 Deurne" },
        { "GO! Spectrumschool (Campus Plantin)", "Plantin en Moretuslei 165, 2140 Borgerhout" },
        { "GO! Atheneum Deurne", "Hooftvunderlei 160, 2100 Deurne" },
        { "GO! Atheneum Hoboken", "Lage Weg 100, 2660 Hoboken" },
        { "GO! De Scheepvaartschool - Cenflumarin", "Gloriantlaan 75, 2050 Antwerpen" },
        { "GO! Atheneum Schoten", "Villerslei 124, 2900 Schoten" },
        { "GO! Atheneum Brasschaat", "Augustijnslei 46, 2930 Brasschaat" },
        { "GO! KTA Kapellen", "Pastoor Vandenhoudtstraat 8, 2950 Kapellen" },
        { "GO! Middenschool Centrum Antwerpen", "Lange Nieuwstraat 105, 2000 Antwerpen" },
        { "GO! KMS (Koninklijke Middenschool)", "Kasteelpleinstraat 31, 2000 Antwerpen" },
        { "GO! De Es (Buitengewoon secundair)", "Karel Oomsstraat 24, 2018 Antwerpen" },
        { "GO! SBSO Zonnebos (Campus Brasschaat)", "Sint-Antoniuslei 95, 2930 Brasschaat" },
        { "GO! SBSO Zonnebos (Campus Antwerpen)", "Karel Oomsstraat 24, 2018 Antwerpen" },
    };

    private final BeschikbareSchoolRepository repository;

    public BeschikbareSchoolSeed(BeschikbareSchoolRepository repository) {
        this.repository = repository;
    }

    @Override
    public void run(String... args) {
        for (String[] school : SCHOLEN) {
            if (!repository.existsByNaam(school[0])) {
                repository.save(new BeschikbareSchool(school[0], school[1]));
            }
        }
    }
}
