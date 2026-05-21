package ap.be.backend.exception;

public class TagHeeftSubtagsException extends RuntimeException {
    public TagHeeftSubtagsException(String naam, long aantalSubtags) {
        super("Hoofdgenre '" + naam + "' kan niet verwijderd worden: heeft nog "
                + aantalSubtags + " subgenre(s). Verwijder eerst de subgenres.");
    }
}
