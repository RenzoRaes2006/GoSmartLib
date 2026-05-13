package ap.be.backend.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "admin_gebruikers")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AdminGebruiker {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String gebruikersnaam;

    @Column(nullable = false)
    private String wachtwoordHash;

    private String naam;

    public AdminGebruiker(String gebruikersnaam, String wachtwoordHash, String naam) {
        this.gebruikersnaam = gebruikersnaam;
        this.wachtwoordHash = wachtwoordHash;
        this.naam = naam;
    }
}
