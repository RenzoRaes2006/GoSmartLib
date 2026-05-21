package ap.be.backend.repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.springframework.data.repository.ListCrudRepository;

import ap.be.backend.entity.Beoordeling;
import ap.be.backend.entity.Boek;

public interface BeoordelingRepository extends ListCrudRepository<Beoordeling, Long> {
    List<Beoordeling> findByBoekId(long boekId);

    // Geeft alle niet-anonieme beoordelingen van een lijst gebruikers.
    // Wordt bij rollover gebruikt om de reviews van vertrokken leerlingen anoniem te maken.
    List<Beoordeling> findByUser_IdInAndIsAnoniemFalse(Collection<Long> userIds);
}