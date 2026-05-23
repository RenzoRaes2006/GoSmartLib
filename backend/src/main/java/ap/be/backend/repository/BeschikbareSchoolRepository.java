package ap.be.backend.repository;

import ap.be.backend.entity.BeschikbareSchool;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BeschikbareSchoolRepository extends JpaRepository<BeschikbareSchool, Long> {
    boolean existsByNaam(String naam);
}
