package ap.be.backend.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public class ScanRequestDTO {
    @JsonProperty("gebruikerId")
    private long gebruikerId;
    @JsonProperty("isbn")
    private String isbn;

    public long getGebruikerId() {
        return gebruikerId;
    }

    public void setGebruikerId(long gebruikerId) {
        this.gebruikerId = gebruikerId;
    }

    public String getIsbn() {
        return isbn;
    }

    public void setIsbn(String isbn) {
        this.isbn = isbn;
    }
}
