package ap.be.backend.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import ap.be.backend.dto.CampusDTO;
import ap.be.backend.dto.SchoolDTO;
import ap.be.backend.dto.SchoolOnboardDTO;
import ap.be.backend.entity.Campus;
import ap.be.backend.entity.School;
import ap.be.backend.mappers.SchoolMapper;
import ap.be.backend.repository.CampusRepository;
import ap.be.backend.repository.SchoolRepository;
import ap.be.backend.service.OneRosterClient;
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
    private final CampusRepository campusRepository;

    public SchoolController(SchoolRepository schoolRepository,
                            SchoolOnboardingService onboardingService,
                            OneRosterClient oneRosterClient,
                            CampusRepository campusRepository) {
        this.schoolRepository = schoolRepository;
        this.onboardingService = onboardingService;
        this.oneRosterClient = oneRosterClient;
        this.campusRepository = campusRepository;
    }

    @GetMapping
    public List<SchoolDTO> getAlleScholen() {
        return schoolRepository.findAll().stream().map(SchoolMapper::toDTO).toList();
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
                "aantalLeerkrachten", leerkrachten.size(),
                "klassen", klassen,
                "leerlingen", leerlingen,
                "leerkrachten", leerkrachten
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
        school.getCampussen().add(campus);
        schoolRepository.save(school);
        return ResponseEntity.ok(Map.of("id", campus.getId(), "naam", campus.getNaam()));
    }

    @DeleteMapping("/{id}/campussen/{campusId}")
    public ResponseEntity<?> verwijderCampus(@PathVariable Long id, @PathVariable Long campusId) {
        Optional<School> schoolOpt = schoolRepository.findById(id);
        if (schoolOpt.isEmpty()) return ResponseEntity.notFound().build();
        School school = schoolOpt.get();
        boolean removed = school.getCampussen().removeIf(c -> c.getId().equals(campusId));
        if (!removed) return ResponseEntity.notFound().build();
        schoolRepository.save(school);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> verwijderSchool(@PathVariable Long id) {
        if (!schoolRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        schoolRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
