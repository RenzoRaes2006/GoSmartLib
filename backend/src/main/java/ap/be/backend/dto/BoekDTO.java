package ap.be.backend.dto;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record BoekDTO(
        @JsonProperty("id") Long id,
        @JsonProperty("titel") @NotBlank(message = "Titel mag niet leeg zijn") String titel,
        @JsonProperty("auteur") @NotBlank(message = "Auteur mag niet leeg zijn") String auteur,
        @JsonProperty("isbn") @NotBlank(message = "ISBN mag niet leeg zijn") @Pattern(regexp = "^(\\d{10}|\\d{13}|GO\\d{11})$", message = "Ongeldig ISBN-formaat") String isbn,
        @JsonProperty("uitgever") String uitgever,
        @JsonProperty("uitgaveJaar") String uitgaveJaar,
        @JsonProperty("pagina") int pagina,
        @JsonProperty("tags") List<TagDTO> tags,
        @JsonProperty("beschrijving") @NotBlank(message = "Beschrijving mag niet leeg zijn") String beschrijving,
        @JsonProperty("aantalExemplaren") @Min(value = 0, message = "Aantal exemplaren mag niet negatief zijn") int aantalExemplaren,
        @JsonProperty("locatie") String locatie,
        @JsonProperty("afbeelding") String afbeelding,
        @JsonProperty("populariteit") Double populariteit,
        @JsonProperty("leesniveau") String leesniveau,
        @JsonProperty("didactischMateriaal") Boolean didactischMateriaal,
        @JsonProperty("campusId") Long campusId,
        @JsonProperty("campusNaam") String campusNaam,
        @JsonProperty("heeftSchade") boolean heeftSchade,
        @JsonProperty("aantalBeschadigd") int aantalBeschadigd) {
}
