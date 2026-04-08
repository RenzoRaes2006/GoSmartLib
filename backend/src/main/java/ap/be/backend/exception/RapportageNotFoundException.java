package ap.be.backend.exception;

public class RapportageNotFoundException extends RuntimeException {
    public RapportageNotFoundException(String message) {
        super(message);
    }
}
