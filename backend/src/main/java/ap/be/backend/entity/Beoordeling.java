package ap.be.backend.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "reviews")
public class Beoordeling {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    Gebruiker gebruiker;

    @Column(name = "is_anonymous", nullable = false)
    private boolean isAnoniem;

    @ManyToOne
    @JoinColumn(name = "book_id", nullable = false)
    @JsonIgnore
    private Boek boek;

    @Column(name = "rating", nullable = false)
    int waardering;

    @Column(name = "content", length = 1000)
    String inhoud;

    public Beoordeling() {
    }

    public Beoordeling(Gebruiker gebruiker, Boek boek, int waardering, String content) {
        this.gebruiker = gebruiker;
        this.boek = boek;
        this.waardering = waardering;
        this.inhoud = content;
    }

    public Long getId() {
        return id;
    }

    public Gebruiker getGebruiker() {
        return gebruiker;
    }

    public Boek getBoek() {
        return boek;
    }

    public int getWaardering() {
        return waardering;
    }

    public String getInhoud() {
        return inhoud;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setGebruiker(Gebruiker gebruiker) {
        this.gebruiker = gebruiker;
    }

    public void setBoek(Boek boek) {
        this.boek = boek;
    }

    public void setWaardering(int waardering) {
        this.waardering = waardering;
    }

    public void setInhoud(String content) {
        this.inhoud = content;
    }

    public boolean isAnoniem() {
        return isAnoniem;
    }

    public void setAnoniem(boolean isAnoniem) {
        this.isAnoniem = isAnoniem;
    }



    @Override
    public String toString() {
        return "Review{" +
                "id=" + id +
                ", userId=" + gebruiker.getId() +
                ", isAnonymous=" + isAnoniem +
                ", boek=" + boek +
                ", waardering=" + waardering +
                ", content='" + inhoud + '\'' +
                '}';
    }
}