package ap.be.backend.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import ap.be.backend.config.SecurityUtils;
import ap.be.backend.dto.BeoordelingDTO;
import ap.be.backend.entity.User;
import ap.be.backend.repository.UserRepository;
import ap.be.backend.service.BeoordelingService;
import ap.be.backend.service.GerapporteerdeBeoordelingService;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api")
public class BeoordelingController {
    private final BeoordelingService beoordelingService;
    private final GerapporteerdeBeoordelingService gerapporteerdeBeoordelingService;
    private final UserRepository userRepository;

    public BeoordelingController(BeoordelingService beoordelingService,
            GerapporteerdeBeoordelingService gerapporteerdeBeoordelingService,
            UserRepository userRepository) {
        this.beoordelingService = beoordelingService;
        this.gerapporteerdeBeoordelingService = gerapporteerdeBeoordelingService;
        this.userRepository = userRepository;
    }

    @GetMapping("/books/{id}/reviews")
    public List<BeoordelingDTO> getBeoordelingenOpBoekId(@PathVariable("id") Long id) {
        return beoordelingService.getBeoordelingenOpBoekId(id);
    }

    @PostMapping("/reviews")
    public BeoordelingDTO beoordelingOpslaan(@Valid @RequestBody BeoordelingDTO beoordeling) {
        beoordelingService.beoordelingOpslaan(beoordeling);
        return beoordeling;
    }

    @PostMapping("/reviews/{id}/report")
    public void rapporteerBeoordeling(@PathVariable("id") Long beoordelingId, @RequestBody String reden) {
        User melder = SecurityUtils.getCurrentUser(userRepository);
        gerapporteerdeBeoordelingService.rapporteerBeoordeling(beoordelingId, melder.getId(), reden);
    }

    @DeleteMapping("/reviews/{id}")
    public ResponseEntity<Void> verwijderBeoordeling(@PathVariable("id") Long beoordelingId) {
        beoordelingService.verwijderBeoordeling(beoordelingId);
        return ResponseEntity.noContent().build();
    }
}
