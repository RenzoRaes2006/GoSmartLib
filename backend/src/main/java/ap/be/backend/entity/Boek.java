package ap.be.backend.entity;

import java.util.List;

import jakarta.persistence.*;

@Entity
public class Boek {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column
    private String titel;

    @Column
    private String auteur;

    @Column
    private String isbn;

    @Column
    private String uitgever;

    @Column
    private int pagina;

    @Column
    private String uitgaveJaar;

    @ElementCollection
    private List<String> genre;

    @Column(columnDefinition = "TEXT")
    private String beschrijving;

    @Column
    private String afbeelding;

    @Column
    private int aantalExemplaren;

    @Column
    private String locatie;

    public Boek() {

    }

    public Boek(String titel, String auteur) {
        this.titel = titel;
        this.auteur = auteur;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getTitel() {
        return titel;
    }

    public void setTitel(String titel) {
        this.titel = titel;
    }

    public String getAuteur() {
        return auteur;
    }

    public void setAuteur(String auteur) {
        this.auteur = auteur;
    }

    public String getIsbn() {
        return isbn;
    }

    public void setIsbn(String isbn) {
        this.isbn = isbn;
    }

    public String getUitgever() {
        return uitgever;
    }

    public void setUitgever(String uitgever) {
        this.uitgever = uitgever;
    }

    public int getPagina() {
        return pagina;
    }

    public void setPagina(int pagina) {
        this.pagina = pagina;
    }

    public String getUitgaveJaar() {
        return uitgaveJaar;
    }

    public void setUitgaveJaar(String uitgaveJaar) {
        this.uitgaveJaar = uitgaveJaar;
    }

    public List<String> getGenre() {
        return genre;
    }

    public void setGenre(List<String> genre) {
        this.genre = genre;
    }

    public String getBeschrijving() {
        return beschrijving;
    }

    public void setBeschrijving(String beschrijving) {
        this.beschrijving = beschrijving;
    }

    public String getAfbeelding() {
        return afbeelding;
    }

    public void setAfbeelding(String afbeelding) {
        this.afbeelding = afbeelding;
    }

    public int getAantalExemplaren() {
        return aantalExemplaren;
    }

    public void setAantalExemplaren(int aantalExemplaren) {
        this.aantalExemplaren = aantalExemplaren;
    }

    public String getLocatie() {
        return locatie;
    }

    public void setLocatie(String locatie) {
        this.locatie = locatie;
    }
}
