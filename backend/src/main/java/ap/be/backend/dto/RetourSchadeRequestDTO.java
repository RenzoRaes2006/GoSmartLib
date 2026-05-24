package ap.be.backend.dto;

import java.util.List;

/**
 * Request voor het inleveren van een batch boeken met per-boek schade-info.
 * Voor de UITLENING-flow wordt nog steeds de bestaande {@code ScanBatchRequestDTO} gebruikt.
 */
public class RetourSchadeRequestDTO {
    private Long gebruikerId;
    private List<Item> items;

    public Long getGebruikerId() { return gebruikerId; }
    public void setGebruikerId(Long gebruikerId) { this.gebruikerId = gebruikerId; }
    public List<Item> getItems() { return items; }
    public void setItems(List<Item> items) { this.items = items; }

    public static class Item {
        private String isbn;
        private boolean beschadigd;
        private String opmerking;

        public String getIsbn() { return isbn; }
        public void setIsbn(String isbn) { this.isbn = isbn; }
        public boolean isBeschadigd() { return beschadigd; }
        public void setBeschadigd(boolean beschadigd) { this.beschadigd = beschadigd; }
        public String getOpmerking() { return opmerking; }
        public void setOpmerking(String opmerking) { this.opmerking = opmerking; }
    }
}
