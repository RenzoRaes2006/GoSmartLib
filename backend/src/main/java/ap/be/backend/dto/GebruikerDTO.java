package ap.be.backend.dto;

import java.time.LocalDate;

import com.fasterxml.jackson.annotation.JsonProperty;

public record GebruikerDTO(
        @JsonProperty("id") Long id,
        @JsonProperty("smartschoolId") String smartschoolId,
        @JsonProperty("voorNaam") String voorNaam,
        @JsonProperty("achternaam") String achternaam,
        @JsonProperty("naam") String naam,
        @JsonProperty("klas") String klas,
        @JsonProperty("rol") String rol,
        @JsonProperty("isBeheerder") boolean isBeheerder,
        @JsonProperty("actief") boolean actief,
        @JsonProperty("aangekomenOp") LocalDate aangekomenOp,
        @JsonProperty("vertrokkenOp") LocalDate vertrokkenOp) {
}
