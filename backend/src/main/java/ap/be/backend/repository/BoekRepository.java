package ap.be.backend.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import ap.be.backend.entity.Boek;

public interface BoekRepository extends JpaRepository<Boek, Long> {
    Optional<Boek> findByIsbn(String isbn);

    @Query("SELECT b FROM Boek b WHERE " +
            "LOWER(b.titel) LIKE LOWER(CONCAT('%', :term, '%')) OR " +
            "LOWER(b.auteur) LIKE LOWER(CONCAT('%', :term, '%')) OR " +
            "b.isbn LIKE CONCAT('%', :term, '%')")
    List<Boek> searchBoeken(@Param("term") String term, Sort sort);
}
