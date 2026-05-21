package ap.be.backend.dto;

import java.util.List;

public record KlassLeeslijstResponseDTO(
        Long id,
        String naam,
        String klas,
        List<Long> leerlingIds,
        List<String> leerlingNamen,
        String beschrijving,
        boolean zichtbaar,
        boolean gearchiveerd,
        List<BoekDTO> boeken) {
}
