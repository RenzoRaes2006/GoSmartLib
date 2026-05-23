package ap.be.backend.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "beschikbare_scholen")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class BeschikbareSchool {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String naam;

    @Column(nullable = false)
    private String adres;

    public BeschikbareSchool(String naam, String adres) {
        this.naam = naam;
        this.adres = adres;
    }
}
