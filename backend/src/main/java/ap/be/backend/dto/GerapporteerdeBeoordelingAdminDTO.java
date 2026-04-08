package ap.be.backend.dto;

import java.time.LocalDateTime;
import com.fasterxml.jackson.annotation.JsonProperty;

public record GerapporteerdeBeoordelingAdminDTO(
        @JsonProperty("id") Long id,
        @JsonProperty("reviewId") Long reviewId,
        @JsonProperty("boekTitel") String boekTitel,
        @JsonProperty("reviewInhoud") String reviewInhoud,
        @JsonProperty("schrijverId") Number schrijverId,
        @JsonProperty("melderId") Number melderId,
        @JsonProperty("reden") String reden,
        @JsonProperty("datumGemeld") LocalDateTime datumGemeld) {
}