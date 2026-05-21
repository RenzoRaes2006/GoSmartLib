package ap.be.backend.dto;

import java.util.List;

public record KoppelingRequestDTO(String klas, List<Long> leerlingIds) {
}
