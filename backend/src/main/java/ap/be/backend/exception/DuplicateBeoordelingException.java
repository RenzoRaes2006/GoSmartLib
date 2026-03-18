package ap.be.backend.exception;

public class DuplicateBeoordelingException extends RuntimeException {
    // Constructor die een foutmelding accepteert en doorgeeft aan de superclass
    // RuntimeException
    // Deze constructor wordt gebruikt om een specifieke foutmelding te geven
    // wanneer een beoordeling al bestaat voor een boek en gebruiker
    public DuplicateBeoordelingException(String message) {
        super(message);
    }
    
}
