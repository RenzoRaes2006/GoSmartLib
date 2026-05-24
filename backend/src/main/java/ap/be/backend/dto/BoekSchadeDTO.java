package ap.be.backend.dto;

import java.time.LocalDateTime;

/**
 * Lijst-DTO voor het schade-overzicht op de beheerderspagina.
 *
 * BELANGRIJK: leerlingNaam wordt door de service live via OneRoster ingevuld
 * en zit ALLEEN in deze API-response. De BoekSchade-entity bevat geen naam-veld;
 * er wordt nooit een naam in de database opgeslagen.
 */
public record BoekSchadeDTO(
        Long id,
        String boekTitel,
        String boekIsbn,
        String leerlingSmartschoolId,
        String leerlingNaam,
        String leerlingKlas,
        String opmerking,
        LocalDateTime gemeldOp) {
}
