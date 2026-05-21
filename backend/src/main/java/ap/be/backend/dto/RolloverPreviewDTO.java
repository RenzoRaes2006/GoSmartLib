package ap.be.backend.dto;

import java.util.List;

/**
 * Geeft de admin een overzicht van wat er gaat veranderen bij een rollover,
 * vóór hij bevestigt. De frontend toont dit in een bevestigingsdialoog.
 *
 * kanDoorgaan=true betekent dat er geen open uitleningen zijn bij vertrekkende
 * leerlingen en de rollover direct kan worden uitgevoerd.
 * kanDoorgaan=false betekent dat de admin eerst de open leningen als "verloren"
 * moet markeren via de forceerVerloren parameter.
 */
public record RolloverPreviewDTO(
        int aantalVertrokkenLeerlingen,
        List<OpenLeningPreviewDTO> openLeningenVertrokkenen,
        int aantalTeArchiverenKlasLeeslijsten,
        boolean kanDoorgaan
) {}
