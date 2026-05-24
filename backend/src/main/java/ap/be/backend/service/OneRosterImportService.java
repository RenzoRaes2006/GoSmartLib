package ap.be.backend.service;

import ap.be.backend.dto.SyncPreviewDTO;
import ap.be.backend.entity.Rol;
import ap.be.backend.entity.School;
import ap.be.backend.entity.User;
import ap.be.backend.repository.SchoolRepository;
import ap.be.backend.repository.UitleningRepository;
import ap.be.backend.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
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
    private final UitleningRepository uitleningRepository;

    public OneRosterImportService(SchoolRepository schoolRepository,
                                  UserRepository userRepository,
                                  OneRosterClient oneRosterClient,
                                  UitleningRepository uitleningRepository) {
        this.schoolRepository = schoolRepository;
        this.userRepository = userRepository;
        this.oneRosterClient = oneRosterClient;
        this.uitleningRepository = uitleningRepository;
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

    /**
     * Berekent (zonder iets op te slaan) wat een handmatige sync zou veranderen:
     * nieuwe / verplaatste / vertrokken leerlingen + leerkrachten. Vertrokken
     * leerlingen met openstaande uitleningen worden apart geteld zodat de UI
     * kan waarschuwen dat die niet automatisch worden gedeactiveerd.
     *
     * Bewust geen namen of andere PII in het resultaat — enkel smartschoolId
     * + klas zoals die in de DB worden bewaard.
     */
    @Transactional(readOnly = true)
    public SyncPreviewDTO previewSync(Long schoolId) {
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

        List<Map<String, Object>> oneRosterLeerlingen = castLijst(data.get("leerlingen"));
        List<Map<String, Object>> oneRosterLeerkrachten = castLijst(data.get("leerkrachten"));
        int aantalKlassen = castLijst(data.get("klassen")).size();

        SectiePreview leerlingPreview = berekenSectiePreview(oneRosterLeerlingen, Rol.LEERLING, school);
        SectiePreview leerkrachtPreview = berekenSectiePreview(oneRosterLeerkrachten, Rol.LEERKRACHT, school);

        // Vertrokken leerlingen = actief in DB voor deze school maar niet in OneRoster
        Set<String> gevondenLeerlingIds = extractSmartschoolIds(oneRosterLeerlingen);
        List<SyncPreviewDTO.VertrokkenEntry> vertrokken = new ArrayList<>();
        int aantalMetOpenLeningen = 0;
        if (!gevondenLeerlingIds.isEmpty()) {
            List<User> vertrokkenUsers = userRepository.findVertrokkenLeerlingen(schoolId, gevondenLeerlingIds);
            for (User u : vertrokkenUsers) {
                int openLeningen = uitleningRepository.findByUser_IdAndIsIngeleverdFalse(u.getId()).size();
                if (openLeningen > 0) aantalMetOpenLeningen++;
                vertrokken.add(new SyncPreviewDTO.VertrokkenEntry(
                        u.getSmartschoolId(),
                        u.getKlas() == null ? "" : u.getKlas(),
                        openLeningen));
            }
        }

        return new SyncPreviewDTO(
                new SyncPreviewDTO.LeerlingSectie(
                        leerlingPreview.nieuw, leerlingPreview.verplaatst,
                        vertrokken, leerlingPreview.ongewijzigd),
                new SyncPreviewDTO.LeerkrachtSectie(
                        leerkrachtPreview.nieuw, leerkrachtPreview.verplaatst,
                        leerkrachtPreview.ongewijzigd),
                aantalKlassen,
                aantalMetOpenLeningen);
    }

    /**
     * Voert een handmatige sync uit: importeert nieuwe/verplaatste gebruikers
     * (via {@link #importSchool}) en deactiveert vervolgens leerlingen die
     * niet meer in OneRoster staan. Leerlingen met open uitleningen blijven
     * actief — de beheerder kan die later handmatig afhandelen.
     */
    @Transactional
    public ManualSyncResultaat synchronizeManually(Long schoolId) {
        ImportResultaat importResultaat = importSchool(schoolId);

        Set<String> gevondenIds = importResultaat.gevondenLeerlingSmartschoolIds();
        if (gevondenIds.isEmpty()) {
            throw new IllegalStateException(
                    "OneRoster stuurde geen leerlingen terug. Sync geannuleerd om te vermijden " +
                    "dat alle leerlingen als vertrokken worden gemarkeerd.");
        }

        List<User> vertrokkenKandidaten = userRepository.findVertrokkenLeerlingen(schoolId, gevondenIds);
        List<User> teMarkeren = new ArrayList<>();
        int geblokkeerd = 0;
        LocalDate vandaag = LocalDate.now();
        for (User u : vertrokkenKandidaten) {
            boolean heeftOpenLeningen = !uitleningRepository.findByUser_IdAndIsIngeleverdFalse(u.getId()).isEmpty();
            if (heeftOpenLeningen) {
                geblokkeerd++;
                continue;
            }
            u.setActief(false);
            u.setVertrokkenOp(vandaag);
            teMarkeren.add(u);
        }
        if (!teMarkeren.isEmpty()) {
            userRepository.saveAll(teMarkeren);
        }
        log.info("Handmatige sync school {}: {} leerlingen vertrokken gemarkeerd, {} geblokkeerd door open uitleningen",
                schoolId, teMarkeren.size(), geblokkeerd);

        return new ManualSyncResultaat(importResultaat, teMarkeren.size(), geblokkeerd);
    }

    private SectiePreview berekenSectiePreview(List<Map<String, Object>> bron, Rol rol, School school) {
        List<SyncPreviewDTO.NieuweEntry> nieuw = new ArrayList<>();
        List<SyncPreviewDTO.VerplaatstEntry> verplaatst = new ArrayList<>();
        int ongewijzigd = 0;
        for (Map<String, Object> rij : bron) {
            String smartschoolId = String.valueOf(rij.get("id"));
            if (smartschoolId == null || smartschoolId.isBlank() || "null".equals(smartschoolId)) continue;
            String klas = (String) rij.getOrDefault("klas", "");

            Optional<User> bestaand = userRepository.findBySmartschoolId(smartschoolId);
            if (bestaand.isEmpty()) {
                nieuw.add(new SyncPreviewDTO.NieuweEntry(smartschoolId, klas));
            } else {
                User u = bestaand.get();
                boolean klasGewijzigd = !Objects.equals(u.getKlas(), klas);
                boolean rolGewijzigd = u.getRol() != rol;
                boolean schoolGewijzigd = u.getSchool() == null
                        || !Objects.equals(u.getSchool().getId(), school.getId());
                if (klasGewijzigd || rolGewijzigd || schoolGewijzigd) {
                    verplaatst.add(new SyncPreviewDTO.VerplaatstEntry(
                            smartschoolId,
                            u.getKlas() == null ? "" : u.getKlas(),
                            klas));
                } else {
                    ongewijzigd++;
                }
            }
        }
        return new SectiePreview(nieuw, verplaatst, ongewijzigd);
    }

    private record SectiePreview(
            List<SyncPreviewDTO.NieuweEntry> nieuw,
            List<SyncPreviewDTO.VerplaatstEntry> verplaatst,
            int ongewijzigd) {}

    public record ManualSyncResultaat(
            ImportResultaat importResultaat,
            int vertrokkenGemarkeerd,
            int vertrokkenGeblokkeerd) {}

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
