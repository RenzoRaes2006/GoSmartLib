package ap.be.backend.exception;

public class InvalidImportDataException extends RuntimeException {
    public InvalidImportDataException(String message) {
        super(message);
    }
}
