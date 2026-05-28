package ap.be.backend.service.notificatie;

import ap.be.backend.entity.School;

import java.time.LocalDate;

public interface NotificatieService {

    void stuurTeLaatBericht(School school,
                            String ontvangerSmartschoolId,
                            String boekTitel,
                            LocalDate inleverDatum);

    void stuurHerinnering(School school,
                          String ontvangerSmartschoolId,
                          String boekTitel,
                          LocalDate inleverDatum);
}
