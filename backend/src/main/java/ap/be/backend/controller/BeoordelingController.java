package ap.be.backend.controller;

import java.util.List;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import ap.be.backend.dto.BeoordelingDTO;
import ap.be.backend.service.BeoordelingService;

@RestController
@CrossOrigin(origins = {"http://frontend.localhost", "http://localhost:4200"})
@RequestMapping("/api")
public class BeoordelingController {
    private final BeoordelingService beoordelingService;

    public BeoordelingController(BeoordelingService beoordelingService) {
        this.beoordelingService = beoordelingService;
    }

    @GetMapping("/books/{id}/reviews")
    public List<BeoordelingDTO> getReviewsByBookId(@PathVariable("id") Long id) {
        return beoordelingService.getReviewByBookId(id);
    }

    @PostMapping("/reviews")
    public BeoordelingDTO addReview(@RequestBody BeoordelingDTO beoordeling) {
        beoordelingService.beoordelingOpslaan(beoordeling);
        return beoordeling;
    }
}
