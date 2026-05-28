package ap.be.backend.dto;

import java.time.LocalDateTime;

public record LesmateriaalDTO(
        Long id,
        Long boekId,
        Long uploaderId,
        String uploaderNaam,
        String origineleBestandsnaam,
        String contentType,
        long grootte,
        String beschrijving,
        LocalDateTime uploadedAt,
        boolean eigenaar) {
}
