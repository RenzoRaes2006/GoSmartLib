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
        // Geeft aan of deze leeslijst gearchiveerd is na een schooljaar-rollover.
        // Gearchiveerde lijsten zijn niet zichtbaar in de actieve weergave maar wel via het archief.
        boolean gearchiveerd,
        List<BoekDTO> boeken) {
}
