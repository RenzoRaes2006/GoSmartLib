package ap.be.backend.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import ap.be.backend.entity.Boek;
import ap.be.backend.entity.Exemplaar;
import ap.be.backend.entity.ExemplaarStatus;
import jakarta.persistence.LockModeType;

@Repository
public interface ExemplaarRepository extends JpaRepository<Exemplaar, Long> {

    /**
     * Reserveert concurrency-veilig het eerste vrije exemplaar van een boek. De
     * pessimistic write-lock zorgt dat twee gelijktijdige scans nooit hetzelfde
     * exemplaar krijgen — de tweede transactie wacht tot de eerste commit.
     * Moet binnen een actieve transactie aangeroepen worden.
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT e FROM Exemplaar e WHERE e.boek = :boek AND e.status = :status ORDER BY e.volgnr ASC")
    List<Exemplaar> findBeschikbareVoorBoek(@Param("boek") Boek boek,
                                            @Param("status") ExemplaarStatus status);

    long countByBoekAndStatus(Boek boek, ExemplaarStatus status);

    long countByBoek(Boek boek);

    List<Exemplaar> findByBoekAndStatusOrderByVolgnrDesc(Boek boek, ExemplaarStatus status);

    boolean existsByBoek(Boek boek);

    @Query("SELECT COALESCE(MAX(e.volgnr), 0) FROM Exemplaar e WHERE e.boek = :boek")
    int maxVolgnrVoorBoek(@Param("boek") Boek boek);

    Optional<Exemplaar> findFirstByBoekOrderByVolgnrAsc(Boek boek);
}
