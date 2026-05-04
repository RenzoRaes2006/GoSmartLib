package ap.be.backend.mappers;

import ap.be.backend.dto.BoekDTO;
import ap.be.backend.entity.Boek;

public class BoekMapper {

    // Van Entity naar DTO
    public static BoekDTO toDTO(Boek boek) {
        return new BoekDTO(
                boek.getId(),
                boek.getTitel(),
                boek.getAuteur(),
                boek.getIsbn(),
                boek.getUitgever(),
                boek.getUitgaveJaar(),
                boek.getPagina(),
                boek.getGenre(),
                boek.getThematischeTag(),
                boek.getBeschrijving(),
                boek.getAantalExemplaren(),
                boek.getLocatie(),
                boek.getAfbeelding(),
                boek.getPopulariteit(),
                boek.getLeesniveau(),
                boek.isDidactischMateriaal(),
                boek.getDidactischeSubcategorie());
    }

    // Van DTO naar Entity
    public static Boek toEntity(BoekDTO dto) {
        Boek boek = new Boek();
        boek.setId(dto.id() != null ? dto.id() : 0);
        boek.setTitel(dto.titel());
        boek.setAuteur(dto.auteur());
        boek.setIsbn(dto.isbn());
        boek.setUitgever(dto.uitgever());
        boek.setUitgaveJaar(dto.uitgaveJaar());
        boek.setPagina(dto.pagina());
        boek.setGenre(dto.genre());
        boek.setThematischeTag(dto.thematischeTag());
        boek.setBeschrijving(dto.beschrijving());
        boek.setAantalExemplaren(dto.aantalExemplaren());
        boek.setLocatie(dto.locatie());
        boek.setAfbeelding(dto.afbeelding());
        boek.setPopulariteit(dto.populariteit());
        boek.setLeesniveau(dto.leesniveau());
        boek.setDidactischMateriaal(dto.didactischMateriaal() != null ? dto.didactischMateriaal() : false);
        boek.setDidactischeSubcategorie(dto.didactischeSubcategorie());
        return boek;
    }
}