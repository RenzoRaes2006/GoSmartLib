package ap.be.backend.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

/**
 * Stelt een schooljaar voor, zoals "2025-2026".
 *
 * Slechts één schooljaar is tegelijk actief. Bij een rollover wordt het huidige
 * schooljaar op actief=false gezet en wordt een nieuw schooljaar aangemaakt.
 * Oude schooljaren blijven bewaard als historiek.
 */
@Entity
@Table(name = "schooljaren")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Schooljaar {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Leesbare naam, bijv. "2025-2026"
    @Column(nullable = false, unique = true)
    private String naam;

    @Column(nullable = false)
    private LocalDate startDatum;

    @Column(nullable = false)
    private LocalDate eindDatum;

    // Slechts één rij mag actief=true hebben — bewaard via applicatielogica in SchooljaarService
    @Column(nullable = false)
    private boolean actief = false;

    public Schooljaar(String naam, LocalDate startDatum, LocalDate eindDatum, boolean actief) {
        this.naam = naam;
        this.startDatum = startDatum;
        this.eindDatum = eindDatum;
        this.actief = actief;
    }
}
