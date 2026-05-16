package ap.be.backend.controller;

import ap.be.backend.dto.KlassLeeslijstRequestDTO;
import ap.be.backend.dto.KlassLeeslijstResponseDTO;
import ap.be.backend.dto.KoppelingRequestDTO;
import ap.be.backend.service.KlassLeeslijstService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Behandelt API-endpoints voor het beheren van klassen-leeslijsten.
 *
 * Leeskrijsten kunnen aangemaakt, gevuld en verwijderd worden door leerkrachten.
 * Een leeslijst kan optioneel gekoppeld zijn aan een klas of aan een
 * individuele leerling, en is pas zichtbaar voor leerlingen als 'zichtbaar' op
 * true staat. Write-operaties zijn beveiligd met @PreAuthorize.
 */
@RestController
@RequestMapping("/api/leeslijsten")
@RequiredArgsConstructor
public class KlassLeeslijstController {

    private final KlassLeeslijstService leeslijstService;

    /**
     * Geeft alle klassen-leeslijsten terug, van alle leerkrachten.
     */
    @GetMapping("/alle")
    public ResponseEntity<List<KlassLeeslijstResponseDTO>> getAllKlassenLeeslijsten() {
        return ResponseEntity.ok(leeslijstService.getAllKlassenLeeslijsten());
    }

    /**
     * Maakt een nieuwe leeslijst aan voor een leerkracht, eventueel gekoppeld
     * aan een klas of een individuele leerling.
     */
    @PreAuthorize("hasRole('TEACHER')")
    @PostMapping("/leerkracht/{leerkrachtId}")
    public ResponseEntity<KlassLeeslijstResponseDTO> createLeeslijst(
            @PathVariable Long leerkrachtId,
            @RequestBody KlassLeeslijstRequestDTO request) {

        KlassLeeslijstResponseDTO nieuweLijst = leeslijstService.createLeeslijst(request, leerkrachtId);
        return ResponseEntity.ok(nieuweLijst);
    }

    /**
     * Maakt een nieuwe leeslijst aan voor de huidige ingelogde leerkracht
     * (geen path-parameter nodig).
     */
    @PreAuthorize("hasRole('TEACHER')")
    @PostMapping
    public ResponseEntity<KlassLeeslijstResponseDTO> createLeeslijstVoorHuidigeLeerkracht(
            @RequestBody KlassLeeslijstRequestDTO request) {
        return ResponseEntity.ok(leeslijstService.createLeeslijstVoorHuidigeLeerkracht(request));
    }

    /**
     * Geeft alle leeslijsten van de huidige ingelogde leerkracht terug.
     */
    @PreAuthorize("hasRole('TEACHER')")
    @GetMapping("/mijn-leerkracht-lijsten")
    public ResponseEntity<List<KlassLeeslijstResponseDTO>> getLeeslijstenVoorHuidigeLeerkracht() {
        return ResponseEntity.ok(leeslijstService.getLeeslijstenVoorHuidigeLeerkracht());
    }

    /**
     * Geeft alle leeslijsten terug van een bepaalde leerkracht.
     */
    @GetMapping("/leerkracht/{leerkrachtId}")
    public ResponseEntity<List<KlassLeeslijstResponseDTO>> getLeeslijstenByLeerkracht(
            @PathVariable Long leerkrachtId) {

        List<KlassLeeslijstResponseDTO> lijsten = leeslijstService.getLeeslijstenByLeerkracht(leerkrachtId);
        return ResponseEntity.ok(lijsten);
    }

    /**
     * Geeft alle voor de huidige leerling zichtbare leeslijsten terug:
     * klas-leeslijsten van zijn klas + persoonlijk toegewezen leeslijsten.
     */
    @PreAuthorize("hasRole('STUDENT')")
    @GetMapping("/mijn-leerling-lijsten")
    public ResponseEntity<List<KlassLeeslijstResponseDTO>> getLeeslijstenVoorHuidigeLeerling() {
        return ResponseEntity.ok(leeslijstService.getLeeslijstenVoorHuidigeLeerling());
    }

    /**
     * Voegt een boek toe aan een bestaande leeslijst.
     */
    @PreAuthorize("hasRole('TEACHER')")
    @PostMapping("/{leeslijstId}/boeken/{boekId}")
    public ResponseEntity<KlassLeeslijstResponseDTO> addBoekToLeeslijst(
            @PathVariable Long leeslijstId,
            @PathVariable Long boekId) {

        KlassLeeslijstResponseDTO geupdateLijst = leeslijstService.addBoekToLeeslijst(leeslijstId, boekId);
        return ResponseEntity.ok(geupdateLijst);
    }

    /**
     * Geeft een leeslijst terug op basis van het ID.
     */
    @GetMapping("/{id}")
    public ResponseEntity<KlassLeeslijstResponseDTO> getLeeslijstById(@PathVariable Long id) {
        return ResponseEntity.ok(leeslijstService.getLeeslijstById(id));
    }

    /**
     * Verwijdert een boek uit een leeslijst.
     */
    @PreAuthorize("hasRole('TEACHER')")
    @DeleteMapping("/{leeslijstId}/boeken/{boekId}")
    public ResponseEntity<KlassLeeslijstResponseDTO> removeBoekFromLeeslijst(
            @PathVariable Long leeslijstId,
            @PathVariable Long boekId) {
        return ResponseEntity.ok(leeslijstService.removeBoekFromLeeslijst(leeslijstId, boekId));
    }

    /**
     * Wijzigt de naam van een bestaande leeslijst.
     */
    @PreAuthorize("hasRole('TEACHER')")
    @PutMapping("/{id}/naam")
    public ResponseEntity<KlassLeeslijstResponseDTO> updateLeeslijstNaam(
            @PathVariable Long id,
            @RequestBody String nieuweNaam) {
        return ResponseEntity.ok(leeslijstService.updateLeeslijstNaam(id, nieuweNaam));
    }

    /**
     * Wijzigt de beschrijving van een bestaande leeslijst.
     */
    @PreAuthorize("hasRole('TEACHER')")
    @PutMapping("/{id}/beschrijving")
    public ResponseEntity<KlassLeeslijstResponseDTO> updateBeschrijving(
            @PathVariable Long id,
            @RequestBody(required = false) String nieuweBeschrijving) {
        return ResponseEntity.ok(leeslijstService.updateBeschrijving(id, nieuweBeschrijving));
    }

    /**
     * Wijzigt de koppeling van een bestaande leeslijst (klas <-> leerling).
     */
    @PreAuthorize("hasRole('TEACHER')")
    @PutMapping("/{id}/koppeling")
    public ResponseEntity<KlassLeeslijstResponseDTO> updateKoppeling(
            @PathVariable Long id,
            @RequestBody KoppelingRequestDTO request) {
        return ResponseEntity.ok(leeslijstService.updateKoppeling(id, request.klas(), request.leerlingId()));
    }

    /**
     * Wisselt de zichtbaarheid van een leeslijst voor leerlingen.
     */
    @PreAuthorize("hasRole('TEACHER')")
    @PutMapping("/{id}/zichtbaar")
    public ResponseEntity<KlassLeeslijstResponseDTO> updateZichtbaar(
            @PathVariable Long id,
            @RequestBody boolean zichtbaar) {
        return ResponseEntity.ok(leeslijstService.updateZichtbaar(id, zichtbaar));
    }

    /**
     * Verwijdert een leeslijst volledig.
     */
    @PreAuthorize("hasRole('TEACHER')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteLeeslijst(@PathVariable Long id) {
        leeslijstService.deleteLeeslijst(id);
        return ResponseEntity.noContent().build();
    }
}
