package ap.be.backend.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import ap.be.backend.entity.Boek;

public interface BoekRepository extends JpaRepository<Boek, Long> {
        Optional<Boek> findByIsbn(String isbn);

        @Query("SELECT DISTINCT b FROM Boek b LEFT JOIN b.genre g WHERE " +
                        "(:zoekterm IS NULL OR LOWER(b.titel) LIKE LOWER(CONCAT('%', :zoekterm, '%')) OR " +
                        "LOWER(b.auteur) LIKE LOWER(CONCAT('%', :zoekterm, '%')) OR " +
                        "b.isbn LIKE CONCAT('%', :zoekterm, '%')) AND " +
                        "(:genres IS NULL OR g IN :genres) AND " +
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
                        @Param("genres") List<String> genres,
                        @Param("uitgever") String uitgever,
                        @Param("minPagina") Integer minPagina,
                        @Param("maxPagina") Integer maxPagina,
                        @Param("beschikbaar") Boolean beschikbaar,
                        @Param("uitgaveJaarVan") Integer uitgaveJaarVan,
                        @Param("uitgaveJaarTot") Integer uitgaveJaarTot,
                        @Param("populariteit") String populariteit,
                        @Param("leesniveau") String leesniveau,
                        Sort sort);

        @Query(value = "SELECT b.* FROM boek b LEFT JOIN leeslijst_map_boeken lmb ON b.id = lmb.boek_id GROUP BY b.id ORDER BY COUNT(lmb.map_id) DESC LIMIT 5", nativeQuery = true)
        List<Boek> findTop5PopulaireBoeken();
}
