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

    // relaties voor later, we hebben geen user en book entities
    // @ManyToOne
    // @JoinColumn(name = "user_id", nullable = false)
    @Column(name = "user_id", nullable = false)
    long gebruikerId;

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

    public Beoordeling(long gebruikerId, Boek boek, int waardering, String content) {
        this.gebruikerId = gebruikerId;
        this.boek = boek;
        this.waardering = waardering;
        this.inhoud = content;
    }

    public Long getId() {
        return id;
    }

    public Long getGebruikerId() {
        return gebruikerId;
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

    public void setGebruikerId(long gebruikerId) {
        this.gebruikerId = gebruikerId;
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

    @Override
    public String toString() {
        return "Review{" +
                "id=" + id +
                ", userId=" + gebruikerId +
                ", boek=" + boek +
                ", waardering=" + waardering +
                ", content='" + inhoud + '\'' +
                '}';
    }
}