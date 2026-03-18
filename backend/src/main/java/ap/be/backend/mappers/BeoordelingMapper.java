package ap.be.backend.mappers;

import ap.be.backend.dto.BeoordelingDTO;
import ap.be.backend.entity.Beoordeling;
import ap.be.backend.entity.Boek;

public class BeoordelingMapper {

    // Van Entity naar DTO
    public static BeoordelingDTO toDTO(Beoordeling beoordeling) {
        return toDTO(beoordeling, false);
    }

    // Van Entity naar DTO met gerapporteerd status
    public static BeoordelingDTO toDTO(Beoordeling beoordeling, boolean gerapporteerd) {
        return new BeoordelingDTO(
                beoordeling.getId(),
                beoordeling.getGebruikerId(),
                beoordeling.getBoek().getId(), // Haal ID uit het Boek object
                beoordeling.getWaardering(),
                beoordeling.getInhoud(),
                gerapporteerd);
    }

    // Van DTO naar Entity
    public static Beoordeling toEntity(BeoordelingDTO dto, Boek boek) {
        Beoordeling beoordeling = new Beoordeling();
        beoordeling.setId(dto.id());
        beoordeling.setGebruikerId(dto.gebruikerId());
        beoordeling.setBoek(boek); // Hier zetten we het volledige Boek object
        beoordeling.setWaardering(dto.waardering());
        beoordeling.setInhoud(dto.inhoud());
        return beoordeling;
    }
}