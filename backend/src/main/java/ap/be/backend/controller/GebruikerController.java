package ap.be.backend.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import ap.be.backend.dto.GebruikerDTO;
import ap.be.backend.service.GebruikerService;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@RestController
@RequestMapping("/api/gebruiker")
@CrossOrigin(origins = { "http://frontend.localhost", "http://localhost:4200" })
public class GebruikerController {
    private final GebruikerService gebruikerService;

    public GebruikerController(GebruikerService gebruikerService) {
        this.gebruikerService = gebruikerService;
    }

    @GetMapping("/{id}")
    public ResponseEntity<GebruikerDTO> getGebruiker(@PathVariable Long id) {
        try {
            var gebruiker = gebruikerService.getGebruikerById(id);
            return ResponseEntity.ok(gebruikerService.mapToDTO(gebruiker));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }
}
