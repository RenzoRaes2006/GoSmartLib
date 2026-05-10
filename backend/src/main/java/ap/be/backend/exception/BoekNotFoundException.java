package ap.be.backend.exception;

/**
 * Gegooid wanneer een boek niet gevonden wordt in de database op basis van het
 * opgegeven ID of ISBN.
 */
public class BoekNotFoundException extends RuntimeException {

    public BoekNotFoundException(String message) {
        super(message);
    }
}
