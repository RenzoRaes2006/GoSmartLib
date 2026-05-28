package ap.be.backend.service.notificatie;

import ap.be.backend.entity.School;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

/**
 * Default implementatie: logt enkel naar de console. Wordt gebruikt zolang de
 * school nog geen Smartschool webservice API-key heeft, of in dev/demo.
 *
 * Activeer met: gosmartlib.notificatie.mode=log (of laat weg, dit is de default).
 */
@Service
@ConditionalOnProperty(name = "gosmartlib.notificatie.mode", havingValue = "log", matchIfMissing = true)
public class LogNotificatieService implements NotificatieService {

    @Override
    public void stuurTeLaatBericht(School school, String ontvangerSmartschoolId,
                                   String boekTitel, LocalDate inleverDatum) {
        System.out.printf("[Notificatie/LOG] te-laat-bericht: school=%s ontvanger=%s boek='%s' deadline=%s%n",
                school.getNaam(), ontvangerSmartschoolId, boekTitel, inleverDatum);
    }

    @Override
    public void stuurHerinnering(School school, String ontvangerSmartschoolId,
                                 String boekTitel, LocalDate inleverDatum) {
        System.out.printf("[Notificatie/LOG] herinnering: school=%s ontvanger=%s boek='%s' deadline=%s%n",
                school.getNaam(), ontvangerSmartschoolId, boekTitel, inleverDatum);
    }
}
