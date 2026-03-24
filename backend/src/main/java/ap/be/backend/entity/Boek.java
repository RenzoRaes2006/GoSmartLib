package ap.be.backend.entity;

import java.util.List;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class Boek {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column
    private String titel;

    @Column
    private String auteur;

    @Column(unique = true)
    private String isbn;

    @Column
    private String uitgever;

    @Column
    private int pagina;

    @Column
    private String uitgaveJaar;

    @ElementCollection
    private List<String> genre;

    @Lob
    @Column(length = 100000)
    private String beschrijving;

    @Column(columnDefinition = "LONGTEXT")
    private String afbeelding;

    @Column
    private int aantalExemplaren;

    @Column
    private String locatie;

    @Column
    private Double populariteit;

    @Column
    private String leesniveau;

    @OneToMany(mappedBy = "boek", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Beoordeling> beoordelingen;

    @JsonIgnore
    @ManyToMany(cascade = { CascadeType.PERSIST, CascadeType.MERGE })
    @JoinTable(name = "leeslijst_map_boeken", inverseJoinColumns = @JoinColumn(name = "map_id"))
    private Set<LeeslijstMap> leeslijstMaps;

    public Boek(String titel, String auteur) {
        this.titel = titel;
        this.auteur = auteur;
    }
}
