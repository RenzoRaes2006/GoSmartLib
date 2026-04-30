package ap.be.backend.entity;

import java.util.HashSet;
import java.util.Set;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;

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
    
    // added missing property for pagination and seeder
    private String klas;

    @Enumerated(EnumType.STRING)
    private Rol Rol;

    @OneToMany(mappedBy = "gebruiker", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    private Set<LeeslijstMap> mappen = new HashSet<>();

    // default constructor required by jpa
    public Gebruiker() {
    }

    // constructor for the database seeder
    public Gebruiker(String voorNaam, String achternaam, String klas) {
        this.VoorNaam = voorNaam;
        this.Achternaam = achternaam;
        this.klas = klas;
    }

    // --- getters and setters ---
    public Set<LeeslijstMap> getMappen() { return mappen; }
    public void setMappen(Set<LeeslijstMap> mappen) { this.mappen = mappen; }
    public void voegMapToe(LeeslijstMap map) {
        mappen.add(map);
        map.setGebruiker(this);
    }

    public Long getGebruikerID() { return gebruikerID; }
    public void setGebruikerID(Long userId) { this.gebruikerID = userId; }

    public String getVoorNaam() { return VoorNaam; }
    public void setVoorNaam(String voorNaam) { this.VoorNaam = voorNaam; }

    public String getAchternaam() { return Achternaam; }
    public void setAchternaam(String achterNaam) { this.Achternaam = achterNaam; }

    public String getEmail() { return Email; }
    public void setEmail(String email) { this.Email = email; }

    public String getKlas() { return klas; }
    public void setKlas(String klas) { this.klas = klas; }

    public Rol getRol() { return Rol; }
    public void setRol(Rol role) { this.Rol = role; }

    public long getSchoolId() { return SchoolId; }
    public void setSchoolId(long schoolId) { this.SchoolId = schoolId; }

    public long getId() { return gebruikerID; }
    public void setId(long id) { this.gebruikerID = id; }
}