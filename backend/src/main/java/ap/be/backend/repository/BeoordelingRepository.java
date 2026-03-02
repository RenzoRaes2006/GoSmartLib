package ap.be.backend.repository;

import java.util.List;

import org.springframework.data.repository.ListCrudRepository;

import ap.be.backend.entity.Beoordeling;

public interface BeoordelingRepository extends ListCrudRepository<Beoordeling, Long> {
    List<Beoordeling> findByBoekId(long boekId);
}