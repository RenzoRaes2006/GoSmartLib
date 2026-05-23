package ap.be.backend.entity;

import java.util.HashSet;
import java.util.List;
import java.util.Set;


import com.fasterxml.jackson.annotation.JsonIgnore;

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

    @Column
    private int aantalExemplaren;

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
    private List<Uitlening> uitleningen;

    @JsonIgnore
    @ManyToMany(cascade = { CascadeType.PERSIST, CascadeType.MERGE })
    @JoinTable(name = "leeslijst_map_boeken", inverseJoinColumns = @JoinColumn(name = "map_id"))
    private Set<LeeslijstMap> leeslijstMaps;

    @Transient
    private boolean bestaatAl;

    @Transient
    private Long bulkCampusId;

    public Boek(String titel, String auteur) {
        this.titel = titel;
        this.auteur = auteur;
    }
}

