package ap.be.backend.config;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.util.Date;

/***
 * BESTAND: JwtService.java
 *
 * WAT IS EEN JWT-TOKEN?
 * JWT staat voor "JSON Web Token". Het is een versleuteld "paspoort" als string,
 * dat de frontend meestuurt bij elke API-call naar de backend.
 *
 * Een JWT-token ziet er zo uit:
 *   eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJFNExENlc...
 *
 * Hij bestaat uit 3 delen gescheiden door een punt (.):
 *   DEEL 1 (header):   versleutelingstype (HS256)
 *   DEEL 2 (payload):  de gegevens (wie ben je, wanneer verloopt de token)
 *   DEEL 3 (signature): bewijs dat de token echt van onze server komt
 *
 * Je kan een token decoderen (lezen) op: https://jwt.io
 * (de inhoud is leesbaar voor iedereen, maar niet aanpasbaar zonder de geheime sleutel)
 *
 * WAT DOET DIT BESTAND?
 * 3 dingen:
 *   1. generateToken()         -> token aanmaken na het inloggen
 *   2. extractSmartschoolId()  -> wie is de gebruiker? (uit een bestaande token lezen)
 *   3. isTokenValid()          -> is de token niet verlopen of vervalst?
 ***/
@Service
public class JwtService {

    /***
     * De geheime sleutel waarmee de tokens versleuteld worden.
     * Gelezen uit application.properties: app.jwt.secret
     *
     * BELANGRIJK: Deze sleutel moet geheim blijven. Als iemand anders deze sleutel kent,
     * kan hij valse tokens aanmaken en als om het even wie inloggen.
     * In productie: zet dit in een environment variable, niet in application.properties.
     ***/
    @Value("${app.jwt.secret}")
    private String jwtSecret;

    /***
     * Zet de geheime string om naar een cryptografische sleutel.
     * HMAC-SHA256 is het versleutelingsalgoritme dat we gebruiken.
     ***/
    private Key getSigningKey() {
        return Keys.hmacShaKeyFor(jwtSecret.getBytes());
    }

    /***
     * Hoe lang is de token geldig?
     * 86400000 milliseconden = 86400 seconden = 1440 minuten = 24 uur
     * Na 24 uur moet de gebruiker opnieuw inloggen.
     ***/
    private final long expiration = 86400000;

    /***
     * TOKEN AANMAKEN
     *
     * Wordt aangeroepen in OAuth2LoginSuccessHandler.java na een succesvolle login.
     * De token bevat:
     *   - subject (sub): de smartschoolId (unieke identifier van de gebruiker)
     *   - role: de rol (student / teacher), opgehaald via OneRoster
     *   - name: de weergavenaam, afkomstig van Smartschool
     *   - klas: de klasnaam (bv. "2SOF"), opgehaald via OneRoster
     *   - iat (issued at): wanneer aangemaakt
     *   - exp (expiration): wanneer vervalt de token (24 uur)
     *
     * @param smartschoolId  de unieke ID van de gebruiker in Smartschool
     * @param rol            "student" of "teacher" (van OneRoster)
     * @param naam           de weergavenaam (van Smartschool)
     * @param klas           de klasnaam bv. "2SOF" (van OneRoster), of "onbekend"
     ***/
    public String generateToken(String smartschoolId, String rol, String naam, String klas) {
        return Jwts.builder()
                .setSubject(smartschoolId)       // wie is de gebruiker (ID)
                .claim("role", rol)              // wat is zijn rol (van OneRoster)
                .claim("name", naam)             // wat is zijn naam (van Smartschool)
                .claim("klas", klas)             // in welke klas zit hij (van OneRoster)
                .setIssuedAt(new Date())         // wanneer aangemaakt
                .setExpiration(new Date(System.currentTimeMillis() + expiration)) // wanneer vervalt
                .signWith(getSigningKey(), SignatureAlgorithm.HS256) // digitale handtekening
                .compact();                      // alles samenvoegen tot één string
    }

    /***
     * SMARTSCHOOL ID UITLEZEN UIT TOKEN
     *
     * Wordt gebruikt in JwtAuthenticationFilter.java om te weten WIE de request verstuurt.
     * "subject" (sub) in de token = de smartschoolId die we bij het aanmaken hebben ingesteld.
     ***/
    public String extractSmartschoolId(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody()
                .getSubject();
    }

    public String extractRole(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody()
                .get("role", String.class);
    }

    /***
     * TOKEN GELDIGHEID CONTROLEREN
     *
     * Wordt gebruikt in JwtAuthenticationFilter.java bij elke inkomende request.
     * Controleert of:
     *   - de token niet verlopen is (vervaldatum)
     *   - de token niet vervalst is (digitale handtekening klopt)
     *
     * Geeft true terug als de token geldig is, false als er iets mis is.
     ***/
    public boolean isTokenValid(String token) {
        try {
            Jwts.parserBuilder().setSigningKey(getSigningKey()).build().parseClaimsJws(token);
            return true;
        } catch (Exception e) {
            // Token is verlopen, vervalst, of beschadigd -> gebruiker moet opnieuw inloggen
            return false;
        }
    }

}
