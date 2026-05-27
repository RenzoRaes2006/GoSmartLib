package ap.be.backend.controller;

import ap.be.backend.entity.School;
import ap.be.backend.exception.SchoolNotFoundException;
import ap.be.backend.repository.SchoolRepository;
import ap.be.backend.service.notificatie.NotificatieTestService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/notificatie")
public class NotificatieController {

    private final NotificatieTestService testService;
    private final SchoolRepository schoolRepository;

    public NotificatieController(NotificatieTestService testService, SchoolRepository schoolRepository) {
        this.testService = testService;
        this.schoolRepository = schoolRepository;
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/test-herinnering")
    public ResponseEntity<NotificatieTestService.TestResultaat> stuurTestHerinnering(@RequestBody TestRequest req) {
        if (req == null || req.schoolId() == null) {
            return ResponseEntity.badRequest().body(NotificatieTestService.TestResultaat.fout("schoolId is verplicht"));
        }
        School school = schoolRepository.findById(req.schoolId())
                .orElseThrow(() -> new SchoolNotFoundException(req.schoolId()));
        NotificatieTestService.TestResultaat resultaat =
                testService.stuurTestHerinnering(school, req.ontvangerSmartschoolId(), req.boekTitel());
        return ResponseEntity.ok(resultaat);
    }

    public record TestRequest(Long schoolId, String ontvangerSmartschoolId, String boekTitel) {}
}
