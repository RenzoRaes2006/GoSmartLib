package ap.be.backend.controller;

import java.util.Set;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import ap.be.backend.Repos.GebruikerRepository;
import ap.be.backend.entity.Boek;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@RestController
@RequestMapping("/api/gebruiker")
@CrossOrigin(origins = { "http://frontend.localhost", "http://localhost:4200" })
public class GebruikerController {
    private final GebruikerRepository gebruikerRepository;

    public GebruikerController(GebruikerRepository gebruikerRepository) {
        this.gebruikerRepository = gebruikerRepository;
    }

    @GetMapping("/{id}/leeslijst")
    public ResponseEntity<Set<Boek>> getLeeslijst(@PathVariable Long id) {
        return gebruikerRepository.findById(id)
                .map(gebruiker -> ResponseEntity.ok(gebruiker.getLeeslijst()))
                .orElse(ResponseEntity.notFound().build());
    }

}
