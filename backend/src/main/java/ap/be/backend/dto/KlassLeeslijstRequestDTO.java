package ap.be.backend.dto;

import java.util.List;

public record KlassLeeslijstRequestDTO(
        String naam,
        String klas,
        List<Long> leerlingIds,
        String beschrijving,
        Boolean zichtbaar) {
}
