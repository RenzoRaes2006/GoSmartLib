package ap.be.backend.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "scholen")
@Data 
@NoArgsConstructor 
@AllArgsConstructor 
public class School {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String naam;

    private String adres;

    // the secret key to communicate with the smartschool api of this specific school
    @Column(name = "api_code")
    private String apiCode;
}
