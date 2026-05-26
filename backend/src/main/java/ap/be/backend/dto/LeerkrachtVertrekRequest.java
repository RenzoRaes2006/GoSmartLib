package ap.be.backend.dto;

import java.util.Map;

/**
 * Body voor PATCH /api/admin/leerkrachten/{id}/vertrek.
 *
 * {@code overdrachten} mapt leeslijst-id → nieuwe leerkracht-id voor lijsten
 * die overgedragen moeten worden. Lijsten die niet in de map staan, worden
 * automatisch gearchiveerd. Null of leeg = alles archiveren.
 */
public record LeerkrachtVertrekRequest(Map<Long, Long> overdrachten) {
}
