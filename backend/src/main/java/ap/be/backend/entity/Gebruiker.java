package ap.be.backend.entity;

import java.util.HashSet;
import java.util.Set;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;

enum Rol {
    LEERLING, LEERKRACHT, BEHEERDER
}

@Entity
@Table(name = "Gebruikers")
public class Gebruiker {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long gebruikerID;

    private long SchoolId;
    private String VoorNaam;
    private String Achternaam;
    private String Email;

    @Enumerated(EnumType.STRING) // Slaat "LEERLING" op ipv 0
    private Rol Rol;

    @ManyToMany
    @JoinTable(name = "gebruiker_leeslijst", joinColumns = @JoinColumn(name = "gebruikerId"), inverseJoinColumns = @JoinColumn(name = "boekId"))

    private Set<Boek> leeslijst = new HashSet<>();

    public Set<Boek> getLeeslijst() {
        return leeslijst;
    }

    public void setLeeslijst(Set<Boek> leeslijst) {
        this.leeslijst = leeslijst;
    }

    public Gebruiker() {
    }

    // Gebruik camelCase (kleine letters aan het begin)
    public Long getGebruikerID() {
        return gebruikerID;
    }

    public void setGebruikerID(Long userId) {
        this.gebruikerID = userId;
    }

    public String getVoorNaam() {
        return VoorNaam;
    }

    public void setVoorNaam(String voorNaam) {
        this.VoorNaam = voorNaam;
    }

    public String getAchternaam() {
        return Achternaam;
    }

    public void setAchternaam(String achterNaam) {
        this.Achternaam = achterNaam;
    }

    public String getEmail() {
        return Email;
    }

    public void setEmail(String email) {
        this.Email = email;
    }

    public Rol getRol() {
        return Rol;
    }

    public void setRol(Rol role) {
        this.Rol = role;
    }
}
