package ap.be.backend.mappers;

import ap.be.backend.dto.BoekDTO;
import ap.be.backend.dto.KlassLeeslijstResponseDTO;
import ap.be.backend.entity.KlassLeeslijst;
import java.util.List;

/**
 * Zet KlassLeeslijst-entiteiten om naar KlassLeeslijstResponseDTO's.
 */
public class KlassLeeslijstMapper {

    /**
     * Zet een KlassLeeslijst-entiteit om naar een DTO inclusief de gekoppelde boeken.
     *
     * @param entity de te converteren leeslijst
     * @return de leeslijst als DTO
     */
    public static KlassLeeslijstResponseDTO toDTO(KlassLeeslijst entity) {
        List<BoekDTO> boekDTOs = entity.getBoeken().stream()
                .map(BoekMapper::toDTO)
                .toList();

        return new KlassLeeslijstResponseDTO(
                entity.getId(),
                entity.getNaam(),
                boekDTOs);
    }
}
