package ap.be.backend.mappers;

import ap.be.backend.dto.GebruikerDTO;
import ap.be.backend.entity.User;

public class GebruikerMapper {

    public static GebruikerDTO toDTO(User user) {
        return toDTO(user, null, null);
    }

    public static GebruikerDTO toDTO(User user, String voorNaam, String achternaam) {
        if (user == null) {
            return null;
        }
        String voor = voorNaam == null ? "" : voorNaam.trim();
        String achter = achternaam == null ? "" : achternaam.trim();
        String volledig = (voor + " " + achter).trim();
        return new GebruikerDTO(
                user.getId(),
                user.getSmartschoolId(),
                voor.isEmpty() ? null : voor,
                achter.isEmpty() ? null : achter,
                volledig.isEmpty() ? null : volledig,
                user.getKlas(),
                user.getRol() != null ? user.getRol().toString() : null,
                user.isBeheerder());
    }
}
