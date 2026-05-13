package ap.be.backend.mappers;

import ap.be.backend.dto.GerapporteerdeBeoordelingAdminDTO;
import ap.be.backend.dto.GerapporteerdeBeoordelingDTO;
import ap.be.backend.entity.GerapporteerdeBeoordeling;

public class GerapporteerdeBeoordelingMapper {

    public static GerapporteerdeBeoordelingDTO toDTO(GerapporteerdeBeoordeling gerapporteerd) {
        return new GerapporteerdeBeoordelingDTO(
                gerapporteerd.getId(),
                gerapporteerd.getBeoordeling().getId(),
                gerapporteerd.getReden(),
                gerapporteerd.getDatumGemeld());
    }

    public static GerapporteerdeBeoordelingAdminDTO toAdminDTO(GerapporteerdeBeoordeling entity) {
        return new GerapporteerdeBeoordelingAdminDTO(
                entity.getId(),
                entity.getBeoordeling().getId(),
                entity.getBeoordeling().getBoek().getTitel(),
                entity.getBeoordeling().getInhoud(),
                entity.getBeoordeling().getUser().getId(),
                entity.getUserMelder().getId(),
                entity.getReden(),
                entity.getDatumGemeld());
    }
}
