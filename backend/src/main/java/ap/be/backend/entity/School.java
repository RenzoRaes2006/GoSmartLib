package ap.be.backend.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.HashSet;
import java.util.Set;

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

    // new fields for smartschool onboarding
    private String smartschoolDomein;
    private String oauthClientId;
    private String oauthClientSecret;

    // cascade ensures that campuses are saved automatically when a school is saved
    @OneToMany(mappedBy = "school", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Campus> campussen = new HashSet<>();
}