package ap.be.backend.service;

import org.springframework.stereotype.Service;
import jakarta.annotation.PostConstruct;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Controleert of een tekst verboden woorden bevat.
 *
 * Bij het opstarten van de applicatie laadt deze service een lijst van
 * verboden woorden uit het bestand resources/scheldwoorden.txt. Alle woorden
 * worden samengevoegd tot één reguliere expressie, zodat elke controle
 * efficiënt in één stap kan worden uitgevoerd.
 */
@Service
public class FilterService {

    private Pattern verbodenWoordenPattern;

    /**
     * Laadt de verboden-woordenlijst in bij het opstarten van de applicatie.
     *
     * Leest scheldwoorden.txt regel voor regel uit. Elk woord wordt omgezet
     * naar een letterlijke regex-string (Pattern.quote) om speciale tekens te
     * neutraliseren. Alle woorden worden samengevoegd tot één hoofdletterongevoelig
     * patroon van de vorm: (?i)\b(woord1|woord2|...)\b
     *
     * Als het bestand niet gevonden wordt of een fout optreedt, blijft het
     * patroon null en zal bevatScheldwoorden altijd false teruggeven.
     */
    @PostConstruct
    public void laadWoordenLijst() {
        List<String> verbodenWoorden = new ArrayList<>();

        try {
            InputStream is = getClass().getResourceAsStream("/scheldwoorden.txt");
            if (is == null) {
                return;
            }

            BufferedReader reader = new BufferedReader(new InputStreamReader(is));
            String regel;
            while ((regel = reader.readLine()) != null) {
                if (!regel.trim().isEmpty()) {
                    verbodenWoorden.add(Pattern.quote(regel.trim()));
                }
            }
            reader.close();

            String regex = "(?i)\\b(" + String.join("|", verbodenWoorden) + ")\\b";
            this.verbodenWoordenPattern = Pattern.compile(regex);

        } catch (Exception e) {
            System.err.println("Fout bij het laden van de woordenlijst: " + e.getMessage());
        }
    }

    /**
     * Controleert of de gegeven tekst een of meer verboden woorden bevat.
     *
     * Geeft false terug als de tekst null is of als de woordenlijst niet
     * geladen kon worden.
     *
     * @param tekst de te controleren tekst
     * @return true als de tekst een verboden woord bevat, anders false
     */
    public boolean bevatScheldwoorden(String tekst) {
        if (tekst == null || verbodenWoordenPattern == null) {
            return false;
        }
        Matcher matcher = verbodenWoordenPattern.matcher(tekst);
        return matcher.find();
    }
}
