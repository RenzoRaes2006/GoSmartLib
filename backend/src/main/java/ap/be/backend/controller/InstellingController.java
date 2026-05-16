package ap.be.backend.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import ap.be.backend.service.InstellingService;

@RestController
@RequestMapping("/api/instellingen")
public class InstellingController {
    private final InstellingService instellingService;

    public InstellingController(InstellingService instellingService) {
        this.instellingService = instellingService;
    }

    @GetMapping("/leenlimiet")
    public ResponseEntity<Integer> getLeenLimiet() {
        return ResponseEntity.ok(instellingService.getLeenLimiet());
    }

    @PreAuthorize("hasRole('BEHEERDER')")
    @PutMapping("/leenlimiet")
    public ResponseEntity<String> updateLeenLimiet(@RequestBody int nieuweLimiet) {
        if (nieuweLimiet < 0) {
            return ResponseEntity.badRequest().body("Limiet mag niet negatief zijn (gebruik 0 voor geen limiet).");
        }
        instellingService.updateLeenLimiet(nieuweLimiet);
        return ResponseEntity.ok("Leenlimiet succesvol geüpdatet naar " + nieuweLimiet);
    }

    @GetMapping("/inlevertermijn")
    public ResponseEntity<Integer> getInlevertermijn() {
        return ResponseEntity.ok(instellingService.getInlevertermijnDagen());
    }

    @PreAuthorize("hasRole('BEHEERDER')")
    @PutMapping("/inlevertermijn")
    public ResponseEntity<String> updateInlevertermijn(@RequestBody int dagen) {
        if (dagen < 1 || dagen > 365) {
            return ResponseEntity.badRequest().body("Inlevertermijn moet tussen 1 en 365 dagen liggen.");
        }
        instellingService.updateInlevertermijnDagen(dagen);
        return ResponseEntity.ok("Standaard inlevertermijn succesvol geüpdatet naar " + dagen + " dagen.");
    }
}
