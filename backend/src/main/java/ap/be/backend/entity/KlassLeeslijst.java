package ap.be.backend.entity;

import java.util.HashSet;
import java.util.Set;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "leeslijsten")
public class KlassLeeslijst {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String naam;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "leerkracht_id", nullable = false)
    private Gebruiker leerkracht;

    @ManyToMany
    @JoinTable(name = "leeslijst_boeken", joinColumns = @JoinColumn(name = "leeslijst_id"), inverseJoinColumns = @JoinColumn(name = "boek_id"))
    private Set<Boek> boeken = new HashSet<>();
}