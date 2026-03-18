package ap.be.backend.entity;

import java.time.LocalDateTime;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


@Entity
@Table(name = "reported_reviews")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class GerapporteerdeBeoordeling {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "review_id", nullable = false)
    private Beoordeling beoordeling;

    @ManyToOne
    @JoinColumn(name = "gebruiker_id", nullable = false)
    private Gebruiker gebruiker;

    @Column()
    private String reden;

    @Column()
    private LocalDateTime datumGemeld = LocalDateTime.now();
}
