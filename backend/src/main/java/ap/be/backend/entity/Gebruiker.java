package ap.be.backend.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

enum Rol {
    LEERLING, LEERKRACHT, BEHEERDER
}

@Entity
@Table(name = "Gebruikers")
public class Gebruiker {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long userId;

    private long SchoolId;
    private String VoorNaam;
    private String Achternaam;
    private String Email;

    @Enumerated(EnumType.STRING) // Slaat "LEERLING" op ipv 0
    private Rol Rol;

    // Zodra Book er is, haal je dit uit commentaar:
    /*
     * @ManyToMany
     * 
     * @JoinTable(
     * name = "user_reading_list",
     * joinColumns = @JoinColumn(name = "user_id"),
     * inverseJoinColumns = @JoinColumn(name = "book_id")
     * )
     * private Set<Book> readingList = new HashSet<>();
     */

    public Gebruiker() {
    }

    // Gebruik camelCase (kleine letters aan het begin)
    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
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
