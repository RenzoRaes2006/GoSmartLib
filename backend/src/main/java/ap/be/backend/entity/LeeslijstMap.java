package ap.be.backend.entity;

import java.util.HashSet;
import java.util.Set;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "LeeslijstMap")
public class LeeslijstMap {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String naam;

    @Column(length = 10)
    private String emoji = "📚";

    @ManyToOne
    @JoinColumn(name = "gebruiker_id", nullable = false)
    private Gebruiker gebruiker;

    @ManyToMany
    @JoinTable(name = "leeslijst_map_boeken",
            joinColumns = @JoinColumn(name = "map_id"),
            inverseJoinColumns = @JoinColumn(name = "boek_id"))
    private Set<Boek> boeken = new HashSet<>();

    public LeeslijstMap(String naam, Gebruiker gebruiker) {
        this.naam = naam;
        this.gebruiker = gebruiker;
    }

    public void voegBoekToe(Boek boek) {
        this.boeken.add(boek);
    }
}
