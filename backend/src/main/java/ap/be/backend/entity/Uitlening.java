package ap.be.backend.entity;

import java.time.LocalDate;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
public class Uitlening {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "gebruiker_id", nullable = false)
    private Gebruiker gebruiker;
    @ManyToOne
    @JoinColumn(name = "boek_id", nullable = false)
    private Boek boek;

    private LocalDate uitleenDatum;
    private LocalDate inleverDatum;

    private boolean isIngeleverd;

    private String status;

    private boolean isVerlengd;

    public Uitlening() {
    }

    public Uitlening(Gebruiker gebruiker, Boek boek, LocalDate uitleenDatum, LocalDate inleverDatum) {
        this.gebruiker = gebruiker;
        this.boek = boek;
        this.status = "AANGEVRAAGD";
        this.uitleenDatum = null;
        this.inleverDatum = null;
        this.isIngeleverd = false;
        this.isVerlengd = false;
    }
}
