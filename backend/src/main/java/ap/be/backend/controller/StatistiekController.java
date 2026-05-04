package ap.be.backend.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import ap.be.backend.dto.AdminStatistiekDTO;
import ap.be.backend.dto.LeerkrachtStatistiekDTO;
import ap.be.backend.dto.LeerlingStatistiekDTO;
import ap.be.backend.service.StatistiekService;

@RestController
@RequestMapping("/api/statistieken")
@CrossOrigin(origins = "*", allowedHeaders = "*")
public class StatistiekController {
    private final StatistiekService statistiekService;

    public StatistiekController(StatistiekService statistiekService) {
        this.statistiekService = statistiekService;
    }

    @GetMapping("/admin")
    public ResponseEntity<AdminStatistiekDTO> getAdminStatistieken() {
        return ResponseEntity.ok(statistiekService.getAdminStatistieken());
    }

    @GetMapping("/leerkracht")
    public ResponseEntity<LeerkrachtStatistiekDTO> getLeerkrachtStatistieken(@RequestParam(required = false) String klasId) {
        return ResponseEntity.ok(statistiekService.getLeerkrachtStatistieken(klasId));
    }

    @GetMapping("/leerling/{schoolId}")
    public ResponseEntity<LeerlingStatistiekDTO> getLeerlingStatistieken(@PathVariable String schoolId) {
        return ResponseEntity.ok(statistiekService.getLeerlingStatistieken(schoolId));
    }
}
