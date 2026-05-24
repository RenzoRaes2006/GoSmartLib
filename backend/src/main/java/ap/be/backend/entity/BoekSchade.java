package ap.be.backend.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Schaderapport voor een ingeleverd boek. Gekoppeld aan de uitlening, zodat
 * boek + leerling (smartschoolId) afleidbaar zijn zonder PII te dupliceren —
 * namen worden niet opgeslagen; UI haalt die live op via OneRoster.
 */
@Entity
@Table(name = "boek_schade")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class BoekSchade {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "uitlening_id", nullable = false)
    private Uitlening uitlening;

    @Column(length = 1000)
    private String opmerking;

    @Column(nullable = false)
    private LocalDateTime gemeldOp;
}
