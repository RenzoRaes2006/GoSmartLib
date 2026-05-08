package ap.be.backend.dto;

import java.time.LocalDate;

public record UitleningDTO(
        Long id,
        BoekDTO boek,
        GebruikerDTO gebruiker,
        LocalDate uitleenDatum,
        LocalDate inleverDatum,
        boolean isIngeleverd,
        boolean isVerlengd,
        String status) {
}
