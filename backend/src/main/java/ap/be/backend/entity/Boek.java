package ap.be.backend.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "boeken")
public class Boek {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "titel", nullable = false)
    private String titel;

    @Column(name = "auteur", nullable = false)
    private String auteur;

    @Column(name = "isbn", nullable = false, unique = true)
    private String isbn;

    @Column(name = "uitgever", nullable = false)
    private String uitgever;

    @Column(name = "publicatiejaar", nullable = false)
    private int publicatieJaar;

    @Column(name = "aantal_paginas", nullable = false)
    private int aantalPaginas;

    @Column(name = "genre", nullable = false)
    private String genre;

    @Column(name = "beschrijving", nullable = false, length = 2000)
    private String beschrijving;

    @Column(name = "aantal_exemplaren", nullable = false)
    private int aantalExemplaren;

    @Column(name = "locatie", nullable = false)
    private String locatie;

    @Column(name = "cover_url")
    private String coverUrl;

    // Constructor
    public Boek() {
    }

    // Getters en setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
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

    public int getPublicatieJaar() {
        return publicatieJaar;
    }

    public void setPublicatieJaar(int publicatieJaar) {
        this.publicatieJaar = publicatieJaar;
    }

    public int getAantalPaginas() {
        return aantalPaginas;
    }

    public void setAantalPaginas(int aantalPaginas) {
        this.aantalPaginas = aantalPaginas;
    }

    public String getGenre() {
        return genre;
    }

    public void setGenre(String genre) {
        this.genre = genre;
    }

    public String getBeschrijving() {
        return beschrijving;
    }

    public void setBeschrijving(String beschrijving) {
        this.beschrijving = beschrijving;
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

    public String getCoverUrl() {
        return coverUrl;
    }

    public void setCoverUrl(String coverUrl) {
        this.coverUrl = coverUrl;
    }

}
