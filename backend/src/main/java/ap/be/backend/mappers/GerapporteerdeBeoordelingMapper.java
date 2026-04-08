package ap.be.backend.mappers;

import ap.be.backend.dto.GerapporteerdeBeoordelingAdminDTO;
import ap.be.backend.dto.GerapporteerdeBeoordelingDTO;
import ap.be.backend.entity.Beoordeling;
import ap.be.backend.entity.GerapporteerdeBeoordeling;


// deze mapper werkt met twee verschillende DTO's: een voor algemene gebruikers (GerapporteerdeBeoordelingDTO) en een uitgebreidere voor admins (GerapporteerdeBeoordelingAdminDTO).
public class GerapporteerdeBeoordelingMapper {

    // Van Entity naar DTO
    public static GerapporteerdeBeoordelingDTO toDTO(GerapporteerdeBeoordeling gerapporteerd) {
        return new GerapporteerdeBeoordelingDTO(
                gerapporteerd.getId(),
                gerapporteerd.getBeoordeling().getId(),
                gerapporteerd.getReden(),
                gerapporteerd.getDatumGemeld());
    }

// Voor admin overzicht (GET) - Van Entity naar Admin DTO
    public static GerapporteerdeBeoordelingAdminDTO toAdminDTO(GerapporteerdeBeoordeling entity) {
        return new GerapporteerdeBeoordelingAdminDTO(
                entity.getId(),
                entity.getBeoordeling().getId(),
                entity.getBeoordeling().getBoek().getTitel(), // Haalt titel via beoordeling -> boek
                entity.getBeoordeling().getInhoud(),
                entity.getBeoordeling().getGebruiker().getSchoolId(), // Originele schrijver
                entity.getGebruikerMelder().getSchoolId(), // De persoon die rapporteert
                entity.getReden(),
                entity.getDatumGemeld()
        );
    }
}