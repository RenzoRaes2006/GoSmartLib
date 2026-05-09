package ap.be.backend.mappers;

import ap.be.backend.dto.BeoordelingDTO;
import ap.be.backend.entity.Beoordeling;
import ap.be.backend.entity.Boek;
import ap.be.backend.entity.User;

public class BeoordelingMapper {

    public static BeoordelingDTO toDTO(Beoordeling beoordeling) {
        return toDTO(beoordeling, false);
    }

    public static BeoordelingDTO toDTO(Beoordeling beoordeling, boolean gerapporteerd) {
        // Geen naam in DB; alleen smartschoolId of "Anoniem" als isAnoniem=true.
        String weergaveNaam = beoordeling.isAnoniem()
                ? "Anoniem"
                : (beoordeling.getUser() != null ? beoordeling.getUser().getSmartschoolId() : null);

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
