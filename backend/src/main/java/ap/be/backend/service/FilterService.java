package ap.be.backend.service;

import org.springframework.stereotype.Service;
import jakarta.annotation.PostConstruct;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.Buffer;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class FilterService {

    private Pattern verbodenWoordenPattern;

    @PostConstruct
    public void laadWoordenLijst() {
        List<String> verbodenWoorden = new ArrayList<>();

        try {
            // Zoek het bestand in de resources map
            InputStream is = getClass().getResourceAsStream("/scheldwoorden.txt");
            if (is == null) {
                System.out.println("Bestand 'scheldwoorden.txt' niet gevonden in resources.");
                return;
            }

            // Lees het bestand regel voor regel uit
            BufferedReader reader = new BufferedReader(new InputStreamReader(is));
            String regel;
            while ((regel = reader.readLine()) != null) {
                if (!regel.trim().isEmpty()) {
                    // Voeg het woord toe aan de lijst van verboden woorden, met regex-escaping
                    verbodenWoorden.add(Pattern.quote(regel.trim()));
                }
            }
            reader.close();

            // Maak één grote Regex van alle woorden: (?i)\b(woord1|woord2)\b
            String regex = "(?i)\\b(" + String.join("|", verbodenWoorden) + ")\\b";
            this.verbodenWoordenPattern = Pattern.compile(regex);

        } catch (Exception e) {
            System.err.println("Fout bij het laden van de woordenlijst: " + e.getMessage());
        }
    }

    // Deze methode roep je straks aan vanuit je BeoordelingService
    public boolean bevatScheldwoorden(String tekst) {
        if (tekst == null || verbodenWoordenPattern == null) {
            return false;
        }
        Matcher matcher = verbodenWoordenPattern.matcher(tekst);
        return matcher.find();
    }
}
