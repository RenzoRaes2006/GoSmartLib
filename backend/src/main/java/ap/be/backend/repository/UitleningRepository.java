package ap.be.backend.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import ap.be.backend.entity.Boek;
import ap.be.backend.entity.Gebruiker;
import ap.be.backend.entity.Uitlening;

@Repository
public interface UitleningRepository extends JpaRepository<Uitlening, Long> {

    long countByIsIngeleverdFalseAndStatusIn(List<String> statussen);

    long countByIsIngeleverdFalseAndStatusInAndInleverDatumBefore(List<String> statussen, LocalDate datum);

    @Query("SELECT g FROM Uitlening u JOIN u.boek b JOIN b.genre g " +
            "WHERE u.status IN :statussen GROUP BY g ORDER BY COUNT(g) DESC")
    List<String> findPopulairsteGenres(@Param("statussen") List<String> statussen, Pageable pageable);

    @Query("SELECT COUNT(u) FROM Uitlening u WHERE u.uitleenDatum >= :sinds AND u.status IN :statussen")
    long countLeningenSinds(@Param("statussen") List<String> statussen, @Param("sinds") LocalDate sinds);

    @Query("SELECT COUNT(u) FROM Uitlening u WHERE u.gebruiker.SchoolId = :schoolId " +
            "AND u.isIngeleverd = false AND u.status IN :statussen")
    long countActieveLeningenVoorSchoolId(@Param("schoolId") long schoolId,
                                          @Param("statussen") List<String> statussen);

    @Query("SELECT COUNT(u) FROM Uitlening u WHERE u.gebruiker.SchoolId = :schoolId " +
            "AND u.isIngeleverd = false AND u.status IN :statussen AND u.inleverDatum < :datum")
    long countTeLaatVoorSchoolId(@Param("schoolId") long schoolId,
                                 @Param("statussen") List<String> statussen,
                                 @Param("datum") LocalDate datum);

    @Query("SELECT COUNT(u) FROM Uitlening u WHERE u.gebruiker.SchoolId = :schoolId " +
            "AND u.status <> :status")
    long countLeengeschiedenisVoorSchoolId(@Param("schoolId") long schoolId,
                                           @Param("status") String status);

    @Query("SELECT COUNT(u) FROM Uitlening u WHERE u.uitleenDatum >= :sinds " +
            "AND u.status IN :statussen " +
            "AND (:klas IS NULL OR :klas = '' OR LOWER(u.gebruiker.klas) = LOWER(:klas))")
    long countLeningenSindsVoorKlas(@Param("klas") String klas,
                                    @Param("statussen") List<String> statussen,
                                    @Param("sinds") LocalDate sinds);

    @Query("SELECT COUNT(u) FROM Uitlening u WHERE u.isIngeleverd = false " +
            "AND u.status IN :statussen " +
            "AND (:klas IS NULL OR :klas = '' OR LOWER(u.gebruiker.klas) = LOWER(:klas))")
    long countActieveLeningenVoorKlas(@Param("klas") String klas,
                                      @Param("statussen") List<String> statussen);

    @Query("SELECT COUNT(u) FROM Uitlening u WHERE u.isIngeleverd = false " +
            "AND u.status IN :statussen AND u.inleverDatum < :datum " +
            "AND (:klas IS NULL OR :klas = '' OR LOWER(u.gebruiker.klas) = LOWER(:klas))")
    long countTeLaatVoorKlas(@Param("klas") String klas,
                             @Param("statussen") List<String> statussen,
                             @Param("datum") LocalDate datum);

    @Query("SELECT u.boek.titel FROM Uitlening u WHERE u.status IN :statussen " +
            "AND (:klas IS NULL OR :klas = '' OR LOWER(u.gebruiker.klas) = LOWER(:klas)) " +
            "GROUP BY u.boek.titel ORDER BY COUNT(u) DESC")
    List<String> findTopBoekTitelsVoorKlas(@Param("klas") String klas,
                                           @Param("statussen") List<String> statussen,
                                           Pageable pageable);


    // Get active loans of a user
    List<Uitlening> findByGebruiker_GebruikerIDAndIsIngeleverdFalse(Long gebruikerId);

    // Check if user has overdue loans
    boolean existsByGebruiker_GebruikerIDAndIsIngeleverdFalseAndInleverDatumBefore(Long gebruikerId, LocalDate vandaag);

    // Check if user already has this book
    boolean existsByGebruiker_GebruikerIDAndBoek_IdAndIsIngeleverdFalse(Long gebruikerId, Long boekId);

    // Fetch all requests by status
    List<Uitlening> findByStatus(String status);

    boolean existsByGebruiker_GebruikerIDAndStatus(Long gebruikerId, String status);

    List<Uitlening> findByGebruiker_GebruikerIDOrderByUitleenDatumDesc(Long gebruikerId);

    Optional<Uitlening> findFirstByGebruikerAndBoekAndIsIngeleverdFalseOrderByUitleenDatumAsc(Gebruiker gebruiker, Boek boek);

    List<Uitlening> findByIsIngeleverdFalseOrderByUitleenDatumDesc();
}