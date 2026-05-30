package ap.be.backend.entity;

import java.util.HashSet;
import java.util.List;
import java.util.Set;


import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import org.hibernate.annotations.Formula;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "boeken")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
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

    @ManyToMany(cascade = { CascadeType.PERSIST, CascadeType.MERGE })
    @JoinTable(name = "boek_tags",
            joinColumns = @JoinColumn(name = "boek_id"),
            inverseJoinColumns = @JoinColumn(name = "tag_id"))
    private Set<Tag> tags = new HashSet<>();

    @Lob
    @Column(length = 100000)
    private String beschrijving;

    @Column(columnDefinition = "LONGTEXT")
    private String afbeelding;

    /**
     * Aantal momenteel uitleenbare exemplaren (status BESCHIKBAAR). Read-only,
     * door Hibernate per query berekend met een subselect — geen aparte teller
     * meer die kan ontsporen. Vervangt het oude {@code aantalExemplaren}-veld en
     * voedt het gelijknamige veld in {@link ap.be.backend.dto.BoekDTO}, zodat het
     * frontend-contract ongewijzigd blijft.
     */
    @JsonIgnore
    @Formula("(SELECT COUNT(e.id) FROM exemplaren e WHERE e.boek_id = id AND e.status = 'BESCHIKBAAR')")
    private int aantalBeschikbaar;

    /** Aantal exemplaren dat momenteel BESCHADIGD is. Read-only, per query berekend. */
    @JsonIgnore
    @Formula("(SELECT COUNT(e.id) FROM exemplaren e WHERE e.boek_id = id AND e.status = 'BESCHADIGD')")
    private int aantalBeschadigd;

    @Column
    private String locatie;

    @Column
    private Double populariteit;

    @Column
    private String leesniveau;

    @Column(nullable = false, columnDefinition = "boolean default false")
    private boolean didactischMateriaal;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "campus_id")
    private Campus campus;

    @OneToMany(mappedBy = "boek", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Beoordeling> beoordelingen;

    @JsonIgnore
    @OneToMany(mappedBy = "boek", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Exemplaar> exemplaren = new java.util.ArrayList<>();

    @JsonIgnore
    @ManyToMany(cascade = { CascadeType.PERSIST, CascadeType.MERGE })
    @JoinTable(name = "leeslijst_map_boeken", inverseJoinColumns = @JoinColumn(name = "map_id"))
    private Set<LeeslijstMap> leeslijstMaps;

    @Transient
    private boolean bestaatAl;

    @Transient
    private Long bulkCampusId;

    /**
     * Tijdelijk veld voor de import-/preview-pijp: hoeveel exemplaren de Excel-rij
     * vraagt. Wordt nooit gepersisteerd; na save zet de importer dit om naar echte
     * {@link Exemplaar}-rijen. Serialiseert als "aantalExemplaren" zodat het
     * import-frontend (dat dat veld bindt en terugstuurt) ongewijzigd blijft.
     */
    @Transient
    @JsonProperty("aantalExemplaren")
    private int importAantal;

    public Boek(String titel, String auteur) {
        this.titel = titel;
        this.auteur = auteur;
    }
}

