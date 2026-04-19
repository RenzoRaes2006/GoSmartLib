package ap.be.backend.config;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.util.Date;

@Service
public class JwtService {

    // Genereer een veilige sleutel voor de versleuteling (HMAC-SHA256)
    // hier wordt de key elke keer opnieuw gegenereerd, wat niet wenselijk is
    // private final Key key = Keys.secretKeyFor(SignatureAlgorithm.HS256);
    @Value("${app.jwt.secret}")
    private String jwtSecret;

    // Helper methode om de sleutel te verkrijgen
    private Key getSigningKey() {
        return Keys.hmacShaKeyFor(jwtSecret.getBytes());
    }

    // Token is 1 dag geldig (in milliseconden)
    private final long expiration = 86400000;

    public String generateToken(String smartschoolId) {
        return Jwts.builder()
                .setSubject(smartschoolId) // Hier stoppen we enkel de ID in de token
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    public String extractSmartschoolId(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody()
                .getSubject();
    }

    public boolean isTokenValid(String token) {
        try {
            Jwts.parserBuilder().setSigningKey(getSigningKey()).build().parseClaimsJws(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

}