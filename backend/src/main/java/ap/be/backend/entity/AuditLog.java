package ap.be.backend.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Onveranderlijke audit-trail van lifecycle-acties. Wordt geschreven binnen
 * dezelfde transactie als de actie zelf, zodat een rollback ook de auditrij
 * terugdraait — anders zou de log liegen over wat er echt is gebeurd.
 *
 * actorGebruikerId is een gewone Long (geen FK) zodat een vertrokken of
 * verwijderde admin de log niet stuk maakt. Bij OneRoster-sync is er geen
 * ingelogde actor en wordt {@code actorGebruikerId} null gelaten + actie-suffix
 * "_SYNC" gebruikt zodat filteren op systeem-acties triviaal blijft.
 */
@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "audit_log", indexes = {
        @Index(name = "idx_audit_tijdstip", columnList = "tijdstip"),
        @Index(name = "idx_audit_entiteit", columnList = "entiteit_type,entiteit_id"),
        @Index(name = "idx_audit_actie", columnList = "actie")
})
public class AuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, updatable = false)
    private LocalDateTime tijdstip;

    @Column(name = "actor_gebruiker_id")
    private Long actorGebruikerId;

    @Column(name = "actor_smartschool_id", length = 64)
    private String actorSmartschoolId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 64)
    private AuditActie actie;

    @Column(name = "entiteit_type", nullable = false, length = 64)
    private String entiteitType;

    @Column(name = "entiteit_id")
    private Long entiteitId;

    @Column(name = "details_json", columnDefinition = "TEXT")
    private String detailsJson;

    @PrePersist
    void prePersist() {
        if (tijdstip == null) {
            tijdstip = LocalDateTime.now();
        }
    }
}
