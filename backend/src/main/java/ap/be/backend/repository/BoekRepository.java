package ap.be.backend.repository;

import java.util.Optional;

import org.springframework.data.repository.ListCrudRepository;

import ap.be.backend.entity.Boek;

public interface BoekRepository extends ListCrudRepository<Boek, Long> {
    Optional<Boek> findByIsbn(String isbn);
}
