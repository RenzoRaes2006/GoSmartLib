package ap.be.backend.Repos;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import ap.be.backend.entity.Gebruiker;

@Repository
public interface GebruikerRepository extends JpaRepository<Gebruiker, Long> {
}
