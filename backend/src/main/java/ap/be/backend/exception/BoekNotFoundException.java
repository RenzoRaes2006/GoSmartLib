package ap.be.backend.exception;

public class BoekNotFoundException extends RuntimeException {
    // Constructor die een foutmelding accepteert en doorgeeft aan de superclass
    // RuntimeException
    // Deze constructor wordt gebruikt om een specifieke foutmelding te geven
    // wanneer een boek niet gevonden wordt
    public BoekNotFoundException(String message) {
        super(message);
    }
}