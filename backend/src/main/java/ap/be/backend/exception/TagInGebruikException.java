package ap.be.backend.exception;

public class TagInGebruikException extends RuntimeException {
    public TagInGebruikException(String naam, long aantalBoeken) {
        super("Tag '" + naam + "' kan niet verwijderd worden: gebruikt door " + aantalBoeken + " boek(en).");
    }
}
