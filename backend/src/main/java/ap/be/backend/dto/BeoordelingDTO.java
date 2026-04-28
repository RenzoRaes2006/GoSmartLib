package ap.be.backend.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record BeoordelingDTO(
                @JsonProperty("id") Long id,

                //@JsonProperty("gebruikerId") @NotNull(message = "Gebruiker ID is verplicht") @Min(value = 1, message = "Gebruiker ID moet minimaal 1 zijn") long gebruikerId,

                @JsonProperty("gebruikerNaam") String gebruikerNaam,

                @JsonProperty("boekId") @NotNull(message = "Boek ID is verplicht") @Min(value = 1, message = "Boek ID moet minimaal 1 zijn") long boekId,

                @JsonProperty("waardering") @NotNull(message = "Waardering is verplicht") @Min(value = 1, message = "De waardering kan minimaal 1 zijn") @Max(value = 5, message = "De waardering mag maximaal 5 zijn") int waardering,

                @JsonProperty("inhoud") @jakarta.validation.constraints.NotBlank(message = "De inhoud van de beoordeling mag niet leeg zijn") @jakarta.validation.constraints.Size(max = 1000, message = "De beoordeling mag maximaal 1000 tekens lang zijn") String inhoud,

                @JsonProperty("isAnoniem") boolean isAnoniem,
                
                @JsonProperty("gerapporteerd") boolean gerapporteerd,

                @JsonProperty("isSpoiler") boolean isSpoiler) {
}