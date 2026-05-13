package ap.be.backend.dto;

/**
 * Bevat de gegevens die worden opgehaald uit de OneRoster API na het opzoeken
 * van een gebruiker via zijn Smartschool-gebruikersnaam.
 *
 * Wordt aangemaakt in OneRosterService en doorgegeven aan OAuth2LoginSuccessHandler
 * om de rol en klas van de ingelogde gebruiker te bepalen.
 */
public class OneRosterUser {

    /**
     * De unieke ID van de gebruiker in het OneRoster-systeem.
     * Wordt gebruikt als parameter om daarna de klas op te halen.
     */
    private final String sourcedId;

    /**
     * De rol van de gebruiker zoals geregistreerd in OneRoster.
     * Mogelijke waarden: "student" of "teacher".
     */
    private final String role;

    public OneRosterUser(String sourcedId, String role) {
        this.sourcedId = sourcedId;
        this.role = role;
    }

    public String getSourcedId() {
        return sourcedId;
    }

    public String getRole() {
        return role;
    }
}
