package ap.be.backend.entity;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "reviews")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Beoordeling {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "is_anonymous", nullable = false)
    private boolean isAnoniem;

    @ManyToOne
    @JoinColumn(name = "book_id", nullable = false)
    @JsonIgnore
    private Boek boek;

    @JsonIgnore
    @OneToMany(mappedBy = "beoordeling", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<GerapporteerdeBeoordeling> gerapporteerdeBeoordelingen;

    @Column(name = "rating", nullable = false)
    private int waardering;

    @Column(name = "content", length = 1000)
    private String inhoud;

    @Column(name = "is_spoiler", nullable = false)
    private boolean isSpoiler;
}
