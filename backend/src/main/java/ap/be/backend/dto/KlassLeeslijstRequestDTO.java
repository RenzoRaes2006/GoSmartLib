package ap.be.backend.dto;

public record KlassLeeslijstRequestDTO(
        String naam,
        String klas,
        Long leerlingId,
        String beschrijving,
        Boolean zichtbaar) {
}
