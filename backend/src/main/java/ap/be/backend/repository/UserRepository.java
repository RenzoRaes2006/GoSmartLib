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

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findBySmartschoolId(String smartschoolId);

    @Query("SELECT u FROM User u WHERE (:klas IS NULL OR :klas = '' OR u.klas = :klas)")
    Page<User> findByKlas(@Param("klas") String klas, Pageable pageable);

    @Query("SELECT u FROM User u WHERE u.rol = :rol AND (:klas IS NULL OR :klas = '' OR u.klas = :klas)")
    Page<User> findByRolAndKlas(@Param("rol") Rol rol, @Param("klas") String klas, Pageable pageable);

    long countByKlasIgnoreCase(String klas);

    List<User> findByRol(Rol rol);

    @Query("SELECT u FROM User u WHERE u.rol = ap.be.backend.entity.Rol.LEERLING AND " +
            "(:klas IS NULL OR :klas = '' OR u.klas = :klas) AND " +
            "(:zoekterm IS NULL OR :zoekterm = '' OR " +
            "LOWER(u.smartschoolId) LIKE LOWER(CONCAT('%', :zoekterm, '%')))")
    Page<User> findLeerlingenMetFilters(
            @Param("klas") String klas,
            @Param("zoekterm") String zoekterm,
            Pageable pageable);
}
