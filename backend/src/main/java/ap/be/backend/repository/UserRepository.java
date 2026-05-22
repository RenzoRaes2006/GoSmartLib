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

    // Telt enkel actieve gebruikers van een specifieke rol — gebruikt door leerkracht-statistieken
    // zodat vertrokken leerlingen en niet-leerlingen niet worden meegeteld.
    long countByRolAndActiefTrue(Rol rol);

    long countByRolAndKlasIgnoreCaseAndActiefTrue(Rol rol, String klas);

    List<User> findByRol(Rol rol);

    Page<User> findByRol(Rol rol, Pageable pageable);

    List<User> findByRolAndSchool_Id(Rol rol, Long schoolId);

    List<User> findBySchool_Id(Long schoolId);

    // Alleen actieve leerlingen tonen — vertrokken leerlingen blijven in de DB voor historiek
    // maar mogen niet in leerkracht-listings of admin-UI verschijnen.
    @Query("SELECT u FROM User u WHERE u.rol = ap.be.backend.entity.Rol.LEERLING AND " +
            "u.school.id = :schoolId AND u.actief = true AND " +
            "(:klas IS NULL OR :klas = '' OR u.klas = :klas)")
    Page<User> findLeerlingenMetFilters(
            @Param("schoolId") Long schoolId,
            @Param("klas") String klas,
            Pageable pageable);

    @Query("SELECT u FROM User u WHERE u.rol = ap.be.backend.entity.Rol.LEERLING AND " +
            "u.school.id = :schoolId AND u.actief = true AND " +
            "(:klas IS NULL OR :klas = '' OR u.klas = :klas)")
    List<User> findLeerlingenVoorZoekopdracht(
            @Param("schoolId") Long schoolId,
            @Param("klas") String klas);

    // Alle leerlingen incl. vertrokken — voor de admin-UI lifecycle-tab waar je vertrek wilt
    // kunnen ongedaan maken of historiek wilt zien. Server-side paging.
    @Query("SELECT u FROM User u WHERE u.rol = ap.be.backend.entity.Rol.LEERLING AND " +
            "u.school.id = :schoolId ORDER BY u.actief DESC, u.smartschoolId ASC")
    Page<User> findAlleLeerlingenInclVertrokken(@Param("schoolId") Long schoolId, Pageable pageable);

    // Leerkrachten van een school, paged. Met `actiefOnly=true` worden alleen actieve teruggegeven —
    // voor lifecycle-UI zet je 'm op false om vertrokken ook te zien.
    @Query("SELECT u FROM User u WHERE u.rol = ap.be.backend.entity.Rol.LEERKRACHT AND " +
            "u.school.id = :schoolId AND (:actiefOnly = false OR u.actief = true) " +
            "ORDER BY u.actief DESC, u.smartschoolId ASC")
    Page<User> findLeerkrachtenVanSchool(
            @Param("schoolId") Long schoolId,
            @Param("actiefOnly") boolean actiefOnly,
            Pageable pageable);

    @Query("SELECT DISTINCT u.klas FROM User u WHERE u.klas IS NOT NULL AND u.klas <> '' ORDER BY u.klas")
    List<String> findDistinctKlassen();

    @Query("SELECT DISTINCT u.klas FROM User u WHERE u.klas IS NOT NULL AND u.klas <> '' " +
            "AND u.school.id = :schoolId ORDER BY u.klas")
    List<String> findDistinctKlassenBySchoolId(@Param("schoolId") Long schoolId);

    // Geeft alle actieve leerlingen van een school waarvan de smartschoolId NIET in de meegegeven set zit.
    // Wordt gebruikt bij rollover om te bepalen welke leerlingen vertrokken zijn (niet meer in OneRoster).
    // LET OP: gooi een fout in de service als smartschoolIds leeg is — dan zou iedereen als vertrokken worden gemarkeerd.
    @Query("SELECT u FROM User u WHERE u.school.id = :schoolId AND u.rol = ap.be.backend.entity.Rol.LEERLING AND u.actief = true AND u.smartschoolId NOT IN :smartschoolIds")
    List<User> findVertrokkenLeerlingen(@Param("schoolId") Long schoolId, @Param("smartschoolIds") Set<String> smartschoolIds);

    // Geeft alle leerkrachten van een school — nodig om bij rollover hun leeslijsten te archiveren.
    List<User> findBySchool_IdAndRol(Long schoolId, Rol rol);

}
