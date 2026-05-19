package ap.be.backend.mappers;

import java.util.List;

import ap.be.backend.dto.BoekDTO;
import ap.be.backend.dto.TagDTO;
import ap.be.backend.entity.Boek;

public class BoekMapper {

    public static BoekDTO toDTO(Boek boek) {
        List<TagDTO> tagDtos = boek.getTags() == null ? List.of()
                : boek.getTags().stream().map(TagDTO::from).toList();
        return new BoekDTO(
                boek.getId(),
                boek.getTitel(),
                boek.getAuteur(),
                boek.getIsbn(),
                boek.getUitgever(),
                boek.getUitgaveJaar(),
                boek.getPagina(),
                tagDtos,
                boek.getBeschrijving(),
                boek.getAantalExemplaren(),
                boek.getLocatie(),
                boek.getAfbeelding(),
                boek.getPopulariteit(),
                boek.getLeesniveau(),
                boek.isDidactischMateriaal());
    }
}
