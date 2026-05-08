package ap.be.backend.entity;

import java.util.HashSet;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "Gebruikers")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Gebruiker {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long gebruikerID;

    private long SchoolId;
    private String VoorNaam;
    private String Achternaam;
    private String Email;
    private String klas;

    @Enumerated(EnumType.STRING)
    private Rol Rol;

    @OneToMany(mappedBy = "gebruiker", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    private Set<LeeslijstMap> mappen = new HashSet<>();

    // constructor for the database seeder
    public Gebruiker(String voorNaam, String achternaam, String klas) {
        this.VoorNaam = voorNaam;
        this.Achternaam = achternaam;
        this.klas = klas;
    }

    public void voegMapToe(LeeslijstMap map) {
        mappen.add(map);
        map.setGebruiker(this);
    }

    public long getId() {
        return gebruikerID;
    }

    public void setId(long id) {
        this.gebruikerID = id;
    }
}
