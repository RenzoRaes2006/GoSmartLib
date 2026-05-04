package ap.be.backend.controller;

import ap.be.backend.dto.KlassLeeslijstRequestDTO;
import ap.be.backend.dto.KlassLeeslijstResponseDTO;
import ap.be.backend.service.KlassLeeslijstService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/leeslijsten")
@RequiredArgsConstructor
public class KlassLeeslijstController {

    private final KlassLeeslijstService leeslijstService;

    /**
     * Maakt een nieuwe leeslijst aan voor een specifieke leerkracht.
     * De leerkracht-ID komt uit de URL (identificatie),
     * de naam uit de body (form-data).
     */
    @PostMapping("/leerkracht/{leerkrachtId}")
    public ResponseEntity<KlassLeeslijstResponseDTO> createLeeslijst(
            @PathVariable Long leerkrachtId,
            @RequestBody KlassLeeslijstRequestDTO request) {

        KlassLeeslijstResponseDTO nieuweLijst = leeslijstService.createLeeslijst(request.naam(), leerkrachtId);
        return ResponseEntity.ok(nieuweLijst);
    }

    /**
     * Haalt alle leeslijsten op van één leerkracht voor het
     * dashboardoverzicht[cite: 2].
     */
    @GetMapping("/leerkracht/{leerkrachtId}")
    public ResponseEntity<List<KlassLeeslijstResponseDTO>> getLeeslijstenByLeerkracht(
            @PathVariable Long leerkrachtId) {

        List<KlassLeeslijstResponseDTO> lijsten = leeslijstService.getLeeslijstenByLeerkracht(leerkrachtId);
        return ResponseEntity.ok(lijsten);
    }

    /**
     * Voegt een boek toe aan een bestaande lijst (1-per-1 flow).
     * Gebruikt de BoekMapper voor de conversie naar DTO.
     */
    @PostMapping("/{leeslijstId}/boeken/{boekId}")
    public ResponseEntity<KlassLeeslijstResponseDTO> addBoekToLeeslijst(
            @PathVariable Long leeslijstId,
            @PathVariable Long boekId) {

        KlassLeeslijstResponseDTO geupdateLijst = leeslijstService.addBoekToLeeslijst(leeslijstId, boekId);
        return ResponseEntity.ok(geupdateLijst);
    }

    @GetMapping("/{id}")
    public ResponseEntity<KlassLeeslijstResponseDTO> getLeeslijstById(@PathVariable Long id) {
        return ResponseEntity.ok(leeslijstService.getLeeslijstById(id));
    }

    @DeleteMapping("/{leeslijstId}/boeken/{boekId}")
    public ResponseEntity<KlassLeeslijstResponseDTO> removeBoekFromLeeslijst(
            @PathVariable Long leeslijstId,
            @PathVariable Long boekId) {
        return ResponseEntity.ok(leeslijstService.removeBoekFromLeeslijst(leeslijstId, boekId));
    }

    @PutMapping("/{id}/naam")
    public ResponseEntity<KlassLeeslijstResponseDTO> updateLeeslijstNaam(
            @PathVariable Long id,
            @RequestBody String nieuweNaam) {
        return ResponseEntity.ok(leeslijstService.updateLeeslijstNaam(id, nieuweNaam));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteLeeslijst(@PathVariable Long id) {
        leeslijstService.deleteLeeslijst(id);
        return ResponseEntity.noContent().build();
    }
}