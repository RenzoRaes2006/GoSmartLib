package ap.be.backend.service.notificatie;

import ap.be.backend.entity.School;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Stuurt op aanvraag (vanuit admin-UI) een test-herinnering naar de
 * Smartschool-inbox van een leerling en geeft een leesbare success/error
 * boodschap terug — i.t.t. de scheduler die fouten enkel naar stderr logt.
 *
 * De sourcedIds die wij opslaan (UUIDs uit OneRoster) worden via
 * {@link SmartschoolIdResolver} live vertaald naar de Smartschool
 * gebruikersnaam, want sendMsg accepteert geen UUIDs als identifier.
 */
@Service
public class NotificatieTestService {

    private static final DateTimeFormatter DATUM = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final Pattern RETURN_PATTERN =
            Pattern.compile("<return[^>]*>([^<]*)</return>", Pattern.CASE_INSENSITIVE);
    private static final Pattern FAULT_PATTERN =
            Pattern.compile("<faultstring[^>]*>([^<]*)</faultstring>", Pattern.CASE_INSENSITIVE);

    private final RestTemplate restTemplate = new RestTemplate();
    private final SmartschoolIdResolver idResolver;

    public NotificatieTestService(SmartschoolIdResolver idResolver) {
        this.idResolver = idResolver;
    }

    public TestResultaat stuurTestHerinnering(School school, String ontvangerSourcedId, String boekTitel) {
        String apiKey = school.getSmartschoolApiKey();
        String domein = school.getSmartschoolDomein();
        String afzenderSourcedId = school.getSmartschoolAfzenderId();
        if (apiKey == null || apiKey.isBlank()) {
            return TestResultaat.fout("School heeft geen Smartschool API-key ingesteld.");
        }
        if (domein == null || domein.isBlank()) {
            return TestResultaat.fout("School heeft geen Smartschool domein ingesteld.");
        }
        if (afzenderSourcedId == null || afzenderSourcedId.isBlank()) {
            return TestResultaat.fout("School heeft geen afzender ingesteld. Stel in via 'Bewerken' op de school.");
        }
        if (ontvangerSourcedId == null || ontvangerSourcedId.isBlank()) {
            return TestResultaat.fout("Geen leerling gekozen.");
        }

        // Vertaal sourcedIds → Smartschool gebruikersnaam (want sendMsg weigert UUIDs).
        String afzenderGn = idResolver.resolveGebruikersnaam(school, afzenderSourcedId);
        if (afzenderGn == null) {
            return TestResultaat.fout("Smartschool-gebruikersnaam van de afzender niet gevonden in OneRoster. Kies een andere leerkracht of synchroniseer eerst.");
        }
        String ontvangerGn = idResolver.resolveGebruikersnaam(school, ontvangerSourcedId);
        if (ontvangerGn == null) {
            return TestResultaat.fout("Smartschool-gebruikersnaam van de ontvanger niet gevonden in OneRoster.");
        }

        String titel = "Herinnering: te laat inleveren";
        String boek = (boekTitel == null || boekTitel.isBlank()) ? "Testboek" : boekTitel;
        LocalDate deadline = LocalDate.now().minusDays(8);
        String bericht = String.format(
                "Beste,\n\nDit is een herinnering: het boek '%s' (deadline %s) is nog steeds niet ingeleverd, ondertussen meer dan een week te laat.\n\nGelieve het boek dringend terug te brengen naar de bibliotheek.\n\nMet vriendelijke groeten,\nDe bibliotheek",
                boek, deadline.format(DATUM));

        String endpoint = "https://" + domein + "/Webservices/V3";
        String envelope = soapEnvelope(apiKey, ontvangerGn, titel, bericht, afzenderGn);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.TEXT_XML);
        headers.add("SOAPAction", "\"sendMsg\"");

        ResponseEntity<String> response;
        try {
            response = restTemplate.postForEntity(endpoint, new HttpEntity<>(envelope, headers), String.class);
        } catch (Exception e) {
            return TestResultaat.fout("HTTP-call naar Smartschool faalde: " + e.getMessage());
        }

        String body = response.getBody() == null ? "" : response.getBody();

        Matcher fault = FAULT_PATTERN.matcher(body);
        if (fault.find()) {
            return TestResultaat.fout("Smartschool SOAP-fault: " + fault.group(1));
        }

        Matcher ret = RETURN_PATTERN.matcher(body);
        if (ret.find()) {
            String code = ret.group(1).trim();
            if ("0".equals(code)) {
                return TestResultaat.ok("Bericht succesvol verstuurd naar Smartschool-inbox.");
            }
            return TestResultaat.fout(beschrijfErrorCode(code) + " — afzender='" + afzenderGn + "', ontvanger='" + ontvangerGn + "'");
        }
        return TestResultaat.fout("Onverwachte Smartschool-response (HTTP " + response.getStatusCode().value() + "): "
                + (body.length() > 300 ? body.substring(0, 300) + "..." : body));
    }

    private static String beschrijfErrorCode(String code) {
        String hint = switch (code) {
            case "1" -> "Ongeldige accesscode (API-key)";
            case "2" -> "Ongeldige parameters";
            case "3" -> "Ontvanger niet gevonden in deze Smartschool-omgeving";
            case "7" -> "Geen toegang tot deze webservice";
            case "8" -> "Foutieve afzender-parameter";
            case "9" -> "Foutieve coc";
            case "10" -> "Foutieve copyToLVS";
            case "12" -> "Afzender niet gevonden in Smartschool";
            default -> "Smartschool gaf foutcode " + code + " terug";
        };
        return hint + " (code " + code + ")";
    }

    private String soapEnvelope(String apiKey, String ontvanger, String titel, String bericht, String afzender) {
        return "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
                + "<soap:Envelope xmlns:soap=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:ss=\"https://oauth.smartschool.be/Webservices/V3\">"
                + "<soap:Body>"
                + "<ss:sendMsg>"
                + "<accesscode>" + xml(apiKey) + "</accesscode>"
                + "<userIdentifier>" + xml(ontvanger) + "</userIdentifier>"
                + "<title>" + xml(titel) + "</title>"
                + "<body>" + xml(bericht) + "</body>"
                + "<senderIdentifier>" + xml(afzender) + "</senderIdentifier>"
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

    public record TestResultaat(boolean success, String boodschap) {
        public static TestResultaat ok(String msg) { return new TestResultaat(true, msg); }
        public static TestResultaat fout(String msg) { return new TestResultaat(false, msg); }
    }
}
