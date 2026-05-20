package ap.be.backend.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record GebruikerDTO(
        @JsonProperty("id") Long id,
        @JsonProperty("smartschoolId") String smartschoolId,
        @JsonProperty("voorNaam") String voorNaam,
        @JsonProperty("achternaam") String achternaam,
        @JsonProperty("naam") String naam,
        @JsonProperty("klas") String klas,
        @JsonProperty("rol") String rol,
        @JsonProperty("isBeheerder") boolean isBeheerder) {
}
