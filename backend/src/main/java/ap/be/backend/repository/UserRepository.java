package ap.be.backend.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import ap.be.backend.entity.Rol;
import ap.be.backend.entity.User;

import java.util.Set;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findBySmartschoolId(String smartschoolId);

    @Query("SELECT u FROM User u WHERE (:klas IS NULL OR :klas = '' OR u.klas = :klas)")
    Page<User> findByKlas(@Param("klas") String klas, Pageable pageable);

    @Query("SELECT u FROM User u WHERE u.rol = :rol AND (:klas IS NULL OR :klas = '' OR u.klas = :klas)")
    Page<User> findByRolAndKlas(@Param("rol") Rol rol, @Param("klas") String klas, Pageable pageable);

    long countByKlasIgnoreCase(String klas);

    List<User> findByRol(Rol rol);

    List<User> findByRolAndSchool_Id(Rol rol, Long schoolId);

    List<User> findBySchool_Id(Long schoolId);

    @Query("SELECT u FROM User u WHERE u.rol = ap.be.backend.entity.Rol.LEERLING AND " +
            "u.school.id = :schoolId AND " +
            "(:klas IS NULL OR :klas = '' OR u.klas = :klas) AND " +
            "(:zoekterm IS NULL OR :zoekterm = '' OR " +
            "LOWER(u.smartschoolId) LIKE LOWER(CONCAT('%', :zoekterm, '%')))")
    Page<User> findLeerlingenMetFilters(
            @Param("schoolId") Long schoolId,
            @Param("klas") String klas,
            @Param("zoekterm") String zoekterm,
            Pageable pageable);

    @Query("SELECT DISTINCT u.klas FROM User u WHERE u.klas IS NOT NULL AND u.klas <> '' ORDER BY u.klas")
    List<String> findDistinctKlassen();

    // Geeft alle actieve leerlingen van een school waarvan de smartschoolId NIET in de meegegeven set zit.
    // Wordt gebruikt bij rollover om te bepalen welke leerlingen vertrokken zijn (niet meer in OneRoster).
    // LET OP: gooi een fout in de service als smartschoolIds leeg is — dan zou iedereen als vertrokken worden gemarkeerd.
    @Query("SELECT u FROM User u WHERE u.school.id = :schoolId AND u.rol = ap.be.backend.entity.Rol.LEERLING AND u.actief = true AND u.smartschoolId NOT IN :smartschoolIds")
    List<User> findVertrokkenLeerlingen(@Param("schoolId") Long schoolId, @Param("smartschoolIds") Set<String> smartschoolIds);

    // Geeft alle leerkrachten van een school — nodig om bij rollover hun leeslijsten te archiveren.
    List<User> findBySchool_IdAndRol(Long schoolId, Rol rol);

}
