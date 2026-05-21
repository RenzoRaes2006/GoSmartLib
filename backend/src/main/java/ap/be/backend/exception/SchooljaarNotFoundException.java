package ap.be.backend.exception;

public class SchooljaarNotFoundException extends RuntimeException {
    public SchooljaarNotFoundException() {
        super("Geen actief schooljaar gevonden.");
    }
}
