package ap.be.backend.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "scholen")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class School {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String naam;

    private String adres;

    @Column(name = "api_code")
    private String apiCode;
}
