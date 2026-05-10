package ap.be.backend.exception;

/**
 * Gegooid wanneer een beoordeling niet voldoet aan de validatieregels,
 * bijvoorbeeld als de waardering buiten het toegestane bereik valt.
 */
public class InvalidBeoordelingException extends RuntimeException {

    public InvalidBeoordelingException(String message) {
        super(message);
    }
}
