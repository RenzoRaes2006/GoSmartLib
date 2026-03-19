package ap.be.backend.exception;

public class GebruikerNotFoundException extends RuntimeException {
    public GebruikerNotFoundException(String message) {
        super(message);
    }
}
