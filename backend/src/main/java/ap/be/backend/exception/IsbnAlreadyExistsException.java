package ap.be.backend.exception;

public class IsbnAlreadyExistsException extends RuntimeException {
    // Constructor die een foutmelding accepteert en doorgeeft aan de superclass
    // RuntimeException
    // Deze constructor wordt gebruikt om een specifieke foutmelding te geven
    // wanneer een ISBN al bestaat
    public IsbnAlreadyExistsException(String message) {
        super(message);
    }
}
