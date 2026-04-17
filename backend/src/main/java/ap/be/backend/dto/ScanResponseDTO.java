package ap.be.backend.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import ap.be.backend.entity.Boek;
import ap.be.backend.entity.Uitlening;

public class ScanResponseDTO {
    @JsonProperty("actie")
    private String actie;
    @JsonProperty("uitlening")
    private Uitlening uitlening;
    @JsonProperty("boek")
    private Boek boek;

    public ScanResponseDTO() {
    }

    public ScanResponseDTO(String actie, Uitlening uitlening, Boek boek) {
        this.actie = actie;
        this.uitlening = uitlening;
        this.boek = boek;
    }

    public String getActie() {
        return actie;
    }

    public void setActie(String actie) {
        this.actie = actie;
    }

    public Uitlening getUitlening() {
        return uitlening;
    }

    public void setUitlening(Uitlening uitlening) {
        this.uitlening = uitlening;
    }

    public Boek getBoek() {
        return boek;
    }

    public void setBoek(Boek boek) {
        this.boek = boek;
    }
}
