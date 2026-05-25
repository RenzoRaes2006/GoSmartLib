package ap.be.backend.service;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import ap.be.backend.config.SecurityUtils;
import ap.be.backend.entity.AuditActie;
import ap.be.backend.entity.KlasWijziging;
import ap.be.backend.entity.Rol;
import ap.be.backend.entity.School;
import ap.be.backend.entity.User;
import ap.be.backend.exception.GebruikerNotFoundException;
import ap.be.backend.exception.GebruikerVertrekGeblokkeerdException;
import ap.be.backend.exception.SchoolNotFoundException;
import ap.be.backend.repository.KlasWijzigingRepository;
import ap.be.backend.repository.SchoolRepository;
import ap.be.backend.repository.UitleningRepository;
import ap.be.backend.repository.UserRepository;

/**
 * Beheert de lifecycle-acties op leerlingen en leerkrachten die niet via
 * OneRoster-sync gebeuren: handmatig toevoegen (bv. een nieuwe leerling die
 * net is ingeschreven maar nog niet in OneRoster staat) en markeren-als-vertrokken.
 *
 * De rollover-flow zet vertrekkende leerlingen ook op {@code actief=false},
 * maar gebruikt z'n eigen pad in {@link SchooljaarService}. Deze service is
 * voor de individuele, ad-hoc actie via de admin-UI.
 */
@Service
public class GebruikerLifecycleService {

    private final UserRepository userRepository;
    private final SchoolRepository schoolRepository;
    private final UitleningRepository uitleningRepository;
    private final OneRosterClient oneRosterClient;
    private final KlasWijzigingRepository klasWijzigingRepository;
    private final AuditService auditService;

    public GebruikerLifecycleService(UserRepository userRepository,
                                     SchoolRepository schoolRepository,
                                     UitleningRepository uitleningRepository,
                                     OneRosterClient oneRosterClient,
                                     KlasWijzigingRepository klasWijzigingRepository,
                                     AuditService auditService) {
        this.userRepository = userRepository;
        this.schoolRepository = schoolRepository;
        this.uitleningRepository = uitleningRepository;
        this.oneRosterClient = oneRosterClient;
        this.klasWijzigingRepository = klasWijzigingRepository;
        this.auditService = auditService;
    }

    /**
     * Wijzigt de klas van een leerling en schrijft een historiek-rij.
     * Geen-op als de nieuwe klas gelijk is aan de huidige (case-sensitive zoals
     * elders in het project). De nieuwe klas moet bestaan voor de school —
     * dezelfde validatie als bij handmatig toevoegen.
     */
    @Transactional
    public User wijzigKlas(Long leerlingId, String nieuweKlas) {
        if (nieuweKlas == null || nieuweKlas.isBlank()) {
            throw new IllegalArgumentException("Nieuwe klas is verplicht.");
        }

        User leerling = userRepository.findById(leerlingId)
                .orElseThrow(() -> new GebruikerNotFoundException("Gebruiker met id " + leerlingId + " niet gevonden."));

        if (leerling.getRol() != Rol.LEERLING) {
            throw new IllegalArgumentException("Klas-wijziging is enkel voor leerlingen.");
        }

        String nieuw = nieuweKlas.trim();
        String oud = leerling.getKlas();
        if (java.util.Objects.equals(oud, nieuw)) {
            return leerling;
        }

        School school = leerling.getSchool();
        if (school != null) {
            List<String> geldigeKlassen = bepaalGeldigeKlassen(school);
            if (!geldigeKlassen.contains(nieuw)) {
                throw new IllegalArgumentException(
                        "Klas '" + nieuw + "' bestaat niet voor deze school. "
                        + "Synchroniseer eerst met OneRoster of kies een bestaande klas.");
            }
        }

        leerling.setKlas(nieuw);
        userRepository.save(leerling);

        User doorWie = SecurityUtils.getCurrentUserOptional(userRepository).orElse(null);
        schrijfHistoriek(leerling, oud, nieuw, doorWie, "HANDMATIG");
        auditService.log(AuditActie.KLAS_GEWIJZIGD, "Gebruiker", leerling.getId(),
                AuditService.details("smartschoolId", leerling.getSmartschoolId(),
                        "van", oud, "naar", nieuw));
        return leerling;
    }

    /**
     * Schrijft een KlasWijziging-rij. Wordt zowel intern gebruikt als door
     * OneRosterImportService aangeroepen voor sync-events.
     */
    @Transactional
    public void schrijfHistoriek(User leerling, String vanKlas, String naarKlas, User doorWie, String bron) {
        KlasWijziging rij = new KlasWijziging();
        rij.setLeerling(leerling);
        rij.setVanKlas(vanKlas);
        rij.setNaarKlas(naarKlas);
        rij.setDoorWie(doorWie);
        rij.setBron(bron);
        klasWijzigingRepository.save(rij);
    }

    @Transactional(readOnly = true)
    public org.springframework.data.domain.Page<KlasWijziging> getHistoriek(
            Long leerlingId, org.springframework.data.domain.Pageable pageable) {
        return klasWijzigingRepository.findByLeerling_IdOrderByGewijzigdOpDesc(leerlingId, pageable);
    }

