package ap.be.backend.dto;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

public class ScanBatchRequestDTO {
    @JsonProperty("gebruikerId")
    private long gebruikerId;
    @JsonProperty("actie")
    private String actie;
    @JsonProperty("isbns")
    private List<String> isbns;

    public long getGebruikerId() {
        return gebruikerId;
    }

    public void setGebruikerId(long gebruikerId) {
        this.gebruikerId = gebruikerId;
    }

    public String getActie() {
        return actie;
    }

    public void setActie(String actie) {
        this.actie = actie;
    }

    public List<String> getIsbns() {
        return isbns;
    }

    public void setIsbns(List<String> isbns) {
        this.isbns = isbns;
    }
}
