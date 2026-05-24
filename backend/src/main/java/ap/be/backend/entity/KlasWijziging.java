package ap.be.backend.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Audit-rij voor elke klaswijziging van een leerling. Wordt zowel geschreven
 * bij handmatige admin-wijziging als bij OneRoster-sync. {@code doorWie} is
 * null voor sync-events (dan is {@code bron} = "ONEROSTER_SYNC").
 */
@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "klas_wijzigingen")
public class KlasWijziging {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "leerling_id", nullable = false)
    private User leerling;

    @Column(name = "van_klas")
    private String vanKlas;

    @Column(name = "naar_klas")
    private String naarKlas;

    @Column(name = "gewijzigd_op", nullable = false, updatable = false)
    private LocalDateTime gewijzigdOp;

    @ManyToOne
    @JoinColumn(name = "door_wie_id")
    private User doorWie;

    @Column(nullable = false, length = 32)
    private String bron;

    @PrePersist
    void prePersist() {
        if (gewijzigdOp == null) {
            gewijzigdOp = LocalDateTime.now();
        }
        if (bron == null) {
            bron = "HANDMATIG";
        }
    }
}
