package ap.be.backend.repository;

import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ap.be.backend.entity.Gebruiker;

@Repository
public interface GebruikerRepository extends JpaRepository<Gebruiker, Long> {

    @Query("select g from Gebruiker g where g.SchoolId = :schoolId")
    Optional<Gebruiker> findBySchoolId(@Param("schoolId") long schoolId);

    Optional<Gebruiker> findBySmartschoolId(String smartschoolId);

    // simple filter by class
    @Query("SELECT g FROM Gebruiker g WHERE (:klas IS NULL OR :klas = '' OR g.klas = :klas)")
    Page<Gebruiker> findByKlas(@Param("klas") String klas, Pageable pageable);

   @Query("SELECT g FROM Gebruiker g WHERE " +
           "(:klas IS NULL OR :klas = '' OR g.klas = :klas) AND " +
           "(:zoekterm IS NULL OR :zoekterm = '' OR " + 
           "LOWER(g.VoorNaam) LIKE LOWER(CONCAT('%', :zoekterm, '%')) OR " +
           "LOWER(g.Achternaam) LIKE LOWER(CONCAT('%', :zoekterm, '%')))")
    Page<Gebruiker> findLeerlingenMetFilters(
            @Param("klas") String klas,
            @Param("zoekterm") String zoekterm,
            Pageable pageable);

    long countByKlasIgnoreCase(String klas);
}