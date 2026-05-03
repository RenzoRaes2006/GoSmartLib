package ap.be.backend.exception;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(IsbnAlreadyExistsException.class)
    public ResponseEntity<String> handleIsbnAlreadyExists(IsbnAlreadyExistsException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(ex.getMessage());
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<String> handleDataIntegrityViolation(DataIntegrityViolationException ex) {
        if (ex.getMessage() != null && ex.getMessage().toLowerCase().contains("isbn")) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("Dit ISBN-nummer bestaat al.");
        }
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Database constraint violation");
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<String> handleValidationExceptions(MethodArgumentNotValidException ex) {
        String errorMessage = ex.getBindingResult().getFieldErrors().get(0).getDefaultMessage();

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Ongeldige invoer: " + errorMessage);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<String> handleGenericException(Exception ex) {
        ex.printStackTrace();
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Er is een fout opgetreden");
    }

    @ExceptionHandler(InvalidYearRangeException.class)
    public ResponseEntity<String> handleInvalidYearRange(InvalidYearRangeException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ex.getMessage());
    }

    @ExceptionHandler(BoekNotFoundException.class)
    public ResponseEntity<String> handleBoekNotFound(BoekNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ex.getMessage());
    }

    @ExceptionHandler(BoekNietBeschikbaarException.class)
    public ResponseEntity<String> handleBoekNietBeschikbaar(BoekNietBeschikbaarException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(ex.getMessage());
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<String> handleIllegalArgument(IllegalArgumentException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ex.getMessage());
    }

    @ExceptionHandler(DuplicateReportException.class)
    public ResponseEntity<String> handleDuplicateReport(DuplicateReportException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(ex.getMessage());
    }

    @ExceptionHandler(MapNotFoundException.class)
    public ResponseEntity<String> handleMapNotFound(MapNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ex.getMessage());
    }

    @ExceptionHandler(UitleningNotFoundException.class)
    public ResponseEntity<String> handleUitleningNotFound(UitleningNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ex.getMessage());
    }

    @ExceptionHandler(LeenVoorwaardeException.class)
    public ResponseEntity<String> handleLeenVoorwaarde(LeenVoorwaardeException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ex.getMessage());
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<String> handleMaxUploadSize(MaxUploadSizeExceededException ex) {
        return ResponseEntity.status(HttpStatus.PAYLOAD_TOO_LARGE)
                .body("Bestand is te groot. Maximale grootte is 5MB.");
    }

    @ExceptionHandler(RapportageNotFoundException.class)
    public ResponseEntity<String> handleRapportageNotFound(RapportageNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ex.getMessage());
    }

    @ExceptionHandler(DuplicateBoekException.class)
    public ResponseEntity<String> handleDuplicateBoek(DuplicateBoekException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(ex.getMessage());
    }

    @ExceptionHandler(InvalidImportDataException.class)
    public ResponseEntity<String> handleInvalidImportData(InvalidImportDataException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ex.getMessage());
    }

    @ExceptionHandler(KlassLeeslijstNotFoundException.class)
    public ResponseEntity<String> handleKlassLeeslijstNotFound(KlassLeeslijstNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ex.getMessage());
    }

}
