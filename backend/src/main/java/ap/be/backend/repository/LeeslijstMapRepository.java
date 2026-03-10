package ap.be.backend.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import ap.be.backend.entity.LeeslijstMap;

@Repository
public interface LeeslijstMapRepository extends JpaRepository<LeeslijstMap, Long> {

    // ophalen alle mapjes van 1 specifieke gebruiker
    List<LeeslijstMap> findByGebruiker_GebruikerID(Long gebruikerId);

    Optional<LeeslijstMap> findByGebruiker_GebruikerIDAndNaam(Long gebruikerId, String naam);
}
