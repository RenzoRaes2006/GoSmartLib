package ap.be.backend.mappers;

import java.util.List;
import java.util.stream.Collectors;

import ap.be.backend.dto.LeeslijstMapDTO;
import ap.be.backend.entity.LeeslijstMap;

public class LeeslijstMapMapper {

    public static LeeslijstMapDTO toDTO(LeeslijstMap map) {
        if (map == null) {
            return null;
        }
        return new LeeslijstMapDTO(
                map.getId(),
                map.getNaam(),
                map.getEmoji(),
                map.getGebruiker() != null ? map.getGebruiker().getGebruikerID() : null,
                map.getBoeken() != null
                        ? map.getBoeken().stream().map(BoekMapper::toDTO).collect(Collectors.toList())
                        : List.of());
    }
}
