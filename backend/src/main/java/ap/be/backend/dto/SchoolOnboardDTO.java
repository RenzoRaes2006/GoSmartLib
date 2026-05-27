package ap.be.backend.dto;

import lombok.Data;
import java.util.List;

@Data
public class SchoolOnboardDTO {
    private String schoolNaam;
    private String adres;

    private String smartschoolDomein;
    private String oauthClientId;
    private String oauthClientSecret;
    private String smartschoolApiKey;

    private List<CampusDTO> campussen;

    private BeheerderDTO beheerder;

    @Data
    public static class BeheerderDTO {
        private String gebruikersnaam;
        private String naam;
        private String wachtwoord;
    }
}
