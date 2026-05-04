package ap.be.backend.dto;

import java.util.List;

public record KlassLeeslijstResponseDTO(
        Long id,
        String naam,
        List<BoekDTO> boeken) {
}