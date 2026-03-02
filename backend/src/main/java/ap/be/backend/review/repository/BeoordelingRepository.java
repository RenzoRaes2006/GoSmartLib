package ap.be.backend.review.repository;

import java.util.List;

import org.springframework.data.repository.ListCrudRepository;

import ap.be.backend.review.entity.Beoordeling;

public interface BeoordelingRepository extends ListCrudRepository<Beoordeling, Long> {
    List<Beoordeling> findByBoekId(long boekId);
}