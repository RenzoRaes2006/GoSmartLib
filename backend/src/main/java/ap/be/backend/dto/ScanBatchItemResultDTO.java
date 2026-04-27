package ap.be.backend.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public class ScanBatchItemResultDTO {
    @JsonProperty("isbn")
    private String isbn;
    @JsonProperty("titel")
    private String titel;
    @JsonProperty("status")
    private String status;
    @JsonProperty("foutReden")
    private String foutReden;

    public ScanBatchItemResultDTO() {
    }

    public ScanBatchItemResultDTO(String isbn, String titel, String status, String foutReden) {
        this.isbn = isbn;
        this.titel = titel;
        this.status = status;
        this.foutReden = foutReden;
    }

    public String getIsbn() {
        return isbn;
    }

    public void setIsbn(String isbn) {
        this.isbn = isbn;
    }

    public String getTitel() {
        return titel;
    }

    public void setTitel(String titel) {
        this.titel = titel;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getFoutReden() {
        return foutReden;
    }

    public void setFoutReden(String foutReden) {
        this.foutReden = foutReden;
    }
}
