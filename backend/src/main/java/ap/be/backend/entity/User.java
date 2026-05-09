package ap.be.backend.entity;

import java.util.HashSet;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String smartschoolId;

    private String klas;

    @Enumerated(EnumType.STRING)
    private Rol rol;

    @JsonIgnore
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<LeeslijstMap> mappen = new HashSet<>();

    public User(String smartschoolId) {
        this.smartschoolId = smartschoolId;
    }

    public User(String smartschoolId, String klas, Rol rol) {
        this.smartschoolId = smartschoolId;
        this.klas = klas;
        this.rol = rol;
    }

    public void voegMapToe(LeeslijstMap map) {
        mappen.add(map);
        map.setUser(this);
    }
}
