package ap.be.backend.entity;

import java.time.LocalDate;

import jakarta.persistence.Entity;
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

@Entity
@Table(name = "uitleningen")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Uitlening {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne
    @JoinColumn(name = "boek_id", nullable = false)
    private Boek boek;

    private LocalDate uitleenDatum;
    private LocalDate inleverDatum;
    private boolean isIngeleverd;
    private String status;
    private boolean isVerlengd;

    // Datum waarop de eerste "te laat"-notificatie naar de Smartschool-inbox is verstuurd.
    // Null = nog niet verstuurd. Gebruikt om dubbele verzending te vermijden door de daily scheduler.
    private LocalDate eersteTeLaatMailOp;

    // Datum waarop de herinnering (7 dagen na de eerste mail) is verstuurd. Null = nog niet.
    private LocalDate herinneringMailOp;

    public Uitlening(User user, Boek boek, LocalDate uitleenDatum, LocalDate inleverDatum) {
        this.user = user;
        this.boek = boek;
        this.status = "AANGEVRAAGD";
        this.uitleenDatum = null;
        this.inleverDatum = null;
        this.isIngeleverd = false;
        this.isVerlengd = false;
    }
}
