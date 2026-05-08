package ap.be.backend.mappers;

import ap.be.backend.dto.GebruikerDTO;
import ap.be.backend.entity.Gebruiker;

public class GebruikerMapper {

    public static GebruikerDTO toDTO(Gebruiker gebruiker) {
        if (gebruiker == null) {
            return null;
        }
        return new GebruikerDTO(
                gebruiker.getGebruikerID(),
                gebruiker.getSchoolId(),
                gebruiker.getVoorNaam(),
                gebruiker.getAchternaam(),
                gebruiker.getEmail(),
                gebruiker.getKlas(),
                gebruiker.getRol() != null ? gebruiker.getRol().toString() : null);
    }
}
