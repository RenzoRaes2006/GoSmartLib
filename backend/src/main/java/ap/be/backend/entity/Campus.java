package ap.be.backend.entity;

import jakarta.persistence.*;
import lombok.*;
import com.fasterxml.jackson.annotation.JsonIgnore;

@Entity
@Table(name = "campussen")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Campus {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String naam;

    // links campus back to the school
    @ManyToOne
    @JoinColumn(name = "school_id", nullable = false)
    @JsonIgnore
    private School school;
}