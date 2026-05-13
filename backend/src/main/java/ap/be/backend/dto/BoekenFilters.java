package ap.be.backend.dto;

import java.util.List;

import ap.be.backend.entity.DidactischeSubcategorie;

public record BoekenFilters(
        String zoekterm,
        List<String> genre,
        List<String> thematischeTag,
        String uitgever,
        Integer minPagina,
        Integer maxPagina,
        String beschikbaar,
        Integer uitgaveJaarVan,
        Integer uitgaveJaarTot,
        String leesniveau,
        Boolean didactischMateriaal,
        DidactischeSubcategorie didactischeSubcategorie) {

    public Boolean beschikbaarAsBoolean() {
        return beschikbaar != null ? Boolean.parseBoolean(beschikbaar) : null;
    }
}
