package ap.be.backend.entity;

import java.util.List;

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

    @Column(columnDefinition = "LONGTEXT")
    private String beschrijving;

    @Column(columnDefinition = "LONGTEXT")
    private String afbeelding;

    @Column
    private int aantalExemplaren;

    @Column
    private String locatie;

    public Boek(String titel, String auteur) {
        this.titel = titel;
        this.auteur = auteur;
    }
}
