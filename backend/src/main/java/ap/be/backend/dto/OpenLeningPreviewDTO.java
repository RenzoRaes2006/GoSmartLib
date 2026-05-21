package ap.be.backend.dto;

/**
 * Toont één open uitlening van een vertrekkende leerling in de rollover-preview.
 * De admin gebruikt dit om te beslissen of hij het boek als "verloren" markeert
 * vóór hij de rollover bevestigt.
 */
public record OpenLeningPreviewDTO(
        Long uitleningId,
        String leerlingSmartschoolId,
        String boekTitel,
        String boekIsbn
) {}
