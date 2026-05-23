package ap.be.backend.mappers;

import ap.be.backend.dto.BeoordelingDTO;
import ap.be.backend.entity.Beoordeling;
import ap.be.backend.entity.Boek;
import ap.be.backend.entity.User;

/**
 * Zet Beoordeling-entiteiten om naar BeoordelingDTO's en omgekeerd.
 */
public class BeoordelingMapper {

    /**
     * Zet een beoordeling om naar een DTO.
     *
     * Als de beoordeling anoniem is, wordt "Anoniem" als weergavenaam gebruikt.
     * Anders wordt de meegegeven naam (live opgehaald uit OneRoster) getoond;
     * valt die weg, dan wordt teruggevallen op de smartschoolId.
     *
     * @param beoordeling   de te converteren entiteit
     * @param gerapporteerd geeft aan of de beoordeling als ongepast is gemeld
     * @param naam          de volledige naam van de gebruiker uit OneRoster,
     *                      of null als die niet beschikbaar is
     * @return de beoordeling als DTO
     */
    public static BeoordelingDTO toDTO(Beoordeling beoordeling, boolean gerapporteerd, String naam) {
        String weergaveNaam;
        if (beoordeling.isAnoniem()) {
            weergaveNaam = "Anoniem";
        } else if (naam != null && !naam.isBlank()) {
            weergaveNaam = naam;
        } else {
            weergaveNaam = beoordeling.getUser() != null ? beoordeling.getUser().getSmartschoolId() : null;
        }

        return new BeoordelingDTO(
                beoordeling.getId(),
                weergaveNaam,
                beoordeling.getBoek().getId(),
                beoordeling.getWaardering(),
                beoordeling.getInhoud(),
                beoordeling.isAnoniem(),
                gerapporteerd,
                beoordeling.isSpoiler());
    }

    /**
     * Zet een BeoordelingDTO om naar een Beoordeling-entiteit.
     *
     * @param dto  de DTO met de invoergegevens
     * @param boek het boek waarover de beoordeling gaat
     * @param user de gebruiker die de beoordeling plaatst
     * @return de aangemaakte Beoordeling-entiteit
     */
    public static Beoordeling toEntity(BeoordelingDTO dto, Boek boek, User user) {
        Beoordeling beoordeling = new Beoordeling();
        beoordeling.setId(dto.id());
        beoordeling.setUser(user);
        beoordeling.setBoek(boek);
        beoordeling.setAnoniem(dto.isAnoniem());
        beoordeling.setWaardering(dto.waardering());
        beoordeling.setInhoud(dto.inhoud());
        beoordeling.setSpoiler(dto.isSpoiler());
        return beoordeling;
    }
}
