package ap.be.backend.dto;

import java.util.List;

public record KlassLeeslijstResponseDTO(
        Long id,
        String naam,
        String klas,
        Long leerlingId,
        String leerlingNaam,
        String beschrijving,
        boolean zichtbaar,
        List<BoekDTO> boeken) {
}
