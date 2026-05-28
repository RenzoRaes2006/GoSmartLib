package ap.be.backend.dto;

/**
 * Body voor PATCH /api/leeslijsten/{id}/overdragen — wisselt de eigenaar
 * (leerkracht) van een leeslijst.
 */
public record OverdragenRequest(Long nieuweLeerkrachtId) {
}
