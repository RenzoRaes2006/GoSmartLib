package ap.be.backend.dto;

/***
 * BESTAND: OneRosterUser.java
 *
 * WAT IS DIT?
 * Dit is een DTO (Data Transfer Object): een eenvoudige datacontainer.
 * Het bevat de gegevens die de OneRoster API teruggeeft wanneer we
 * een gebruiker opzoeken via zijn Smartschool-username.
 *
 * WANNEER WORDT DIT GEBRUIKT?
 * In OneRosterService.java: na het ophalen van de gebruiker uit OneRoster
 * stoppen we de twee belangrijkste velden in dit object:
 *   - sourcedId: de unieke ID van de gebruiker in OneRoster
 *                (nodig om daarna de klas op te vragen)
 *   - role:      de officiële rol: "student" of "teacher"
 *
 * VOORBEELD:
 * Stel, OneRoster geeft dit terug:
 *   { "sourcedId": "abc123", "role": "student", "username": "sof2.haitam.samire" }
 * Dan maken we: new OneRosterUser("abc123", "student")
 ***/
public class OneRosterUser {

    /***
     * De unieke ID van de gebruiker in het OneRoster systeem.
     * Voorbeeld: "abc123" of "550e8400-e29b-41d4-a716-446655440000"
     * Wordt gebruikt als parameter in het tweede API-verzoek (klas ophalen).
     ***/
    private final String sourcedId;

    /***
     * De rol van de gebruiker zoals geregistreerd in OneRoster.
     * Mogelijke waarden: "student" of "teacher"
     * Dit is betrouwbaarder dan de rol die Smartschool zelf teruggeeft.
     ***/
    private final String role;

    /***
     * Constructor: wordt aangeroepen in OneRosterService na het parsen van de JSON.
     ***/
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
