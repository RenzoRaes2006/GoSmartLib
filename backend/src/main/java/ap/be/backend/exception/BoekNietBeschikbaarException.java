package ap.be.backend.exception;

public class BoekNietBeschikbaarException extends RuntimeException {
    public BoekNietBeschikbaarException(String message) {
        super(message);
    }
}
