package ap.be.backend.mappers;

import ap.be.backend.dto.BoekDTO;
import ap.be.backend.dto.KlassLeeslijstResponseDTO;
import ap.be.backend.entity.KlassLeeslijst;
import java.util.List;

public class KlassLeeslijstMapper {

    public static KlassLeeslijstResponseDTO toDTO(KlassLeeslijst entity) {
        // Gebruik je bestaande BoekMapper voor de individuele boeken
        List<BoekDTO> boekDTOs = entity.getBoeken().stream()
                .map(BoekMapper::toDTO)
                .toList();

        return new KlassLeeslijstResponseDTO(
                entity.getId(),
                entity.getNaam(),
                boekDTOs);
    }
}