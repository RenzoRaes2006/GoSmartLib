package ap.be.backend.dto;

/**
 * Request-body voor het handmatig toevoegen van een leerling of leerkracht
 * door de admin (buiten OneRoster-sync om).
 *
 * @param smartschoolId verplicht — moet uniek zijn over alle scholen heen
 * @param schoolId      verplicht — koppelt de gebruiker aan een bestaande school
 * @param klas          optioneel
 */
public record GebruikerCreateRequest(
        String smartschoolId,
        Long schoolId,
        String klas) {
}
