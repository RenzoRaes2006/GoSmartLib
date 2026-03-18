package ap.be.backend.exception;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class GlobalExceptionHandler {

    // handel specifieke uitzondering voor ISBN al bestaand
    // Deze handler wordt getriggerd wanneer een IsbnAlreadyExistsException wordt
    // gegooid
    @ExceptionHandler(IsbnAlreadyExistsException.class)
    public ResponseEntity<String> handleIsbnAlreadyExists(IsbnAlreadyExistsException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(ex.getMessage());
    }

    // handel database constraint violations, zoals unieke beperkingen op ISBN
    // Deze handler wordt getriggerd wanneer een DataIntegrityViolationException
    // wordt gegooid, bijvoorbeeld bij een unieke beperking op ISBN
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<String> handleDataIntegrityViolation(DataIntegrityViolationException ex) {
        if (ex.getMessage() != null && ex.getMessage().toLowerCase().contains("isbn")) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("Dit ISBN-nummer bestaat al.");
        }
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Database constraint violation");
    }

    // handel validatie fouten, zoals ontbrekende of onjuiste velden in de invoer
    // Deze handler wordt getriggerd wanneer een MethodArgumentNotValidException
    // wordt gegooid, bijvoorbeeld bij validatiefouten in de invoer
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<String> handleValidationExceptions(MethodArgumentNotValidException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Ongeldige invoer: " + ex.getMessage());
    }

    // handel alle andere uitzonderingen die niet specifiek zijn afgevangen door de
    // bovenstaande handlers
    // Deze handler wordt getriggerd voor alle andere uitzonderingen die niet
    // specifiek zijn af
    @ExceptionHandler(Exception.class)
    public ResponseEntity<String> handleGenericException(Exception ex) {
        ex.printStackTrace();
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Er is een fout opgetreden");
    }

    @ExceptionHandler(InvalidYearRangeException.class)
    public ResponseEntity<String> handleInvalidYearRange(InvalidYearRangeException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ex.getMessage());
    }

}
