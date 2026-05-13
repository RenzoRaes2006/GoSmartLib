package ap.be.backend.dto;

import java.util.List;

public record LeeslijstMapDTO(
        Long id,
        String naam,
        String emoji,
        Long gebruikerId,
        List<BoekDTO> boeken) {
}
