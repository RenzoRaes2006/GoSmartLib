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
    @JoinColumn(name = "melder_id", nullable = false)
    private Gebruiker gebruikerMelder;

    @Column(nullable = false)
    private String reden;

    @Column(nullable = false)
    private LocalDateTime datumGemeld;
}