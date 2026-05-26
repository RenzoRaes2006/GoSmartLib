package ap.be.backend.dto;

import java.util.List;

/**
 * Preview voor de vertrek-flow van een leerkracht:
 *  - {@code leeslijsten}: alle actieve (niet-gearchiveerde) leeslijsten van de leerkracht
 *  - {@code kandidaten}: actieve leerkrachten van dezelfde school waarnaar lijsten
 *    overgedragen kunnen worden (de vertrekkende leerkracht zelf zit hier niet bij)
 *  - {@code aantalOpenLeningen}: open uitleningen op naam van deze leerkracht;
 *    blokkeert het vertrek als > 0
 */
public record LeerkrachtVertrekPreviewDTO(
        List<KlassLeeslijstResponseDTO> leeslijsten,
        List<KandidaatLeerkrachtDTO> kandidaten,
        int aantalOpenLeningen) {
}
