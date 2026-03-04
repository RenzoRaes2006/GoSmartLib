package ap.be.backend.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record BeoordelingDTO(
                @JsonProperty("id") Long id,
                @JsonProperty("gebruikerId") @NotNull @Min(1) long gebruikerId,
                @JsonProperty("boekId") @NotNull @Min(1) long boekId,
                @JsonProperty("waardering") @NotNull @Min(1) @Max(5) int waardering,
                @JsonProperty("inhoud") @NotBlank @Size(max = 1000) String inhoud) {
}
