package ap.be.backend.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import ap.be.backend.service.GebruikerService;
import ap.be.backend.dto.GebruikerDTO;
import ap.be.backend.dto.LeenStatusResponse;
import ap.be.backend.dto.LeeslijstMapDTO;
import ap.be.backend.dto.UitleningDTO;
import ap.be.backend.entity.Gebruiker;
import ap.be.backend.mappers.GebruikerMapper;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;

@RestController
@RequestMapping("/api/gebruiker")
// @CrossOrigin(origins = { "https://frontend.localhost",
// "http://localhost:4200", "https://frontend-sp09.cloud-ap.be" })
public class GebruikerController {
    private final GebruikerService gebruikerService;

    public GebruikerController(GebruikerService gebruikerService) {
        this.gebruikerService = gebruikerService;
    }

    // mappen ophalen
    @GetMapping("/{id}/mappen")
    public ResponseEntity<List<LeeslijstMapDTO>> getAlleMappenVanGebruiker(@PathVariable Long id) {
        return ResponseEntity.ok(gebruikerService.getMappenVoorGebruiker(id));
    }

    @PostMapping("/{gebruikerId}/mappen/{mapNaam}/boeken/{boekId}")
    public ResponseEntity<String> voegBoekToeAanLijst(@PathVariable Long gebruikerId,
            @PathVariable String mapNaam,
            @PathVariable Long boekId,
            @RequestParam(required = false, defaultValue = "📁") String emoji) {
        gebruikerService.voegBoekToeAanMap(gebruikerId, mapNaam, boekId, emoji);
        return ResponseEntity.ok("Boek succesvol toegevoegd aan map");
    }

    @DeleteMapping("/{gebruikerId}/mappen/{mapNaam}/boeken/{boekId}")
    public ResponseEntity<String> verwijderBoekUitLijst(
            @PathVariable Long gebruikerId,
            @PathVariable String mapNaam,
            @PathVariable Long boekId) {
        gebruikerService.verwijderBoekUitMap(gebruikerId, mapNaam, boekId);
        return ResponseEntity.ok("Boek succesvol verwijderd uit map");
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
        gebruikerService.bevestigUitlening(gebruikerId, boekId);
        return ResponseEntity.ok("Boek succesvol geleend! Je hebt 1 maand de tijd om het terug te brengen.");
    }

    @PostMapping("/{gebruikerId}/lenen/bulk")
    public ResponseEntity<String> leenBoekenBulk(
            @PathVariable Long gebruikerId,
            @RequestBody List<Long> boekIds) {

        gebruikerService.bevestigBulkUitlening(gebruikerId, boekIds);
        return ResponseEntity
                .ok(boekIds.size() + " boeken succesvol geleend! Je hebt 1 maand de tijd om ze terug te brengen.");
    }

    @PreAuthorize("hasAnyRole('TEACHER', 'BEHEERDER')")
    @GetMapping("/aanvragen")
    public ResponseEntity<List<UitleningDTO>> getAlleAanvragen() {
        return ResponseEntity.ok(gebruikerService.getAlleAanvragenDTO());
    }

    @PreAuthorize("hasAnyRole('TEACHER', 'BEHEERDER')")
    @PutMapping("/aanvragen/{uitleningId}/goedkeuren")
    public ResponseEntity<String> keurGoed(@PathVariable Long uitleningId) {
        gebruikerService.keurAanvraagGoed(uitleningId);
        return ResponseEntity.ok("Aanvraag goedgekeurd. Uitleentermijn is gestart!");
    }

    @PreAuthorize("hasAnyRole('TEACHER', 'BEHEERDER')")
    @PutMapping("/aanvragen/{uitleningId}/weigeren")
    public ResponseEntity<String> weiger(@PathVariable Long uitleningId) {
        gebruikerService.weigerAanvraag(uitleningId);
        return ResponseEntity.ok("Aanvraag geweigerd en boek is teruggezet in de voorraad.");
    }

    @GetMapping("/{gebruikerId}/leningen")
    public ResponseEntity<List<UitleningDTO>> getActieveLeningen(@PathVariable Long gebruikerId) {
        return ResponseEntity.ok(gebruikerService.getActieveLeningenVanGebruikerDTO(gebruikerId));
    }

    @DeleteMapping("/leningen/{uitleningId}")
    public ResponseEntity<String> verwijderLening(@PathVariable Long uitleningId) {
        // execute the delete action and return a success message
        gebruikerService.verwijderLening(uitleningId);
        return ResponseEntity.ok("Lening succesvol verwijderd en voorraad hersteld.");
    }

    @PatchMapping("/leningen/{uitleningId}/verlengen")
    @CrossOrigin(origins = { "http://localhost:4200", "https://localhost:4200",
            "https://frontend.localhost", "https://frontend-sp09.cloud-ap.be",
            "https://api-sp09.cloud-ap.be" }, allowedHeaders = "*", allowCredentials = "true")

    public ResponseEntity<String> verlengLening(@PathVariable("uitleningId") Long uitleningId) {
        gebruikerService.verlengUitlening(uitleningId);
        return ResponseEntity.ok("Uitleentermijn succesvol verlengd met 14 dagen.");
    }

    @GetMapping("/{gebruikerId}/geschiedenis")
    public ResponseEntity<List<UitleningDTO>> getLeengeschiedenis(@PathVariable Long gebruikerId) {
        return ResponseEntity.ok(gebruikerService.getLeengeschiedenisDTO(gebruikerId));
    }

    @GetMapping("/schoolid/{schoolId}")
    public ResponseEntity<GebruikerDTO> getLeerlingBySchoolId(@PathVariable long schoolId) {
        Gebruiker leerling = gebruikerService.getLeerlingBySchoolId(schoolId);
        return ResponseEntity.ok(GebruikerMapper.toDTO(leerling));
    }

}
