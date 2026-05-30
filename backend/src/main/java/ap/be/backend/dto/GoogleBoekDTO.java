package ap.be.backend.dto;

/**
 * Boekgegevens zoals opgehaald bij Google Books, klaar om in het
 * boek-formulier in te vullen. Dezelfde veldnamen als de frontend verwacht.
 */
public record GoogleBoekDTO(
        String titel,
        String auteur,
        String isbn,
        String uitgever,
        String uitgaveJaar,
        String pagina,
        String beschrijving,
        String afbeelding) {
}
