package ap.be.backend.dto;

import java.util.List;

/**
 * Preview-resultaat voor een handmatige OneRoster-sync. Bevat enkel
 * smartschoolId + klas — geen namen of andere PII (zie privacy-regel:
 * namen worden nooit gepersisteerd én niet door dit DTO getransporteerd).
 */
public record SyncPreviewDTO(
        LeerlingSectie leerlingen,
        LeerkrachtSectie leerkrachten,
        int aantalKlassen,
        int aantalVertrokkenMetOpenLeningen) {

    public record LeerlingSectie(
            List<NieuweEntry> nieuw,
            List<VerplaatstEntry> verplaatst,
            List<VertrokkenEntry> vertrokken,
            int aantalOngewijzigd) {}

    public record LeerkrachtSectie(
            List<NieuweEntry> nieuw,
            List<VerplaatstEntry> verplaatst,
            int aantalOngewijzigd) {}

    public record NieuweEntry(String smartschoolId, String klas) {}

    public record VerplaatstEntry(String smartschoolId, String vorigeKlas, String nieuweKlas) {}

    public record VertrokkenEntry(String smartschoolId, String klas, int aantalOpenUitleningen) {}
}
