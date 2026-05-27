package ap.be.backend.repository;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import ap.be.backend.entity.Boek;
import ap.be.backend.entity.Uitlening;
import ap.be.backend.entity.User;

@Repository
public interface UitleningRepository extends JpaRepository<Uitlening, Long> {

    long countByUser_Id(Long userId);

    long countByIsIngeleverdFalseAndStatusIn(List<String> statussen);

    long countByIsIngeleverdFalseAndStatusInAndInleverDatumBefore(List<String> statussen, LocalDate datum);

    @Query("SELECT t.naam FROM Uitlening u JOIN u.boek b JOIN b.tags t " +
            "WHERE u.status IN :statussen AND t.type = ap.be.backend.entity.TagType.GENRE " +
            "GROUP BY t.naam ORDER BY COUNT(t) DESC")
    List<String> findPopulairsteGenres(@Param("statussen") List<String> statussen, Pageable pageable);

    @Query("SELECT COUNT(u) FROM Uitlening u WHERE u.uitleenDatum >= :sinds AND u.status IN :statussen")
    long countLeningenSinds(@Param("statussen") List<String> statussen, @Param("sinds") LocalDate sinds);

    @Query("SELECT COUNT(u) FROM Uitlening u WHERE u.user.id = :userId " +
            "AND u.isIngeleverd = false AND u.status IN :statussen")
    long countActieveLeningenVoorUser(@Param("userId") Long userId,
                                       @Param("statussen") List<String> statussen);

    @Query("SELECT COUNT(u) FROM Uitlening u WHERE u.user.id = :userId " +
            "AND u.isIngeleverd = false AND u.status IN :statussen AND u.inleverDatum < :datum")
    long countTeLaatVoorUser(@Param("userId") Long userId,
                              @Param("statussen") List<String> statussen,
                              @Param("datum") LocalDate datum);

    @Query("SELECT COUNT(u) FROM Uitlening u WHERE u.user.id = :userId " +
            "AND u.status <> :status")
    long countLeengeschiedenisVoorUser(@Param("userId") Long userId,
                                        @Param("status") String status);

    @Query("SELECT COUNT(u) FROM Uitlening u WHERE u.uitleenDatum >= :sinds " +
            "AND u.status IN :statussen " +
            "AND (:klas IS NULL OR :klas = '' OR LOWER(u.user.klas) = LOWER(:klas))")
    long countLeningenSindsVoorKlas(@Param("klas") String klas,
                                    @Param("statussen") List<String> statussen,
                                    @Param("sinds") LocalDate sinds);

    @Query("SELECT COUNT(u) FROM Uitlening u WHERE u.isIngeleverd = false " +
            "AND u.status IN :statussen " +
            "AND (:klas IS NULL OR :klas = '' OR LOWER(u.user.klas) = LOWER(:klas))")
    long countActieveLeningenVoorKlas(@Param("klas") String klas,
                                      @Param("statussen") List<String> statussen);

    @Query("SELECT COUNT(u) FROM Uitlening u WHERE u.isIngeleverd = false " +
            "AND u.status IN :statussen AND u.inleverDatum < :datum " +
            "AND (:klas IS NULL OR :klas = '' OR LOWER(u.user.klas) = LOWER(:klas))")
    long countTeLaatVoorKlas(@Param("klas") String klas,
                             @Param("statussen") List<String> statussen,
                             @Param("datum") LocalDate datum);

    @Query("SELECT u.boek.titel FROM Uitlening u WHERE u.status IN :statussen " +
            "AND (:klas IS NULL OR :klas = '' OR LOWER(u.user.klas) = LOWER(:klas)) " +
            "GROUP BY u.boek.titel ORDER BY COUNT(u) DESC")
    List<String> findTopBoekTitelsVoorKlas(@Param("klas") String klas,
                                           @Param("statussen") List<String> statussen,
                                           Pageable pageable);

    List<Uitlening> findByUser_IdAndIsIngeleverdFalse(Long userId);

    boolean existsByUser_IdAndIsIngeleverdFalseAndInleverDatumBefore(Long userId, LocalDate vandaag);

    boolean existsByUser_IdAndBoek_IdAndIsIngeleverdFalse(Long userId, Long boekId);

    List<Uitlening> findByStatus(String status);

    boolean existsByUser_IdAndStatus(Long userId, String status);

    List<Uitlening> findByUser_IdOrderByUitleenDatumDesc(Long userId);

    Optional<Uitlening> findFirstByUserAndBoekAndIsIngeleverdFalseOrderByUitleenDatumAsc(User user, Boek boek);

    List<Uitlening> findByIsIngeleverdFalseOrderByUitleenDatumDesc();

    @Query("SELECT COUNT(u) FROM Uitlening u WHERE u.user.id = :id AND u.isIngeleverd = true")
    long countOnTime(@Param("id") Long id);

    // Geeft alle open uitleningen voor een lijst van gebruikers — gebruikt bij rollover preview
    // om te controleren of vertrekkende leerlingen nog boeken hebben uitstaan.
    List<Uitlening> findByUser_IdInAndIsIngeleverdFalse(Collection<Long> userIds);

    // Te-laat-notificaties: nog niet ingeleverd, deadline verstreken, eerste mail nog niet verstuurd.
    @Query("SELECT u FROM Uitlening u WHERE u.isIngeleverd = false " +
            "AND u.inleverDatum < :vandaag AND u.eersteTeLaatMailOp IS NULL")
    List<Uitlening> findTeLaatZonderEersteMail(@Param("vandaag") LocalDate vandaag);

    // Herinneringen: eerste mail verstuurd >= 7 dagen geleden, nog niet ingeleverd, reminder nog niet verstuurd.
    @Query("SELECT u FROM Uitlening u WHERE u.isIngeleverd = false " +
            "AND u.eersteTeLaatMailOp IS NOT NULL AND u.eersteTeLaatMailOp <= :drempel " +
            "AND u.herinneringMailOp IS NULL")
    List<Uitlening> findKlaarVoorHerinnering(@Param("drempel") LocalDate drempel);
}
