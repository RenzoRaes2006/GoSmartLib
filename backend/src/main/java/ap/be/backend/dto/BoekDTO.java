package ap.be.backend.dto;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;

public record BoekDTO(
        @JsonProperty("id") Long id,
        @JsonProperty("titel") @NotBlank(message = "Titel mag niet leeg zijn") String titel,
        @JsonProperty("auteur") @NotBlank(message = "Auteur mag niet leeg zijn") String auteur,
        @JsonProperty("isbn") @NotBlank(message = "ISBN mag niet leeg zijn") @Pattern(regexp = "^\\d{10}$|^\\d{13}$", message = "ISBN moet 10 of 13 cijfers zijn") String isbn,
        @JsonProperty("uitgever") @NotBlank(message = "Uitgever mag niet leeg zijn") String uitgever,
        @JsonProperty("uitgaveJaar") @NotBlank(message = "Uitgavejaar mag niet leeg zijn") String uitgaveJaar,
        @JsonProperty("pagina") @Min(value = 1, message = "Aantal pagina's mag niet leeg zijn") int pagina,
        @JsonProperty("genre") @NotEmpty(message = "Genre mag niet leeg zijn") List<String> genre,
        @JsonProperty("beschrijving") @NotBlank(message = "Beschrijving mag niet leeg zijn") String beschrijving,
        @JsonProperty("aantalExemplaren") @Min(value = 0, message = "Aantal exemplaren mag niet leeg zijn") int aantalExemplaren,
        @JsonProperty("locatie") @NotBlank(message = "Locatie mag niet leeg zijn") String locatie,
        @JsonProperty("afbeelding") String afbeelding,
        @JsonProperty("populariteit") Double populariteit,
        @JsonProperty("leesniveau") String leesniveau) {
}
