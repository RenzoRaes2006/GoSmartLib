package ap.be.backend.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import ap.be.backend.entity.Boek;

public interface BoekRepository extends JpaRepository<Boek, Long> {
    Optional<Boek> findByIsbn(String isbn);
}
