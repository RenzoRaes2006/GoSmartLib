package ap.be.backend.mappers;

import ap.be.backend.dto.GebruikerDTO;
import ap.be.backend.entity.User;

public class GebruikerMapper {

    public static GebruikerDTO toDTO(User user) {
        return toDTO(user, null);
    }

    public static GebruikerDTO toDTO(User user, String naam) {
        if (user == null) {
            return null;
        }
        return new GebruikerDTO(
                user.getId(),
                user.getSmartschoolId(),
                naam,
                user.getKlas(),
                user.getRol() != null ? user.getRol().toString() : null,
                user.isBeheerder());
    }
}
