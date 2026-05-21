package ap.be.backend.mappers;

import ap.be.backend.dto.SchooljaarDTO;
import ap.be.backend.entity.Schooljaar;

public class SchooljaarMapper {

    public static SchooljaarDTO toDTO(Schooljaar schooljaar) {
        if (schooljaar == null) return null;
        return new SchooljaarDTO(
                schooljaar.getId(),
                schooljaar.getNaam(),
                schooljaar.getStartDatum(),
                schooljaar.getEindDatum(),
                schooljaar.isActief()
        );
    }
}
