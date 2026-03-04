package ap.be.backend.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record GebruikerDTO(
        @JsonProperty("gebruikerID") Long gebruikerID,
        @JsonProperty("schoolId") @NotNull(message = "SchoolId mag niet leeg zijn") long schoolId,
        @JsonProperty("voorNaam") @NotBlank(message = "Voornaam mag niet leeg zijn") String voorNaam,
        @JsonProperty("achternaam") @NotBlank(message = "Achternaam mag niet leeg zijn") String achternaam,
        @JsonProperty("email") @NotBlank(message = "Email mag niet leeg zijn") @Email(message = "Email moet geldig zijn") String email,
        @JsonProperty("rol") @NotBlank(message = "Rol mag niet leeg zijn") String rol) {
}
