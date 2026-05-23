package ap.be.backend.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import ap.be.backend.dto.CampusDTO;
import ap.be.backend.dto.SchoolDTO;
import ap.be.backend.dto.SchoolOnboardDTO;
import ap.be.backend.entity.BeschikbareSchool;
import ap.be.backend.entity.Campus;
import ap.be.backend.entity.School;
import ap.be.backend.mappers.SchoolMapper;
import ap.be.backend.entity.User;
import ap.be.backend.repository.BeschikbareSchoolRepository;
import ap.be.backend.repository.BoekRepository;
import ap.be.backend.repository.CampusRepository;
import ap.be.backend.repository.SchoolRepository;
import ap.be.backend.repository.UserRepository;
import ap.be.backend.service.OneRosterClient;
import ap.be.backend.service.OneRosterImportService;
import ap.be.backend.service.SchoolOnboardingService;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Behandelt API-endpoints voor het beheren van scholen in het systeem.
 */
@RestController
@RequestMapping("/api/scholen")
@PreAuthorize("hasRole('ADMIN')")
public class SchoolController {

    private final SchoolRepository schoolRepository;
    private final SchoolOnboardingService onboardingService;
    private final OneRosterClient oneRosterClient;
    private final OneRosterImportService importService;
    private final CampusRepository campusRepository;
    private final UserRepository userRepository;
    private final BoekRepository boekRepository;
    private final BeschikbareSchoolRepository beschikbareSchoolRepository;

    public SchoolController(SchoolRepository schoolRepository,
                            SchoolOnboardingService onboardingService,
                            OneRosterClient oneRosterClient,
                            OneRosterImportService importService,
                            CampusRepository campusRepository,
                            UserRepository userRepository,
                            BoekRepository boekRepository,
                            BeschikbareSchoolRepository beschikbareSchoolRepository) {
        this.schoolRepository = schoolRepository;
        this.onboardingService = onboardingService;
        this.oneRosterClient = oneRosterClient;
        this.importService = importService;
        this.campusRepository = campusRepository;
        this.userRepository = userRepository;
        this.boekRepository = boekRepository;
        this.beschikbareSchoolRepository = beschikbareSchoolRepository;
    }

    @GetMapping
    public List<SchoolDTO> getAlleScholen() {
        return schoolRepository.findAll().stream().map(SchoolMapper::toDTO).toList();
    }

    // --- BESCHIKBARE SCHOLEN (catalogus) ---

    @GetMapping("/beschikbaar")
    public List<BeschikbareSchool> getBeschikbareScholen() {
        List<String> geregistreerdeNamen = schoolRepository.findAll().stream()
                .map(School::getNaam).toList();
        return beschikbareSchoolRepository.findAll().stream()
                .filter(bs -> !geregistreerdeNamen.contains(bs.getNaam()))
                .toList();
    }

    @PostMapping("/beschikbaar")
    public ResponseEntity<?> voegBeschikbareSchoolToe(@RequestBody BeschikbareSchool school) {
        if (school.getNaam() == null || school.getNaam().isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Schoolnaam is verplicht"));
        }
        if (school.getAdres() == null || school.getAdres().isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Adres is verplicht"));
        }
        if (beschikbareSchoolRepository.existsByNaam(school.getNaam().trim())) {
            return ResponseEntity.badRequest().body(Map.of("error", "School bestaat al in de catalogus"));
        }
        school.setNaam(school.getNaam().trim());
        school.setAdres(school.getAdres().trim());
        return ResponseEntity.ok(beschikbareSchoolRepository.save(school));
    }

