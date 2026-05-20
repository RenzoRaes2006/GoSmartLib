package ap.be.backend.exception;

public class DuplicateTagException extends RuntimeException {
    public DuplicateTagException(String naam) {
        super("Tag met naam '" + naam + "' bestaat al binnen dit type.");
    }
}
