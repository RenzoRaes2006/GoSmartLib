package ap.be.backend.service.notificatie;

import ap.be.backend.entity.School;
import ap.be.backend.entity.Uitlening;
import ap.be.backend.entity.User;
import ap.be.backend.repository.UitleningRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

/**
 * Dagelijkse job die controleert welke uitleningen te laat zijn en de
 * leerling een bericht stuurt in zijn Smartschool-inbox.
 *
 * Twee fases:
 *   1. Eerste mail: zodra de inleverdatum verstreken is en het boek niet
 *      ingeleverd, sturen we ÉÉN te-laat-bericht.
 *   2. Herinnering: 7 dagen na de eerste mail sturen we — als het boek nog
 *      steeds niet binnen is — ÉÉN reminder. Daarna stoppen we.
 *
 * Beide stappen worden geregistreerd op de Uitlening zelf
 * (eersteTeLaatMailOp / herinneringMailOp) zodat we bij elke run weten wat al
 * verstuurd is, ook als de scheduler meerdere keren per dag draait of na een
 * crash herstart.
 */
@Component
public class TeLaatNotificatieScheduler {

    private static final int REMINDER_NA_DAGEN = 7;

    private final UitleningRepository uitleningRepository;
    private final NotificatieService notificatieService;

    public TeLaatNotificatieScheduler(UitleningRepository uitleningRepository,
                                      NotificatieService notificatieService) {
        this.uitleningRepository = uitleningRepository;
        this.notificatieService = notificatieService;
    }

    // Elke dag om 08:00. Cron: sec min uur dag-vd-maand maand dag-vd-week.
    @Scheduled(cron = "0 0 8 * * *")
    @Transactional
    public void verstuurDagelijkseNotificaties() {
        LocalDate vandaag = LocalDate.now();
        verstuurEersteMails(vandaag);
        verstuurHerinneringen(vandaag);
    }

    private void verstuurEersteMails(LocalDate vandaag) {
        List<Uitlening> teLaat = uitleningRepository.findTeLaatZonderEersteMail(vandaag);
        for (Uitlening u : teLaat) {
            School school = schoolVan(u);
            if (school == null || !heeftApiKey(school)) continue;
            try {
                notificatieService.stuurTeLaatBericht(
                        school,
                        u.getUser().getSmartschoolId(),
                        u.getExemplaar().getBoek().getTitel(),
                        u.getInleverDatum());
                u.setEersteTeLaatMailOp(vandaag);
                uitleningRepository.save(u);
            } catch (Exception e) {
                System.err.printf("[TeLaatScheduler] eerste mail mislukt voor uitlening %d: %s%n",
                        u.getId(), e.getMessage());
            }
        }
    }

    private void verstuurHerinneringen(LocalDate vandaag) {
        LocalDate drempel = vandaag.minusDays(REMINDER_NA_DAGEN);
        List<Uitlening> reminders = uitleningRepository.findKlaarVoorHerinnering(drempel);
        for (Uitlening u : reminders) {
            School school = schoolVan(u);
            if (school == null || !heeftApiKey(school)) continue;
            try {
                notificatieService.stuurHerinnering(
                        school,
                        u.getUser().getSmartschoolId(),
                        u.getExemplaar().getBoek().getTitel(),
                        u.getInleverDatum());
                u.setHerinneringMailOp(vandaag);
                uitleningRepository.save(u);
            } catch (Exception e) {
                System.err.printf("[TeLaatScheduler] herinnering mislukt voor uitlening %d: %s%n",
                        u.getId(), e.getMessage());
            }
        }
    }

    private School schoolVan(Uitlening u) {
        User user = u.getUser();
        return user == null ? null : user.getSchool();
    }

    private boolean heeftApiKey(School school) {
        return school.getSmartschoolApiKey() != null && !school.getSmartschoolApiKey().isBlank();
    }
}
