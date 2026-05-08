package ap.be.backend.dto;

import java.time.LocalDate;

public record UitleningSummaryDTO(
        Long id,
        Long boekId,
        String boekTitel,
        Long gebruikerId,
        String gebruikerVoorNaam,
        String gebruikerAchternaam,
        LocalDate uitleenDatum,
        LocalDate inleverDatum,
        boolean isIngeleverd,
        boolean isVerlengd,
        String status) {
}
