package ap.be.backend.service;

import ap.be.backend.entity.Rol;
import ap.be.backend.entity.School;
import ap.be.backend.entity.User;
import ap.be.backend.repository.SchoolRepository;
import ap.be.backend.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

/**
 * Bulk-import vanuit OneRoster naar de lokale database. Idempotent: een tweede
 * aanroep met identieke OneRoster-data wijzigt geen records.
 */
@Slf4j
@Service
public class OneRosterImportService {

    private final SchoolRepository schoolRepository;
    private final UserRepository userRepository;
    private final OneRosterClient oneRosterClient;

    public OneRosterImportService(SchoolRepository schoolRepository,
                                  UserRepository userRepository,
                                  OneRosterClient oneRosterClient) {
        this.schoolRepository = schoolRepository;
        this.userRepository = userRepository;
        this.oneRosterClient = oneRosterClient;
    }

    public List<Map<String, Object>> fetchKlassen(Long schoolId) {
        return castLijst(rawData(schoolId).get("klassen"));
    }

    public Map<String, List<Map<String, Object>>> fetchGebruikers(Long schoolId) {
        Map<String, Object> data = rawData(schoolId);
        return Map.of(
                "leerlingen", castLijst(data.get("leerlingen")),
                "leerkrachten", castLijst(data.get("leerkrachten"))
        );
    }

    @Transactional
    public ImportResultaat importSchool(Long schoolId) {
        School school = schoolRepository.findById(schoolId)
                .orElseThrow(() -> new IllegalArgumentException("School bestaat niet: " + schoolId));

        if (school.getSmartschoolDomein() == null
                || school.getOauthClientId() == null
                || school.getOauthClientSecret() == null) {
            throw new IllegalStateException("School heeft geen OneRoster-credentials");
        }

        Map<String, Object> data = oneRosterClient.haalLiveSchoolDataOp(
                school.getSmartschoolDomein(),
                school.getOauthClientId(),
                school.getOauthClientSecret());

        Telling leerlingen = importeerType(castLijst(data.get("leerlingen")), Rol.LEERLING, school);
        Telling leerkrachten = importeerType(castLijst(data.get("leerkrachten")), Rol.LEERKRACHT, school);
        int aantalKlassen = castLijst(data.get("klassen")).size();

        // Verzamel de smartschoolIds van leerlingen die OneRoster teruggeeft.
        // De SchooljaarService gebruikt deze set om te bepalen wie vertrokken is.
        Set<String> gevondenLeerlingIds = extractSmartschoolIds(castLijst(data.get("leerlingen")));

        ImportResultaat resultaat = new ImportResultaat(
                leerlingen.nieuw, leerlingen.geupdatet, leerlingen.ongewijzigd,
                leerkrachten.nieuw, leerkrachten.geupdatet, leerkrachten.ongewijzigd,
                aantalKlassen, gevondenLeerlingIds);

        log.info("OneRoster import school {}: leerlingen +{}/~{}/={}, leerkrachten +{}/~{}/={}, klassen {}",
                school.getId(),
                resultaat.leerlingenNieuw(), resultaat.leerlingenGeupdatet(), resultaat.leerlingenOngewijzigd(),
                resultaat.leerkrachtenNieuw(), resultaat.leerkrachtenGeupdatet(), resultaat.leerkrachtenOngewijzigd(),
                resultaat.aantalKlassen());
        return resultaat;
    }

    private Telling importeerType(List<Map<String, Object>> bron, Rol rol, School school) {
        Telling t = new Telling();
        for (Map<String, Object> rij : bron) {
            String smartschoolId = String.valueOf(rij.get("id"));
            if (smartschoolId == null || smartschoolId.isBlank() || "null".equals(smartschoolId)) {
                log.debug("OneRoster {}: rij overgeslagen wegens lege id ({})", rol, rij);
                continue;
            }
            String klas = (String) rij.getOrDefault("klas", "");

            Optional<User> bestaand = userRepository.findBySmartschoolId(smartschoolId);
            if (bestaand.isEmpty()) {
                User nieuw = new User(smartschoolId, klas, rol);
                nieuw.setSchool(school);
                nieuw.setAangekomenOp(java.time.LocalDate.now());
                userRepository.save(nieuw);
                t.nieuw++;
                log.debug("OneRoster {} nieuw: {} (klas={})", rol, smartschoolId, klas);
            } else {
                User u = bestaand.get();
                boolean gewijzigd = false;
                if (!Objects.equals(u.getKlas(), klas)) {
                    u.setKlas(klas);
                    gewijzigd = true;
                }
                if (u.getRol() != rol) {
                    u.setRol(rol);
                    gewijzigd = true;
                }
                if (u.getSchool() == null
                        || !Objects.equals(u.getSchool().getId(), school.getId())) {
                    u.setSchool(school);
                    gewijzigd = true;
                }
                if (gewijzigd) {
                    userRepository.save(u);
                    t.geupdatet++;
                    log.debug("OneRoster {} geupdatet: {} (klas={})", rol, smartschoolId, klas);
                } else {
                    t.ongewijzigd++;
                }
            }
        }
        return t;
    }

    private Map<String, Object> rawData(Long schoolId) {
        School school = schoolRepository.findById(schoolId)
                .orElseThrow(() -> new IllegalArgumentException("School bestaat niet: " + schoolId));
        return oneRosterClient.haalLiveSchoolDataOp(
                school.getSmartschoolDomein(),
                school.getOauthClientId(),
                school.getOauthClientSecret());
    }

    // Haalt alle smartschoolIds op uit een lijst van OneRoster-rijen.
    private Set<String> extractSmartschoolIds(List<Map<String, Object>> rijen) {
        Set<String> ids = new HashSet<>();
        for (Map<String, Object> rij : rijen) {
            String id = String.valueOf(rij.get("id"));
            if (id != null && !id.isBlank() && !"null".equals(id)) {
                ids.add(id);
            }
        }
        return ids;
    }

    @SuppressWarnings("unchecked")
    private List<Map<String, Object>> castLijst(Object obj) {
        if (obj instanceof List<?> lst) {
            List<Map<String, Object>> result = new ArrayList<>();
            for (Object item : lst) {
                if (item instanceof Map<?, ?> m) {
                    result.add((Map<String, Object>) m);
                }
            }
            return result;
        }
        return List.of();
    }

    private static class Telling {
        int nieuw = 0;
        int geupdatet = 0;
        int ongewijzigd = 0;
    }

    public record ImportResultaat(
            int leerlingenNieuw,
            int leerlingenGeupdatet,
            int leerlingenOngewijzigd,
            int leerkrachtenNieuw,
            int leerkrachtenGeupdatet,
            int leerkrachtenOngewijzigd,
            int aantalKlassen,
            // Set van smartschoolIds die OneRoster terugstuurde voor leerlingen.
            // Gebruikt door SchooljaarService om vertrokken leerlingen te detecteren.
            Set<String> gevondenLeerlingSmartschoolIds
    ) {}
}
