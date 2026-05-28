package ap.be.backend.service.notificatie;

import ap.be.backend.entity.School;
import ap.be.backend.service.OneRosterClient;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * Vertaalt een OneRoster sourcedId (= wat wij in {@code User.smartschoolId}
 * opslaan, een UUID) naar de echte Smartschool-gebruikersnaam die door
 * sendMsg vereist wordt. Smartschool's sendMsg accepteert geen UUIDs als
 * userIdentifier of senderIdentifier.
 *
 * Lookup gaat via {@link OneRosterClient} — die cachet zelf 30 minuten dus
 * herhaalde calls zijn gratis.
 */
@Service
public class SmartschoolIdResolver {

    private final OneRosterClient oneRosterClient;

    public SmartschoolIdResolver(OneRosterClient oneRosterClient) {
        this.oneRosterClient = oneRosterClient;
    }

    /**
     * Geeft de Smartschool gebruikersnaam (username/login) van de gebruiker met
     * deze sourcedId, of null als niet gevonden of OneRoster onbereikbaar is.
     */
    @SuppressWarnings("unchecked")
    public String resolveGebruikersnaam(School school, String sourcedId) {
        if (sourcedId == null || sourcedId.isBlank()) return null;
        if (school.getSmartschoolDomein() == null
                || school.getOauthClientId() == null
                || school.getOauthClientSecret() == null) {
            return null;
        }
        try {
            Map<String, Object> data = oneRosterClient.haalLiveSchoolDataOp(
                    school.getSmartschoolDomein(),
                    school.getOauthClientId(),
                    school.getOauthClientSecret());
            for (String sleutel : new String[]{"leerlingen", "leerkrachten"}) {
                List<Map<String, Object>> lijst = (List<Map<String, Object>>) data.getOrDefault(sleutel, List.of());
                for (Map<String, Object> u : lijst) {
                    if (sourcedId.equals(String.valueOf(u.get("id")))) {
                        String gn = String.valueOf(u.getOrDefault("gebruikersnaam", ""));
                        return gn.isBlank() ? null : gn;
                    }
                }
            }
        } catch (Exception ignored) {
            // OneRoster onbereikbaar — geef null terug
        }
        return null;
    }
}
