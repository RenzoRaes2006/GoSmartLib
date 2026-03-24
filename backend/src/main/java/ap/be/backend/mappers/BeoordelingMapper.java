package ap.be.backend.mappers;

import ap.be.backend.dto.BeoordelingDTO;
import ap.be.backend.entity.Beoordeling;
import ap.be.backend.entity.Boek;
import ap.be.backend.entity.Gebruiker;

public class BeoordelingMapper {

    public static BeoordelingDTO toDTO(Beoordeling beoordeling) {
        return toDTO(beoordeling, false);
    }

    public static BeoordelingDTO toDTO(Beoordeling beoordeling, boolean gerapporteerd) {
        // Logica: als anoniem true is, toon 'Anoniem', anders de echte naam
        String weergaveNaam = beoordeling.isAnoniem() ? "Anoniem" : "Test Naam"; // TODO: Vervang "Test Gebruiker" met daadwerkelijke naam uit beoordeling.getGebruiker()

        return new BeoordelingDTO(
                beoordeling.getId(),
                weergaveNaam, 
                beoordeling.getBoek().getId(),
                beoordeling.getWaardering(),
                beoordeling.getInhoud(),
                beoordeling.isAnoniem(),
                gerapporteerd);
    }

    // Nu geven we zowel Boek als Gebruiker mee
    public static Beoordeling toEntity(BeoordelingDTO dto, Boek boek, Gebruiker gebruiker) {
        Beoordeling beoordeling = new Beoordeling();
        beoordeling.setId(dto.id());
        beoordeling.setGebruiker(gebruiker); // Het hele object
        beoordeling.setBoek(boek);           // Het hele object
        beoordeling.setAnoniem(dto.isAnoniem());
        beoordeling.setWaardering(dto.waardering());
        beoordeling.setInhoud(dto.inhoud());
        return beoordeling;
    }
}