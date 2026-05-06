package ap.be.backend.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "admin_gebruikers")
public class AdminGebruiker {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String gebruikersnaam;

    @Column(nullable = false)
    private String wachtwoordHash;

    private String naam;

    public AdminGebruiker() {}

    public AdminGebruiker(String gebruikersnaam, String wachtwoordHash, String naam) {
        this.gebruikersnaam = gebruikersnaam;
        this.wachtwoordHash = wachtwoordHash;
        this.naam = naam;
    }

    public Long getId() { return id; }

    public String getGebruikersnaam() { return gebruikersnaam; }
    public void setGebruikersnaam(String gebruikersnaam) { this.gebruikersnaam = gebruikersnaam; }

    public String getWachtwoordHash() { return wachtwoordHash; }
    public void setWachtwoordHash(String wachtwoordHash) { this.wachtwoordHash = wachtwoordHash; }

    public String getNaam() { return naam; }
    public void setNaam(String naam) { this.naam = naam; }
}
