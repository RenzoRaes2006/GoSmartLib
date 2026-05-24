package ap.be.backend.dto;

import java.time.LocalDateTime;

public record KlasWijzigingDTO(
        Long id,
        String vanKlas,
        String naarKlas,
        LocalDateTime gewijzigdOp,
        String doorWieSmartschoolId,
        String bron
) {}
