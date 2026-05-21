package ap.be.backend.repository;

import ap.be.backend.entity.Schooljaar;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SchooljaarRepository extends JpaRepository<Schooljaar, Long> {

    // Geeft het huidige actieve schooljaar terug. Er mag maar één actief zijn.
    Optional<Schooljaar> findByActiefTrue();
}
