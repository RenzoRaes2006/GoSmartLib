package ap.be.backend.dto;

/**
 * Compacte voorstelling van een actieve leerkracht die in aanmerking komt om
 * leeslijsten over te nemen van een vertrekkende collega.
 */
public record KandidaatLeerkrachtDTO(
        Long id,
        String smartschoolId,
        String voorNaam,
        String achternaam) {
}
