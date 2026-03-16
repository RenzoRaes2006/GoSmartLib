package ap.be.backend.dto;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record GerapporteerdeBeoordelingDTO(
                @JsonProperty("id") Long id,
                @JsonProperty("reviewId") @NotNull @Min(1) Long reviewId,
                @JsonProperty("reden") @NotBlank @Size(max = 1000) String reden,
                @JsonProperty("datumGemeld") LocalDateTime datumGemeld) {
}
