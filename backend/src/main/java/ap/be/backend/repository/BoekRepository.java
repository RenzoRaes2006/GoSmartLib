package ap.be.backend.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import ap.be.backend.entity.Boek;

public interface BoekRepository extends JpaRepository<Boek, Long> {
        Optional<Boek> findByIsbn(String isbn);

        @Query("SELECT DISTINCT b FROM Boek b " +
                        "LEFT JOIN b.tags t " +
                        "LEFT JOIN b.campus c " +
                        "LEFT JOIN c.school s WHERE " +
                        "(:schoolId IS NULL OR c IS NULL OR s.id = :schoolId) AND " +
                        "(:campusIds IS NULL OR c.id IN :campusIds) AND " +
                        "(:didactischMateriaal IS NULL OR b.didactischMateriaal = :didactischMateriaal) AND " +
                        "(:zoekterm IS NULL OR LOWER(b.titel) LIKE LOWER(CONCAT('%', :zoekterm, '%')) OR " +
                        "LOWER(b.auteur) LIKE LOWER(CONCAT('%', :zoekterm, '%')) OR " +
                        "b.isbn LIKE CONCAT('%', :zoekterm, '%')) AND " +
                        "(:tagIds IS NULL OR t.id IN :tagIds) AND " +
                        "(:uitgever IS NULL OR LOWER(b.uitgever) LIKE LOWER(CONCAT('%', :uitgever, '%'))) AND " +
                        "(:minPagina IS NULL OR b.pagina >= :minPagina) AND " +
                        "(:maxPagina IS NULL OR b.pagina <= :maxPagina) AND " +
                        "(:beschikbaar IS NULL OR " +
                        "  (:beschikbaar = true AND b.aantalExemplaren > 0) OR " +
                        "  (:beschikbaar = false AND b.aantalExemplaren = 0)) AND " +
                        "(:uitgaveJaarVan IS NULL OR b.uitgaveJaar >= :uitgaveJaarVan) AND " +
                        "(:uitgaveJaarTot IS NULL OR b.uitgaveJaar <= :uitgaveJaarTot) AND " +
                        "(:leesniveau IS NULL OR b.leesniveau = :leesniveau) AND " +
                        "(:minSterren IS NULL OR " +
                        "  (SELECT COALESCE(AVG(beo.waardering), 0) FROM Beoordeling beo WHERE beo.boek = b) >= :minSterren)")
        Page<Boek> filterBoeken(@Param("zoekterm") String zoekterm,
                        @Param("tagIds") List<Long> tagIds,
                        @Param("uitgever") String uitgever,
                        @Param("minPagina") Integer minPagina,
                        @Param("maxPagina") Integer maxPagina,
                        @Param("beschikbaar") Boolean beschikbaar,
                        @Param("uitgaveJaarVan") Integer uitgaveJaarVan,
                        @Param("uitgaveJaarTot") Integer uitgaveJaarTot,
                        @Param("leesniveau") String leesniveau,
                        @Param("didactischMateriaal") Boolean didactischMateriaal,
                        @Param("schoolId") Long schoolId,
                        @Param("campusIds") List<Long> campusIds,
                        @Param("minSterren") Integer minSterren,
                        Pageable pageable);

        long countByCampus_Id(Long campusId);

        Page<Boek> findByDidactischMateriaalTrue(Pageable pageable);

        @Query(value = "SELECT b.* FROM boeken b LEFT JOIN leeslijst_map_boeken lmb ON b.id = lmb.boek_id GROUP BY b.id ORDER BY COUNT(lmb.map_id) DESC LIMIT 5", nativeQuery = true)
        List<Boek> findTop5PopulaireBoeken();

        Optional<Boek> findByTitelAndAuteur(String titel, String auteur);

        @Query("SELECT COALESCE(SUM(b.aantalExemplaren), 0) FROM Boek b")
        long sumTotaalAantalExemplaren();

}
