package ap.be.backend.config;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.util.Date;

/**
 * Verantwoordelijk voor het aanmaken, lezen en valideren van JWT-tokens.
 *
 * Een JWT (JSON Web Token) is een ondertekende string die de frontend bij elke
 * API-call meestuurt in de Authorization-header. De token bewijst wie de
 * gebruiker is, zodat de backend geen sessie hoeft bij te houden.
 *
 * De token bestaat uit drie delen gescheiden door een punt:
 *   - Header:    het gebruikte algoritme (HS256).
 *   - Payload:   de gegevens (gebruiker, rol, vervaldatum).
 *   - Signature: een digitale handtekening die garandeert dat de token
 *                door deze server is aangemaakt en niet is aangepast.
 *
 * De inhoud van een token is leesbaar voor iedereen, maar zonder de geheime
 * sleutel kan niemand een geldige token vervalsen.
 */
@Service
public class JwtService {

    @Value("${app.jwt.secret}")
    private String jwtSecret;

    private final long expiration = 86_400_000L;

    private Key getSigningKey() {
        return Keys.hmacShaKeyFor(jwtSecret.getBytes());
    }

    /**
     * Maakt een nieuwe JWT-token aan voor een succesvol ingelogde gebruiker.
     *
     * Wordt aangeroepen door OAuth2LoginSuccessHandler na een Smartschool-login
     * en door AdminAuthController na een beheerder-login. De resulterende token
     * is 24 uur geldig.
     *
     * De token bevat de volgende gegevens:
     *   - sub:  de smartschoolId of admin-gebruikersnaam (unieke identificatie).
     *   - role: de rol van de gebruiker ("student", "teacher" of "beheerder").
     *   - name: de weergavenaam.
     *   - klas: de klasnaam (bv. "2SOF"), of "nvt" voor beheerders.
     *   - iat:  tijdstip van uitgifte.
     *   - exp:  tijdstip van verval (24 uur na uitgifte).
     *
     * @param smartschoolId unieke identifier van de gebruiker
     * @param rol           rol van de gebruiker
     * @param naam          weergavenaam
     * @param klas          klasnaam, of "nvt" indien niet van toepassing
     * @return de ondertekende JWT-token als compacte string
     */
    public String generateToken(String smartschoolId, String rol, String naam, String klas) {
        return Jwts.builder()
                .setSubject(smartschoolId)
                .claim("role", rol)
                .claim("name", naam)
                .claim("klas", klas)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    private final long passwordResetExpiration = 15 * 60 * 1000L;

    /**
     * Maakt een kortlevende token die uitsluitend bedoeld is om bij eerste
     * login het wachtwoord te wijzigen. De scope-claim zorgt dat dit token
     * niet als gewoon admin-token misbruikt kan worden.
     */
    public String generatePasswordResetToken(String gebruikersnaam) {
        return Jwts.builder()
                .setSubject(gebruikersnaam)
                .claim("scope", "wachtwoord-wijzigen")
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + passwordResetExpiration))
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    public String extractScope(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody()
                .get("scope", String.class);
    }

    /**
     * Leest de smartschoolId (subject-claim) uit een bestaande token.
     *
     * Wordt gebruikt door JwtAuthenticationFilter om bij elke binnenkomende
     * request te bepalen welke gebruiker de aanvraag doet.
     *
     * @param token de JWT-token
     * @return de smartschoolId die als subject in de token staat
     */
    public String extractSmartschoolId(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody()
                .getSubject();
    }

    /**
     * Leest de rol-claim uit een bestaande token.
     *
     * @param token de JWT-token
     * @return de rol als string ("student", "teacher" of "beheerder")
     */
    public String extractRole(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody()
                .get("role", String.class);
    }

    /**
     * Controleert of een token nog geldig en ongewijzigd is.
     *
     * Een token is geldig als de handtekening klopt (de token is door deze
     * server aangemaakt) en de vervaldatum nog niet bereikt is. Bij elke
     * andere uitkomst — verlopen, vervalst, beschadigd of leeg — geeft
     * deze methode false terug en moet de gebruiker opnieuw inloggen.
     *
     * @param token de te controleren token
     * @return true als de token geldig is, anders false
     */
    public boolean isTokenValid(String token) {
        try {
            Jwts.parserBuilder().setSigningKey(getSigningKey()).build().parseClaimsJws(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
