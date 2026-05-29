package ap.be.backend.entity;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Eén fysiek exemplaar van een {@link Boek}. Maakt elk fysiek boek individueel
 * traceerbaar: schade hoort bij een concreet exemplaar en de status (beschikbaar /
 * uitgeleend / beschadigd) wordt per exemplaar bijgehouden i.p.v. één teller op het boek.
 */
@Entity
@Table(name = "exemplaren",
        uniqueConstraints = @UniqueConstraint(name = "uk_exemplaar_boek_volgnr", columnNames = { "boek_id", "volgnr" }))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Exemplaar {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "boek_id", nullable = false)
    private Boek boek;

    /** Leesbaar volgnummer 1..N binnen het boek, voor weergave in de admin. */
    @Column(nullable = false)
    private int volgnr;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ExemplaarStatus status = ExemplaarStatus.BESCHIKBAAR;

    @JsonIgnore
    @OneToMany(mappedBy = "exemplaar", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Uitlening> uitleningen;

    public Exemplaar(Boek boek, int volgnr, ExemplaarStatus status) {
        this.boek = boek;
        this.volgnr = volgnr;
        this.status = status;
    }
}
