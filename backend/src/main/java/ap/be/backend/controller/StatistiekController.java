package ap.be.backend.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

import ap.be.backend.dto.AdminStatistiekDTO;
import ap.be.backend.dto.LeerkrachtStatistiekDTO;
import ap.be.backend.dto.LeerlingStatistiekDTO;
import ap.be.backend.service.StatistiekService;

@RestController
@RequestMapping("/api/statistieken")
public class StatistiekController {
    private final StatistiekService statistiekService;

    public StatistiekController(StatistiekService statistiekService) {
        this.statistiekService = statistiekService;
    }

    @PreAuthorize("hasRole('BEHEERDER')")
    @GetMapping("/admin")
    public ResponseEntity<AdminStatistiekDTO> getAdminStatistieken() {
        return ResponseEntity.ok(statistiekService.getAdminStatistieken());
    }

    @PreAuthorize("hasRole('TEACHER')")
    @GetMapping("/leerkracht")
    public ResponseEntity<LeerkrachtStatistiekDTO> getLeerkrachtStatistieken(@RequestParam(required = false) String klasId) {
        return ResponseEntity.ok(statistiekService.getLeerkrachtStatistieken(klasId));
    }

    @GetMapping("/leerling/{schoolId}")
    public ResponseEntity<LeerlingStatistiekDTO> getLeerlingStatistieken(@PathVariable String schoolId) {
        return ResponseEntity.ok(statistiekService.getLeerlingStatistieken(schoolId));
    }

    @PutMapping("/leerling/{schoolId}/leesdoel")
    public ResponseEntity<LeerlingStatistiekDTO> updateLeesdoel(@PathVariable String schoolId,
                                                                @RequestBody Map<String, Integer> body) {
        Integer waarde = body == null ? null : body.get("leesdoel");
        if (waarde == null) {
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.ok(statistiekService.updateLeesdoel(schoolId, waarde));
    }
}
