package ap.be.backend.controller;

import ap.be.backend.dto.RolloverPreviewDTO;
import ap.be.backend.dto.RolloverResultaatDTO;
import ap.be.backend.dto.SchooljaarDTO;
import ap.be.backend.service.SchooljaarService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

/**
 * Beheert schooljaren en de jaarlijkse rollover.
 * Alle write-endpoints zijn uitsluitend beschikbaar voor de AdminGebruiker (ROLE_ADMIN).
 *
 * Typische flow:
 *   1. GET /api/schooljaar/{schoolId}/rollover/preview  → admin bekijkt wat er gaat veranderen
 *   2. POST /api/schooljaar/{schoolId}/rollover         → admin bevestigt de rollover
 */
@RestController
@RequestMapping("/api/schooljaar")
@RequiredArgsConstructor
public class SchooljaarController {

    private final SchooljaarService schooljaarService;

    /**
     * Geeft alle schooljaren terug, gesorteerd van nieuw naar oud.
     */
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping
    public ResponseEntity<List<SchooljaarDTO>> getAllSchooljaren() {
        return ResponseEntity.ok(schooljaarService.getAllSchooljaren());
    }

    /**
     * Geeft het huidige actieve schooljaar terug.
     * Toegankelijk voor alle ingelogde gebruikers (info op dashboard).
     */
    @GetMapping("/actief")
    public ResponseEntity<SchooljaarDTO> getActiefSchooljaar() {
        return ResponseEntity.ok(schooljaarService.getActiefSchooljaar());
    }

    /**
     * Geeft een preview van wat er gaat veranderen bij een rollover.
     * De admin gebruikt dit in de bevestigingsdialoog vóór hij de rollover uitvoert.
     *
     * @param schoolId ID van de school
     */
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/{schoolId}/rollover/preview")
    public ResponseEntity<RolloverPreviewDTO> previewRollover(@PathVariable Long schoolId) {
        return ResponseEntity.ok(schooljaarService.previewRollover(schoolId));
    }

    /**
     * Voert de schooljaar-rollover uit voor een school.
     *
     * @param schoolId        ID van de school
     * @param forceerVerloren als true worden open uitleningen van vertrekkende leerlingen als VERLOREN gemarkeerd.
     *                        Verplicht als er open uitleningen zijn, anders wordt de rollover geblokkeerd.
     * @param nieuwNaam       naam van het nieuwe schooljaar (bijv. "2025-2026")
     * @param nieuwStartDatum startdatum van het nieuwe schooljaar (formaat: yyyy-MM-dd)
     * @param nieuwEindDatum  einddatum van het nieuwe schooljaar (formaat: yyyy-MM-dd)
     */
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/{schoolId}/rollover")
    public ResponseEntity<RolloverResultaatDTO> voerRolloverUit(
            @PathVariable Long schoolId,
            @RequestParam(defaultValue = "false") boolean forceerVerloren,
            @RequestParam String nieuwNaam,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate nieuwStartDatum,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate nieuwEindDatum) {

        RolloverResultaatDTO resultaat = schooljaarService.voerRolloverUit(
                schoolId, forceerVerloren, nieuwNaam, nieuwStartDatum, nieuwEindDatum);

        return ResponseEntity.ok(resultaat);
    }
}
