package ap.be.backend.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import ap.be.backend.repository.LeeslijstMapRepository;
import ap.be.backend.service.GebruikerService;
import ap.be.backend.dto.LeenStatusResponse;
import ap.be.backend.entity.LeeslijstMap;
import ap.be.backend.entity.Uitlening;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;

@RestController
@RequestMapping("/api/gebruiker")
// @CrossOrigin(origins = { "http://frontend.localhost", "http://localhost:4200", "http://frontend-sp09.cloud-ap.be" })
public class GebruikerController {
    private final LeeslijstMapRepository leeslijstMapRepository;
    private final GebruikerService gebruikerService;

    public GebruikerController(LeeslijstMapRepository leeslijstMapRepository, GebruikerService gebruikerService) {
        this.leeslijstMapRepository = leeslijstMapRepository;
        this.gebruikerService = gebruikerService;
    }

    // mappen ophalen
    @GetMapping("/{id}/mappen")
    public ResponseEntity<List<LeeslijstMap>> getAlleMappenVanGebruiker(@PathVariable Long id) {
        List<LeeslijstMap> mappen = leeslijstMapRepository.findByGebruiker_GebruikerID(id);

        return ResponseEntity.ok(mappen);
    }

    @PostMapping("/{gebruikerId}/mappen/{mapNaam}/boeken/{boekId}")
    public ResponseEntity<String> voegBoekToeAanLijst(@PathVariable Long gebruikerId,
            @PathVariable String mapNaam,
            @PathVariable Long boekId) {
        try {
            gebruikerService.voegBoekToeAanMap(gebruikerId, mapNaam, boekId);
            return ResponseEntity.ok("Boek succesvol toegevoegd aan map");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @DeleteMapping("/{gebruikerId}/mappen/{mapNaam}/boeken/{boekId}")
    public ResponseEntity<String> verwijderBoekUitLijst(
            @PathVariable Long gebruikerId,
            @PathVariable String mapNaam,
            @PathVariable Long boekId) {
        try {
            gebruikerService.verwijderBoekUitMap(gebruikerId, mapNaam, boekId);
            return ResponseEntity.ok("Boek succesvol verwijderd uit map");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/{gebruikerId}/lenen/{boekId}/status")
    public ResponseEntity<LeenStatusResponse> checkLeenStatus(
            @PathVariable Long gebruikerId,
            @PathVariable Long boekId) {

        LeenStatusResponse status = gebruikerService.checkLeenStatus(gebruikerId, boekId);
        return ResponseEntity.ok(status);
    }

    @PostMapping("/{gebruikerId}/lenen/{boekId}")
    public ResponseEntity<String> leenBoek(
            @PathVariable Long gebruikerId,
            @PathVariable Long boekId) {
        try {
            gebruikerService.bevestigUitlening(gebruikerId, boekId);
            return ResponseEntity.ok("Boek succesvol geleend! Je hebt 1 maand de tijd om het terug te brengen.");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/aanvragen")
    public ResponseEntity<List<Uitlening>> getAlleAanvragen() {
        return ResponseEntity.ok(gebruikerService.getAlleAanvragen());
    }

    @PutMapping("/aanvragen/{uitleningId}/goedkeuren")
    public ResponseEntity<String> keurGoed(@PathVariable Long uitleningId) {
        gebruikerService.keurAanvraagGoed(uitleningId);
        return ResponseEntity.ok("Aanvraag goedgekeurd. Uitleentermijn is gestart!");
    }

    @PutMapping("/aanvragen/{uitleningId}/weigeren")
    public ResponseEntity<String> weiger(@PathVariable Long uitleningId) {
        gebruikerService.weigerAanvraag(uitleningId);
        return ResponseEntity.ok("Aanvraag geweigerd en boek is teruggezet in de voorraad.");
    }
}
