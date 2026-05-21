package ap.be.backend.dto;

import java.time.LocalDate;

public record SchooljaarDTO(
        Long id,
        String naam,
        LocalDate startDatum,
        LocalDate eindDatum,
        boolean actief
) {}
