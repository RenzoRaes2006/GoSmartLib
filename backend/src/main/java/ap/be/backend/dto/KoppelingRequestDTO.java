package ap.be.backend.dto;

/**
 * Verzoek om de koppeling van een bestaande leeslijst te wijzigen.
 * Exact één van klas of leerlingId moet ingevuld zijn.
 */
public record KoppelingRequestDTO(String klas, Long leerlingId) {
}
