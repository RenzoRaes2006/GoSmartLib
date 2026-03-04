package ap.be.backend.dto;


import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

public record BoekDTO(
        @JsonProperty("id") Long id,
        @JsonProperty("titel") @NotBlank(message = "Titel mag niet leeg zijn") String titel,
        @JsonProperty("auteur") @NotBlank(message = "Auteur mag niet leeg zijn") String auteur,
        @JsonProperty("isbn") @NotBlank(message = "ISBN mag niet leeg zijn") @Pattern(regexp = "^(97(8|9))?\\d{9}(\\d|X)$", message = "Ongeldig ISBN-formaat") String isbn,
        @JsonProperty("uitgever") @NotBlank(message = "Uitgever mag niet leeg zijn") String uitgever,
        @JsonProperty("uitgaveJaar") @NotBlank(message = "Uitgavejaar mag niet leeg zijn") String uitgaveJaar,
        @JsonProperty("pagina") @NotNull @Min(value = 1, message = "Aantal pagina's mag niet leeg zijn") int pagina,
        @JsonProperty("genre") @NotBlank(message = "Genre mag niet leeg zijn") String genre,
        @JsonProperty("beschrijving") @NotBlank(message = "Beschrijving mag niet leeg zijn") String beschrijving,
        @JsonProperty("aantalExemplaren") @NotNull @Min(value = 0, message = "Aantal exemplaren mag niet leeg zijn") int aantalExemplaren,
        @JsonProperty("locatie") @NotBlank(message = "Locatie mag niet leeg zijn") String locatie,
        @JsonProperty("afbeelding") @NotBlank(message = "Afbeelding URL mag niet leeg zijn") String afbeelding) {
}