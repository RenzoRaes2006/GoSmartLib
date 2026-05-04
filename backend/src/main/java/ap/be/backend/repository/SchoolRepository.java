package ap.be.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import ap.be.backend.entity.School;

@Repository
public interface SchoolRepository extends JpaRepository<School, Long> {
}
