package ap.be.backend.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.CONFLICT)
public class OnvoldoendeVoorraadException extends RuntimeException {
    public OnvoldoendeVoorraadException(String message) {
        super(message);
    }
}