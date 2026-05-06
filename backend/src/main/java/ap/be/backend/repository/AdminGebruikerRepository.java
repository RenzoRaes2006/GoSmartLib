package ap.be.backend.repository;

import ap.be.backend.entity.AdminGebruiker;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AdminGebruikerRepository extends JpaRepository<AdminGebruiker, Long> {
    Optional<AdminGebruiker> findByGebruikersnaam(String gebruikersnaam);
}
