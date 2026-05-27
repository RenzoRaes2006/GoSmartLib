package ap.be.backend.service.notificatie;

import ap.be.backend.entity.School;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * Stuurt berichten naar de Smartschool-inbox van een leerling via de SOAP
 * webservice (sendMsg). Vereist dat de school een geactiveerde API-key heeft
 * in {@link School#getSmartschoolApiKey()}.
 *
 * Activeer met: gosmartlib.notificatie.mode=soap
 *
 * Endpoint wordt afgeleid uit {@link School#getSmartschoolDomein()} ->
 *   https://{domein}/Webservices/V3
 *
 * Afzender is een vaste systeem-naam ("GoSmartLib"). Smartschool toont die als
 * "platform-afzender" bij de leerling.
 */
@Service
@ConditionalOnProperty(name = "gosmartlib.notificatie.mode", havingValue = "soap")
public class SmartschoolSoapNotificatieService implements NotificatieService {

    // Leeg = bericht komt vanaf "Smartschool" (platformafzender). Een vrije string
    // wordt geweigerd met foutcode 12; senderIdentifier moet een geldig intern
    // Smartschool-userID zijn óf leeg blijven.
    private static final String AFZENDER = "";
    private static final DateTimeFormatter DATUM = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private final RestTemplate restTemplate = new RestTemplate();

    @Override
    public void stuurTeLaatBericht(School school, String ontvangerSmartschoolId,
                                   String boekTitel, LocalDate inleverDatum) {
        String body = String.format(
                "Beste,\n\nHet boek '%s' is te laat ingeleverd. Het had ingediend moeten worden tegen %s.\n\nGelieve dit zo snel mogelijk in orde te brengen bij de bibliotheek.\n\nMet vriendelijke groeten,\nDe bibliotheek",
                boekTitel, inleverDatum.format(DATUM));
        verzend(school, ontvangerSmartschoolId, "Te laat inleveren", body);
    }

    @Override
    public void stuurHerinnering(School school, String ontvangerSmartschoolId,
                                 String boekTitel, LocalDate inleverDatum) {
        String body = String.format(
                "Beste,\n\nDit is een herinnering: het boek '%s' (deadline %s) is nog steeds niet ingeleverd, ondertussen meer dan een week te laat.\n\nGelieve het boek dringend terug te brengen naar de bibliotheek.\n\nMet vriendelijke groeten,\nDe bibliotheek",
                boekTitel, inleverDatum.format(DATUM));
        verzend(school, ontvangerSmartschoolId, "Herinnering: te laat inleveren", body);
    }

    private void verzend(School school, String ontvangerSmartschoolId, String titel, String bericht) {
        String apiKey = school.getSmartschoolApiKey();
        String domein = school.getSmartschoolDomein();
        if (apiKey == null || apiKey.isBlank() || domein == null || domein.isBlank()) {
            System.err.printf("[Notificatie/SOAP] overgeslagen: school %s mist API-key of domein%n", school.getNaam());
            return;
        }
        String endpoint = "https://" + domein + "/Webservices/V3";

        String envelope = soapEnvelope(apiKey, ontvangerSmartschoolId, titel, bericht);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.TEXT_XML);
        headers.add("SOAPAction", "\"sendMsg\"");

        try {
            restTemplate.postForEntity(endpoint, new HttpEntity<>(envelope, headers), String.class);
        } catch (Exception e) {
            // Niet hard falen: een mislukte notificatie mag de scheduled job niet stuk maken.
            System.err.printf("[Notificatie/SOAP] versturen mislukt voor %s op %s: %s%n",
                    ontvangerSmartschoolId, school.getNaam(), e.getMessage());
        }
    }

    private String soapEnvelope(String apiKey, String ontvanger, String titel, String bericht) {
        return "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
                + "<soap:Envelope xmlns:soap=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:ss=\"https://oauth.smartschool.be/Webservices/V3\">"
                + "<soap:Body>"
                + "<ss:sendMsg>"
                + "<accesscode>" + xml(apiKey) + "</accesscode>"
                + "<userIdentifier>" + xml(ontvanger) + "</userIdentifier>"
                + "<title>" + xml(titel) + "</title>"
                + "<body>" + xml(bericht) + "</body>"
                + "<senderIdentifier>" + xml(AFZENDER) + "</senderIdentifier>"
                + "<attachments></attachments>"
                + "<coc>0</coc>"
                + "<copyToLVS>false</copyToLVS>"
                + "</ss:sendMsg>"
                + "</soap:Body>"
                + "</soap:Envelope>";
    }

    private static String xml(String s) {
        if (s == null) return "";
        return s.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;");
    }
}
