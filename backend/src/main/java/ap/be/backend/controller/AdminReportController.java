package ap.be.backend.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import ap.be.backend.dto.GerapporteerdeBeoordelingAdminDTO;
import ap.be.backend.service.GerapporteerdeBeoordelingService;

@RestController
@RequestMapping("/api")
public class AdminReportController {
    private final GerapporteerdeBeoordelingService gerapporteerdeBeoordelingService;

    public AdminReportController(GerapporteerdeBeoordelingService gerapporteerdeBeoordelingService) {
        this.gerapporteerdeBeoordelingService = gerapporteerdeBeoordelingService;
    }

    @GetMapping("/reports")
    public ResponseEntity<List<GerapporteerdeBeoordelingAdminDTO>> getAlleRapportages() {
        List<GerapporteerdeBeoordelingAdminDTO> rapportages = gerapporteerdeBeoordelingService
                .getAlleGerapporteerdeBeoordelingen();
        return ResponseEntity.ok(rapportages);
    }

    @DeleteMapping("/reports/{id}")
    public ResponseEntity<Void> negeerRapportage(@PathVariable("id") Long rapportageId) {
        gerapporteerdeBeoordelingService.verwijderRapportage(rapportageId);
        return ResponseEntity.noContent().build();
    }
}
