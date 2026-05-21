package ap.be.backend.exception;

public class SchoolNotFoundException extends RuntimeException {
    public SchoolNotFoundException(Long schoolId) {
        super("School niet gevonden: " + schoolId);
    }
}
