package ap.be.backend.entity;

import java.time.LocalDateTime;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "gerapporteerde_beoordelingen")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class GerapporteerdeBeoordeling {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "beoordeling_id", nullable = false)
    private Beoordeling beoordeling;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User userMelder;

    @Column(nullable = false)
    private String reden;

    @Column(nullable = false)
    private LocalDateTime datumGemeld;
}