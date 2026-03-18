package ap.be.backend.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import ap.be.backend.dto.BeoordelingDTO;
import ap.be.backend.service.BeoordelingService;
import ap.be.backend.service.GerapporteerdeBeoordelingService;
import jakarta.validation.Valid;

@RestController
// @CrossOrigin(origins = {"http://frontend.localhost", "http://localhost:4200"})
@RequestMapping("/api")
public class BeoordelingController {
    private final BeoordelingService beoordelingService;
    private final GerapporteerdeBeoordelingService gerapporteerdeBeoordelingService;

    public BeoordelingController(BeoordelingService beoordelingService, GerapporteerdeBeoordelingService gerapporteerdeBeoordelingService) {
        this.beoordelingService = beoordelingService;
        this.gerapporteerdeBeoordelingService = gerapporteerdeBeoordelingService;
    }

    @GetMapping("/books/{id}/reviews")
    public List<BeoordelingDTO> getBeoordelingenOpBoekId(@PathVariable("id") Long id) {
        return beoordelingService.getBeoordelingenOpBoekId(id);
    }

    @PostMapping("/reviews") // dat moet verbeter worden, want er moet ook een boekId meegegeven worden. dan in de service moet dan ook een boek opgehaald worden en dan de beoordeling aan dat boek koppelen
    public BeoordelingDTO beoordelingOpslaan(@Valid @RequestBody BeoordelingDTO beoordeling) {
        beoordelingService.beoordelingOpslaan(beoordeling);
        return beoordeling;
    }

    @PostMapping("/reviews/{id}/report")
    public void rapporteerBeoordeling(@PathVariable("id") Long beoordelingId, @RequestBody String reden) {
        Long tijdelijkeGebruikerId = 1L; // tijdelijke gebruiker ID, dit moet vervangen worden door de daadwerkelijke gebruiker ID uit de authenticatie context
        gerapporteerdeBeoordelingService.rapporteerBeoordeling(beoordelingId, tijdelijkeGebruikerId, reden);
    }
}
