package ap.be.backend.controller;

import ap.be.backend.dto.KlassLeeslijstRequestDTO;
import ap.be.backend.dto.KlassLeeslijstResponseDTO;
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
 * Leerlingen en beheerders kunnen leeslijsten raadplegen. Write-operaties zijn
 * beveiligd met @PreAuthorize("hasRole('TEACHER')").
 */
@RestController
@RequestMapping("/api/leeslijsten")
@RequiredArgsConstructor
public class KlassLeeslijstController {

    private final KlassLeeslijstService leeslijstService;

    /**
     * Geeft alle klassen-leeslijsten terug, van alle leerkrachten.
     *
     * @return lijst van alle leeslijsten als DTO's
     */
    @GetMapping("/alle")
    public ResponseEntity<List<KlassLeeslijstResponseDTO>> getAllKlassenLeeslijsten() {
        return ResponseEntity.ok(leeslijstService.getAllKlassenLeeslijsten());
    }

    /**
     * Maakt een nieuwe leeslijst aan voor een leerkracht.
     *
     * @param leerkrachtId het ID van de leerkracht die de lijst aanmaakt
     * @param request      de request-body met de naam van de nieuwe leeslijst
     * @return de aangemaakte leeslijst als DTO
     */
    @PreAuthorize("hasRole('TEACHER')")
    @PostMapping("/leerkracht/{leerkrachtId}")
    public ResponseEntity<KlassLeeslijstResponseDTO> createLeeslijst(
            @PathVariable Long leerkrachtId,
            @RequestBody KlassLeeslijstRequestDTO request) {

        KlassLeeslijstResponseDTO nieuweLijst = leeslijstService.createLeeslijst(request.naam(), leerkrachtId);
        return ResponseEntity.ok(nieuweLijst);
    }

    /**
     * Geeft alle leeslijsten terug van een bepaalde leerkracht.
     *
     * @param leerkrachtId het ID van de leerkracht
     * @return lijst van leeslijsten als DTO's
     */
    @GetMapping("/leerkracht/{leerkrachtId}")
    public ResponseEntity<List<KlassLeeslijstResponseDTO>> getLeeslijstenByLeerkracht(
            @PathVariable Long leerkrachtId) {

        List<KlassLeeslijstResponseDTO> lijsten = leeslijstService.getLeeslijstenByLeerkracht(leerkrachtId);
        return ResponseEntity.ok(lijsten);
    }

    /**
     * Voegt een boek toe aan een bestaande leeslijst.
     *
     * @param leeslijstId het ID van de leeslijst
     * @param boekId      het ID van het toe te voegen boek
     * @return de bijgewerkte leeslijst als DTO
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
     *
     * @param id het ID van de leeslijst
     * @return de leeslijst als DTO
     */
    @GetMapping("/{id}")
    public ResponseEntity<KlassLeeslijstResponseDTO> getLeeslijstById(@PathVariable Long id) {
        return ResponseEntity.ok(leeslijstService.getLeeslijstById(id));
    }

    /**
     * Verwijdert een boek uit een leeslijst.
     *
     * @param leeslijstId het ID van de leeslijst
     * @param boekId      het ID van het te verwijderen boek
     * @return de bijgewerkte leeslijst als DTO
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
     *
     * @param id         het ID van de leeslijst
     * @param nieuweNaam de nieuwe naam
     * @return de bijgewerkte leeslijst als DTO
     */
    @PreAuthorize("hasRole('TEACHER')")
    @PutMapping("/{id}/naam")
    public ResponseEntity<KlassLeeslijstResponseDTO> updateLeeslijstNaam(
            @PathVariable Long id,
            @RequestBody String nieuweNaam) {
        return ResponseEntity.ok(leeslijstService.updateLeeslijstNaam(id, nieuweNaam));
    }

    /**
     * Verwijdert een leeslijst volledig.
     *
     * @param id het ID van de te verwijderen leeslijst
     * @return 204 No Content bij succes
     */
    @PreAuthorize("hasRole('TEACHER')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteLeeslijst(@PathVariable Long id) {
        leeslijstService.deleteLeeslijst(id);
        return ResponseEntity.noContent().build();
    }
}
