package ap.be.backend.mappers;

import ap.be.backend.dto.UitleningDTO;
import ap.be.backend.dto.UitleningSummaryDTO;
import ap.be.backend.entity.Boek;
import ap.be.backend.entity.Uitlening;
import ap.be.backend.entity.User;

public class UitleningMapper {

    public static UitleningDTO toDTO(Uitlening uitlening) {
        if (uitlening == null) {
            return null;
        }
        return new UitleningDTO(
                uitlening.getId(),
                BoekMapper.toDTO(uitlening.getExemplaar().getBoek()),
                GebruikerMapper.toDTO(uitlening.getUser()),
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
        Boek boek = uitlening.getExemplaar() == null ? null : uitlening.getExemplaar().getBoek();
        User user = uitlening.getUser();
        return new UitleningSummaryDTO(
                uitlening.getId(),
                boek != null ? boek.getId() : null,
                boek != null ? boek.getTitel() : null,
                user != null ? user.getId() : null,
                user != null ? user.getSmartschoolId() : null,
                uitlening.getUitleenDatum(),
                uitlening.getInleverDatum(),
                uitlening.isIngeleverd(),
                uitlening.isVerlengd(),
                uitlening.getStatus());
    }
}
