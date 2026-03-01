package ap.be.backend.repository;

import java.util.Optional;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import ap.be.backend.entity.Boek;

@Repository
public interface BoekRepository extends CrudRepository<Boek, Long> {
    Optional<Boek> findByIsbn(String isbn);
}
