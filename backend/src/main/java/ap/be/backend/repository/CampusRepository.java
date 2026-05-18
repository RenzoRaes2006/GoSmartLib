package ap.be.backend.repository;

import ap.be.backend.entity.Campus;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CampusRepository extends JpaRepository<Campus, Long> {
}
