package ap.be.backend.mappers;

import java.util.List;

import ap.be.backend.dto.BoekDTO;
import ap.be.backend.dto.TagDTO;
import ap.be.backend.entity.Boek;

public class BoekMapper {

    public static BoekDTO toDTO(Boek boek) {
        return toDTO(boek, false);
    }

    public static BoekDTO toDTO(Boek boek, boolean heeftSchade) {
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
                boek.getAantalBeschikbaar(),
                boek.getLocatie(),
                boek.getAfbeelding(),
                boek.getPopulariteit(),
                boek.getLeesniveau(),
                boek.isDidactischMateriaal(),
                boek.getCampus() == null ? null : boek.getCampus().getId(),
                boek.getCampus() == null ? null : boek.getCampus().getNaam(),
                heeftSchade,
                boek.getAantalBeschadigd());
    }
}
