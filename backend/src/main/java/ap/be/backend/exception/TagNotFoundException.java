package ap.be.backend.exception;

public class TagNotFoundException extends RuntimeException {
    public TagNotFoundException(Long id) {
        super("Tag met id " + id + " niet gevonden.");
    }
}
