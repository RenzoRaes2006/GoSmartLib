package ap.be.backend.exception;

/**
 * Gegooid wanneer een gebruiker een beoordeling probeert te plaatsen voor een
 * boek dat hij al eerder beoordeeld heeft.
 */
public class DuplicateBeoordelingException extends RuntimeException {

    public DuplicateBeoordelingException(String message) {
        super(message);
    }
}
