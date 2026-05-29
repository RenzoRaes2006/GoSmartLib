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
                        "  (:beschikbaar = true AND EXISTS (SELECT 1 FROM Exemplaar e WHERE e.boek = b AND e.status = ap.be.backend.entity.ExemplaarStatus.BESCHIKBAAR)) OR " +
                        "  (:beschikbaar = false AND NOT EXISTS (SELECT 1 FROM Exemplaar e WHERE e.boek = b AND e.status = ap.be.backend.entity.ExemplaarStatus.BESCHIKBAAR))) AND " +
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

        // De afgeleide @Formula `aantalBeschikbaar` op Boek wordt door Hibernate niet
        // automatisch aan een native SELECT toegevoegd; daarom moet de subquery hier
        // expliciet als gelijknamige kolom meegegeven worden, anders faalt de entity-mapping
        // met "Column 'aantalBeschikbaar' not found".
        @Query(value = "SELECT b.*, " +
                        "(SELECT COUNT(e.id) FROM exemplaren e WHERE e.boek_id = b.id AND e.status = 'BESCHIKBAAR') AS aantalBeschikbaar, " +
                        "(SELECT COUNT(e.id) FROM exemplaren e WHERE e.boek_id = b.id AND e.status = 'BESCHADIGD') AS aantalBeschadigd " +
                        "FROM boeken b LEFT JOIN leeslijst_map_boeken lmb ON b.id = lmb.boek_id " +
                        "GROUP BY b.id ORDER BY COUNT(lmb.map_id) DESC LIMIT 5", nativeQuery = true)
        List<Boek> findTop5PopulaireBoeken();

        Optional<Boek> findByTitelAndAuteur(String titel, String auteur);

        // Aantal beschikbare exemplaren over de hele collectie. Behoudt de oude
        // semantiek (het vroegere aantalExemplaren-veld was óók de beschikbare voorraad),
        // zodat de dashboard-statistieken vóór en na de migratie blijven kloppen.
        @Query("SELECT COUNT(e) FROM Exemplaar e WHERE e.status = ap.be.backend.entity.ExemplaarStatus.BESCHIKBAAR")
        long sumTotaalAantalExemplaren();

        @Query("SELECT b FROM Boek b JOIN b.tags t WHERE t.id = :tagId")
        List<Boek> findAllMetTag(@Param("tagId") Long tagId);

}
