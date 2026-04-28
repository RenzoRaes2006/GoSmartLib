package ap.be.backend.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.repository.ListCrudRepository;

import ap.be.backend.entity.Beoordeling;
import ap.be.backend.entity.Boek;

public interface BeoordelingRepository extends ListCrudRepository<Beoordeling, Long> {
    List<Beoordeling> findByBoekId(long boekId);

}