    @Transactional
    public User voegHandmatigToe(String smartschoolId, Long schoolId, String klas, Rol rol) {
        if (smartschoolId == null || smartschoolId.isBlank()) {
            throw new IllegalArgumentException("smartschoolId is verplicht.");
        }
        if (schoolId == null) {
            throw new IllegalArgumentException("schoolId is verplicht.");
        }
        if (rol != Rol.LEERLING && rol != Rol.LEERKRACHT) {
            throw new IllegalArgumentException("Alleen LEERLING of LEERKRACHT kan handmatig toegevoegd worden.");
        }

        School school = schoolRepository.findById(schoolId)
                .orElseThrow(() -> new SchoolNotFoundException(schoolId));

        String trimmed = smartschoolId.trim();
        if (userRepository.findBySmartschoolId(trimmed).isPresent()) {
            throw new IllegalArgumentException("Er bestaat al een gebruiker met smartschoolId " + trimmed + ".");
        }

        String klasGetrimd = klas == null ? null : klas.trim();
        if (klasGetrimd != null && !klasGetrimd.isBlank()) {
            List<String> geldigeKlassen = bepaalGeldigeKlassen(school);
            if (!geldigeKlassen.contains(klasGetrimd)) {
                throw new IllegalArgumentException(
                        "Klas '" + klasGetrimd + "' bestaat niet voor deze school. "
                        + "Synchroniseer eerst met OneRoster of kies een bestaande klas.");
            }
        } else {
            klasGetrimd = null;
        }

        User user = new User();
        user.setSmartschoolId(trimmed);
        user.setSchool(school);
        user.setRol(rol);
        user.setKlas(klasGetrimd);
        user.setActief(true);
        user.setAangekomenOp(LocalDate.now());
        user.setVertrokkenOp(null);
        userRepository.save(user);
        auditService.log(AuditActie.GEBRUIKER_TOEGEVOEGD, "Gebruiker", user.getId(),
                AuditService.details("smartschoolId", trimmed, "rol", rol.name(),
                        "klas", klasGetrimd, "schoolId", schoolId, "bron", "HANDMATIG"));
        return user;
    }

    /**
     * Bepaalt welke klas-strings geldig zijn voor een school. Gebruikt OneRoster
     * als bron-van-waarheid; valt terug op DB-distinct als OneRoster onbereikbaar
     * is of de school nog geen credentials heeft.
     */
    @SuppressWarnings("unchecked")
    private List<String> bepaalGeldigeKlassen(School school) {
        if (school.getSmartschoolDomein() == null
                || school.getOauthClientId() == null
                || school.getOauthClientSecret() == null) {
            return userRepository.findDistinctKlassenBySchoolId(school.getId());
        }
        try {
            Map<String, Object> data = oneRosterClient.haalLiveSchoolDataOp(
                    school.getSmartschoolDomein(),
                    school.getOauthClientId(),
                    school.getOauthClientSecret());
            List<Map<String, Object>> klassen = (List<Map<String, Object>>) data.getOrDefault("klassen", List.of());
            List<String> result = klassen.stream()
                    .map(k -> String.valueOf(k.getOrDefault("naam", "")))
                    .filter(n -> !n.isBlank())
                    .distinct()
                    .toList();
            return result.isEmpty() ? userRepository.findDistinctKlassenBySchoolId(school.getId()) : result;
        } catch (Exception e) {
            return userRepository.findDistinctKlassenBySchoolId(school.getId());
        }
    }

    /**
     * Zet een vertrokken gebruiker terug op actief. Idempotent — al-actieve
     * gebruikers blijven zoals ze zijn. Werkt zowel voor leerlingen als leerkrachten.
     */
    @Transactional
    public User reactiveer(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new GebruikerNotFoundException("Gebruiker met id " + userId + " niet gevonden."));

        if (user.isActief()) {
            return user;
        }

        user.setActief(true);
        user.setVertrokkenOp(null);
        user.setAangekomenOp(LocalDate.now());
        userRepository.save(user);
        auditService.log(AuditActie.GEBRUIKER_GEREACTIVEERD, "Gebruiker", user.getId(),
                AuditService.details("smartschoolId", user.getSmartschoolId(),
                        "rol", user.getRol() == null ? null : user.getRol().name()));
        return user;
    }

    @Transactional
    public User markeerVertrokken(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new GebruikerNotFoundException("Gebruiker met id " + userId + " niet gevonden."));

        if (!user.isActief()) {
            // Al vertrokken — idempotent, geen actie nodig.
            return user;
        }

        List<?> openLeningen = uitleningRepository.findByUser_IdAndIsIngeleverdFalse(userId);
        if (!openLeningen.isEmpty()) {
            throw new GebruikerVertrekGeblokkeerdException(openLeningen.size());
        }

        user.setActief(false);
        user.setVertrokkenOp(LocalDate.now());
        userRepository.save(user);
        auditService.log(AuditActie.GEBRUIKER_VERTROKKEN, "Gebruiker", user.getId(),
                AuditService.details("smartschoolId", user.getSmartschoolId(),
                        "rol", user.getRol() == null ? null : user.getRol().name(),
                        "klas", user.getKlas()));
        return user;
    }
}