    @DeleteMapping("/beschikbaar/{id}")
    public ResponseEntity<Void> verwijderBeschikbareSchool(@PathVariable Long id) {
        if (!beschikbareSchoolRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        beschikbareSchoolRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    // --- WIZARD ENDPOINTS ---

    @PostMapping("/test-verbinding")
    public ResponseEntity<?> testVerbinding(@RequestBody SchoolOnboardDTO dto) {
        try {
            boolean ok = oneRosterClient.testVerbinding(
                    dto.getSmartschoolDomein(), dto.getOauthClientId(), dto.getOauthClientSecret());
            if (ok) {
                return ResponseEntity.ok(Map.of("success", true, "message", "Verbinding gelukt"));
            }
            return ResponseEntity.status(400).body(Map.of("success", false, "message", "Verbinding mislukt"));
        } catch (Exception e) {
            String detail = e.getClass().getSimpleName() + ": " + e.getMessage();
            return ResponseEntity.status(400).body(Map.of(
                    "success", false,
                    "message", "Verbinding mislukt",
                    "detail", detail
            ));
        }
    }

    @PostMapping("/preview")
    public ResponseEntity<?> previewImport(@RequestBody SchoolOnboardDTO dto) {
        Map<String, Object> data = oneRosterClient.haalLiveSchoolDataOp(
                dto.getSmartschoolDomein(), dto.getOauthClientId(), dto.getOauthClientSecret());
        List<?> klassen = (List<?>) data.getOrDefault("klassen", List.of());
        List<?> leerlingen = (List<?>) data.getOrDefault("leerlingen", List.of());
        List<?> leerkrachten = (List<?>) data.getOrDefault("leerkrachten", List.of());
        return ResponseEntity.ok(Map.of(
                "aantalKlassen", klassen.size(),
                "aantalGebruikers", leerlingen.size() + leerkrachten.size(),
                "aantalLeerlingen", leerlingen.size(),
                "aantalLeerkrachten", leerkrachten.size()
        ));
    }

    @PostMapping("/onboard")
    public ResponseEntity<?> onboardSchool(@RequestBody SchoolOnboardDTO dto) {
        try {
            School opgeslagen = onboardingService.onboardNieuweSchool(dto);
            return ResponseEntity.ok(Map.of(
                    "message", "School succesvol opgeslagen in de database",
                    "schoolId", opgeslagen.getId()
            ));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/{id}/beheerder")
    public ResponseEntity<?> duidBeheerderAan(@PathVariable Long id, @RequestBody SchoolOnboardDTO.BeheerderDTO dto) {
        try {
            var beheerder = onboardingService.duidBeheerderAan(id, dto);
            return ResponseEntity.ok(Map.of("id", beheerder.getId(), "gebruikersnaam", beheerder.getGebruikersnaam()));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // --- END WIZARD ENDPOINTS ---

    @PostMapping
    public ResponseEntity<SchoolDTO> maakSchoolAan(@RequestBody School nieuweSchool) {
        School opgeslagenSchool = schoolRepository.save(nieuweSchool);
        return ResponseEntity.ok(SchoolMapper.toDTO(opgeslagenSchool));
    }

    @GetMapping("/{id}/live-data")
    public ResponseEntity<?> getLiveSmartschoolData(@PathVariable Long id) {
        Optional<School> schoolOpt = schoolRepository.findById(id);

        if (schoolOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        School school = schoolOpt.get();
        Map<String, Object> liveData = oneRosterClient.haalLiveSchoolDataOp(
                school.getSmartschoolDomein(),
                school.getOauthClientId(),
                school.getOauthClientSecret());

        Map<String, Object> response = Map.of(
                "schoolInfo", Map.of(
                        "naam", school.getNaam(),
                        "adres", school.getAdres() == null ? "" : school.getAdres()),
                "liveSmartschoolData", liveData
        );

        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}/campussen")
    public ResponseEntity<?> getCampussen(@PathVariable Long id) {
        Optional<School> schoolOpt = schoolRepository.findById(id);
        if (schoolOpt.isEmpty()) return ResponseEntity.notFound().build();
        List<Map<String, Object>> result = schoolOpt.get().getCampussen().stream()
                .map(c -> Map.<String, Object>of("id", c.getId(), "naam", c.getNaam()))
                .toList();
        return ResponseEntity.ok(result);
    }

    @PostMapping("/{id}/campussen")
    public ResponseEntity<?> voegCampusToe(@PathVariable Long id, @RequestBody CampusDTO dto) {
        Optional<School> schoolOpt = schoolRepository.findById(id);
        if (schoolOpt.isEmpty()) return ResponseEntity.notFound().build();
        if (dto.getNaam() == null || dto.getNaam().isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Campusnaam is verplicht"));
        }
        School school = schoolOpt.get();
        Campus campus = new Campus();
        campus.setNaam(dto.getNaam().trim());
        campus.setSchool(school);
        Campus opgeslagen = campusRepository.save(campus);
        return ResponseEntity.ok(Map.of("id", opgeslagen.getId(), "naam", opgeslagen.getNaam()));
    }

    @PutMapping("/{id}/campussen/{campusId}")
    public ResponseEntity<?> hernoemCampus(@PathVariable Long id, @PathVariable Long campusId,
                                           @RequestBody CampusDTO dto) {
        Optional<School> schoolOpt = schoolRepository.findById(id);
        if (schoolOpt.isEmpty()) return ResponseEntity.notFound().build();
        if (dto.getNaam() == null || dto.getNaam().isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Campusnaam is verplicht"));
        }
        Optional<Campus> campusOpt = campusRepository.findById(campusId);
        if (campusOpt.isEmpty() || !campusOpt.get().getSchool().getId().equals(id)) {
            return ResponseEntity.notFound().build();
        }
        Campus campus = campusOpt.get();
        campus.setNaam(dto.getNaam().trim());
        Campus opgeslagen = campusRepository.save(campus);
        return ResponseEntity.ok(Map.of("id", opgeslagen.getId(), "naam", opgeslagen.getNaam()));
    }

    @DeleteMapping("/{id}/campussen/{campusId}")
    public ResponseEntity<?> verwijderCampus(@PathVariable Long id, @PathVariable Long campusId) {
        Optional<School> schoolOpt = schoolRepository.findById(id);
        if (schoolOpt.isEmpty()) return ResponseEntity.notFound().build();
        long aantalBoeken = boekRepository.countByCampus_Id(campusId);
        if (aantalBoeken > 0) {
            return ResponseEntity.status(409).body(Map.of(
                    "error", "Campus heeft nog " + aantalBoeken + " gekoppelde boek(en) en kan niet verwijderd worden",
                    "aantalBoeken", aantalBoeken));
        }
        School school = schoolOpt.get();
        boolean removed = school.getCampussen().removeIf(c -> c.getId().equals(campusId));
        if (!removed) return ResponseEntity.notFound().build();
        schoolRepository.save(school);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/sync-gebruikers")
    public ResponseEntity<?> syncGebruikers(@PathVariable Long id) {
        try {
            OneRosterImportService.ImportResultaat r = importService.importSchool(id);
            return ResponseEntity.ok(Map.of(
                    "message", "Sync voltooid",
                    "leerlingenNieuw", r.leerlingenNieuw(),
                    "leerlingenGeupdatet", r.leerlingenGeupdatet(),
                    "leerlingenOngewijzigd", r.leerlingenOngewijzigd(),
                    "leerkrachtenNieuw", r.leerkrachtenNieuw(),
                    "leerkrachtenGeupdatet", r.leerkrachtenGeupdatet(),
                    "leerkrachtenOngewijzigd", r.leerkrachtenOngewijzigd(),
                    "aantalKlassen", r.aantalKlassen()
            ));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(502).body(Map.of("error", "OneRoster onbereikbaar: " + e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    @org.springframework.transaction.annotation.Transactional
    public ResponseEntity<Void> verwijderSchool(@PathVariable Long id) {
        if (!schoolRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        List<User> gekoppeld = userRepository.findBySchool_Id(id);
        for (User u : gekoppeld) {
            u.setSchool(null);
        }
        userRepository.saveAll(gekoppeld);
        schoolRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
