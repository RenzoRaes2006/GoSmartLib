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
                        "(:zoekterm IS NULL OR LOWER(b.titel) LIKE LOWER(CONCAT('%', :zoekterm, '%')) OR " +
                        "LOWER(b.auteur) LIKE LOWER(CONCAT('%', :zoekterm, '%')) OR " +
                        "b.isbn LIKE CONCAT('%', :zoekterm, '%')) AND " +
                        "(:genre IS NULL OR :genre MEMBER OF b.genre) AND " +
                        "(:uitgever IS NULL OR LOWER(b.uitgever) LIKE LOWER(CONCAT('%', :uitgever, '%'))) AND " +
                        "(:minPagina IS NULL OR b.pagina >= :minPagina) AND " +
                        "(:maxPagina IS NULL OR b.pagina <= :maxPagina) AND " +
                        "(:beschikbaar IS NULL OR " +
                        "  (:beschikbaar = true AND b.aantalExemplaren > 0) OR " +
                        "  (:beschikbaar = false AND b.aantalExemplaren = 0)) AND " +
                        "(:uitgaveJaarVan IS NULL OR b.uitgaveJaar >= :uitgaveJaarVan) AND " +
                        "(:uitgaveJaarTot IS NULL OR b.uitgaveJaar <= :uitgaveJaarTot) AND " +
                        "(:populariteit IS NULL OR " +
                        "  (:populariteit = '1' AND (SELECT AVG(be.waardering) FROM Beoordeling be WHERE be.boek.id = b.id) >= 4)) AND "
                        +
                        "(:leesniveau IS NULL OR b.leesniveau = :leesniveau)")
        List<Boek> filterBoeken(@Param("zoekterm") String zoekterm,
                        @Param("genre") String genre,
                        @Param("uitgever") String uitgever,
                        @Param("minPagina") Integer minPagina,
                        @Param("maxPagina") Integer maxPagina,
                        @Param("beschikbaar") Boolean beschikbaar,
                        @Param("uitgaveJaarVan") Integer uitgaveJaarVan,
                        @Param("uitgaveJaarTot") Integer uitgaveJaarTot,
                        @Param("populariteit") String populariteit,
                        @Param("leesniveau") String leesniveau,
                        Sort sort);
}
