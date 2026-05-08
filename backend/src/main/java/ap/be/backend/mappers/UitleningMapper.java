package ap.be.backend.mappers;

import ap.be.backend.dto.UitleningDTO;
import ap.be.backend.dto.UitleningSummaryDTO;
import ap.be.backend.entity.Boek;
import ap.be.backend.entity.Gebruiker;
import ap.be.backend.entity.Uitlening;

public class UitleningMapper {

    public static UitleningDTO toDTO(Uitlening uitlening) {
        if (uitlening == null) {
            return null;
        }
        return new UitleningDTO(
                uitlening.getId(),
                BoekMapper.toDTO(uitlening.getBoek()),
                GebruikerMapper.toDTO(uitlening.getGebruiker()),
                uitlening.getUitleenDatum(),
                uitlening.getInleverDatum(),
                uitlening.isIngeleverd(),
                uitlening.isVerlengd(),
                uitlening.getStatus());
    }

    public static UitleningSummaryDTO toSummaryDTO(Uitlening uitlening) {
        if (uitlening == null) {
            return null;
        }
        Boek boek = uitlening.getBoek();
        Gebruiker gebruiker = uitlening.getGebruiker();
        return new UitleningSummaryDTO(
                uitlening.getId(),
                boek != null ? boek.getId() : null,
                boek != null ? boek.getTitel() : null,
                gebruiker != null ? gebruiker.getGebruikerID() : null,
                gebruiker != null ? gebruiker.getVoorNaam() : null,
                gebruiker != null ? gebruiker.getAchternaam() : null,
                uitlening.getUitleenDatum(),
                uitlening.getInleverDatum(),
                uitlening.isIngeleverd(),
                uitlening.isVerlengd(),
                uitlening.getStatus());
    }
}
