package ap.be.backend.dto;

import java.time.LocalDate;

public record UitleningSummaryDTO(
        Long id,
        Long boekId,
        String boekTitel,
        Long gebruikerId,
        String smartschoolId,
        LocalDate uitleenDatum,
        LocalDate inleverDatum,
        boolean isIngeleverd,
        boolean isVerlengd,
        String status) {
}
