package ap.be.backend.repository;

import java.time.LocalDateTime;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import ap.be.backend.entity.AuditActie;
import ap.be.backend.entity.AuditLog;

@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {

    /**
     * Filter-query met optionele parameters; null = wildcard. Gesorteerd via
     * Pageable (controller geeft tijdstip-DESC mee).
     */
    @Query("""
            SELECT a FROM AuditLog a
            WHERE (:actie IS NULL OR a.actie = :actie)
              AND (:entiteitType IS NULL OR a.entiteitType = :entiteitType)
              AND (:actorId IS NULL OR a.actorGebruikerId = :actorId)
              AND (:vanaf IS NULL OR a.tijdstip >= :vanaf)
              AND (:tot IS NULL OR a.tijdstip < :tot)
            """)
    Page<AuditLog> zoek(@Param("actie") AuditActie actie,
                        @Param("entiteitType") String entiteitType,
                        @Param("actorId") Long actorId,
                        @Param("vanaf") LocalDateTime vanaf,
                        @Param("tot") LocalDateTime tot,
                        Pageable pageable);
}
