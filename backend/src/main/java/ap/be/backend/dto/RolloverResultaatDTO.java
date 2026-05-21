package ap.be.backend.dto;

/**
 * Bevestigt aan de admin wat de rollover heeft gedaan.
 * Wordt teruggestuurd na een succesvolle POST /api/schooljaar/{schoolId}/rollover.
 */
public record RolloverResultaatDTO(
        String nieuwSchooljaar,
        int leerlingenGedeactiveerd,
        int beoordelingenGeanonimiseerd,
        int leeslijstenGearchiveerd,
        int leningenAlsVerlorenGemarkeerd
) {}
