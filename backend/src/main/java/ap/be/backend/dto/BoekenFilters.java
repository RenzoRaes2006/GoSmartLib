package ap.be.backend.dto;

import java.util.List;

public record BoekenFilters(
        String zoekterm,
        List<Long> tagIds,
        String uitgever,
        Integer minPagina,
        Integer maxPagina,
        String beschikbaar,
        Integer uitgaveJaarVan,
        Integer uitgaveJaarTot,
        String leesniveau,
        Boolean didactischMateriaal,
        List<Long> campusIds,
        Integer minSterren) {

    public Boolean beschikbaarAsBoolean() {
        return beschikbaar != null ? Boolean.parseBoolean(beschikbaar) : null;
    }
}
