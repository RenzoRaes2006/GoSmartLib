package ap.be.backend.mappers;

import ap.be.backend.dto.SchoolDTO;
import ap.be.backend.entity.School;

public class SchoolMapper {

    public static SchoolDTO toDTO(School school) {
        if (school == null) {
            return null;
        }
        return new SchoolDTO(
                school.getId(),
                school.getNaam(),
                school.getAdres(),
                school.getApiCode());
    }
}
