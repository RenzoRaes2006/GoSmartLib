package ap.be.backend.seeds;

import ap.be.backend.entity.Schooljaar;
import ap.be.backend.repository.SchooljaarRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

/**
 * Zorgt dat er altijd een actief schooljaar in de database staat bij opstart.
 * Als er nog geen enkel schooljaar bestaat, wordt "2024-2025" aangemaakt als actief schooljaar.
 * Dit voorkomt null-pointer fouten in de rest van de applicatie.
 */
@Component
@Order(2)
@Profile("!test")
public class SchooljaarSeed implements CommandLineRunner {

    private final SchooljaarRepository schooljaarRepository;

    public SchooljaarSeed(SchooljaarRepository schooljaarRepository) {
        this.schooljaarRepository = schooljaarRepository;
    }

    @Override
    public void run(String... args) {
        if (schooljaarRepository.count() == 0) {
            Schooljaar eerste = new Schooljaar(
                    "2024-2025",
                    LocalDate.of(2024, 9, 1),
                    LocalDate.of(2025, 6, 30),
                    true
            );
            schooljaarRepository.save(eerste);
        }
    }
}
