package ap.be.backend.controller;

import org.springframework.http.ResponseEntity;
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

    @PutMapping("/leenlimiet")
    public ResponseEntity<String> updateLeenLimiet(@RequestBody int nieuweLimiet) {
        if (nieuweLimiet < 0) {
            return ResponseEntity.badRequest().body("Limiet mag niet negatief zijn (gebruik 0 voor geen limiet).");
        }
        instellingService.updateLeenLimiet(nieuweLimiet);
        return ResponseEntity.ok("Leenlimiet succesvol geüpdatet naar " + nieuweLimiet);
    }
}
