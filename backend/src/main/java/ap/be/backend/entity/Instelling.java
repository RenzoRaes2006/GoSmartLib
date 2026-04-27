package ap.be.backend.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class Instelling {
    @Id
    private String sleutel;

    private String waarde;

    public Instelling(String sleutel, String waarde) {
        this.sleutel = sleutel;
        this.waarde = waarde;
    }
}
