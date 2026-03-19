package ap.be.backend.mappers;

import ap.be.backend.dto.GerapporteerdeBeoordelingDTO;
import ap.be.backend.entity.Beoordeling;
import ap.be.backend.entity.GerapporteerdeBeoordeling;

public class GerapporteerdeBeoordelingMapper {

    // Van Entity naar DTO
    public static GerapporteerdeBeoordelingDTO toDTO(GerapporteerdeBeoordeling gerapporteerd) {
        return new GerapporteerdeBeoordelingDTO(
                gerapporteerd.getId(),
                gerapporteerd.getBeoordeling().getId(),
                gerapporteerd.getReden(),
                gerapporteerd.getDatumGemeld());
    }

    // Van DTO naar Entity
    public static GerapporteerdeBeoordeling toEntity(GerapporteerdeBeoordelingDTO dto, Beoordeling beoordeling) {
        GerapporteerdeBeoordeling gerapporteerd = new GerapporteerdeBeoordeling();
        gerapporteerd.setId(dto.id());
        gerapporteerd.setBeoordeling(beoordeling);
        gerapporteerd.setReden(dto.reden());
        if (dto.datumGemeld() != null) {
            gerapporteerd.setDatumGemeld(dto.datumGemeld());
        }
        return gerapporteerd;
    }
}