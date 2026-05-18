package ap.be.backend.entity;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "klassleeslijsten")
public class KlassLeeslijst {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String naam;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "leerkracht_id", nullable = false)
    private User leerkracht;

    @Column(name = "klas")
    private String klas;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "leerling_id")
    private User leerling;

    @Column(length = 500)
    private String beschrijving;

    @Column(nullable = false)
    private boolean zichtbaar = false;

    @ManyToMany
    @JoinTable(name = "klassleeslijst_boeken", joinColumns = @JoinColumn(name = "klassleeslijst_id"), inverseJoinColumns = @JoinColumn(name = "boek_id"))
    private Set<Boek> boeken = new HashSet<>();

    @Column(name = "aangemaakt_op", updatable = false)
    private LocalDateTime aangemaaktOp;

    @PrePersist
    void prePersist() {
        if (aangemaaktOp == null) {
            aangemaaktOp = LocalDateTime.now();
        }
    }
}
