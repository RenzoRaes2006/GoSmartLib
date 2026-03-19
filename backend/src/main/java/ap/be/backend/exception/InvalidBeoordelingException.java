package ap.be.backend.exception;

public class InvalidBeoordelingException extends RuntimeException {
    // Constructor die een foutmelding accepteert en doorgeeft aan de superclass
    // RuntimeException
    // Deze constructor wordt gebruikt om een specifieke foutmelding te geven
    // wanneer een beoordeling ongeldig is
    public InvalidBeoordelingException(String message) {
        super(message);
    }
}
