package ap.be.backend.dto;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

public class ScanBatchResponseDTO {
    @JsonProperty("actie")
    private String actie;
    @JsonProperty("resultaten")
    private List<ScanBatchItemResultDTO> resultaten;

    public ScanBatchResponseDTO() {
    }

    public ScanBatchResponseDTO(String actie, List<ScanBatchItemResultDTO> resultaten) {
        this.actie = actie;
        this.resultaten = resultaten;
    }

    public String getActie() {
        return actie;
    }

    public void setActie(String actie) {
        this.actie = actie;
    }

    public List<ScanBatchItemResultDTO> getResultaten() {
        return resultaten;
    }

    public void setResultaten(List<ScanBatchItemResultDTO> resultaten) {
        this.resultaten = resultaten;
    }
}
