package ap.be.backend.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

/***
 * BESTAND: JwtAuthenticationFilter.java
 *
 * WAT DOET DIT BESTAND?
 * Dit is een "filter" — een stuk code dat bij ELKE request naar de backend wordt uitgevoerd,
 * VOORDAT de request bij de eigenlijke controller aankomt (bv. BoekController).
 *
 * Het enige doel van dit bestand is:
 *   "Heeft deze request een geldige JWT-token? Zo ja, wie is de gebruiker?"
 *
 * HOE WERKT HET?
 * De Angular frontend stuurt bij elke API-call een header mee:
 *   Authorization: Bearer eyJhbGciOiJIUzI1NiJ9...
 *
 * Dit filter leest die header, valideert de token, en vertelt Spring Security
 * wie de ingelogde gebruiker is. Daarna beslist SecurityConfig.java of die
 * gebruiker de gevraagde URL mag bezoeken.
 *
 * "OncePerRequestFilter" betekent: dit filter wordt maar 1 keer per request uitgevoerd
 * (Spring zou het anders soms meerdere keren aanroepen).
 ***/
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    /***
     * JwtService: de service die tokens kan lezen en valideren.
     * We gebruiken die hier om de token te controleren en de gebruiker-ID eruit te halen.
     ***/
    @Autowired
    private JwtService jwtService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        /***
         * Lees de "Authorization" header uit de request.
         * De frontend stuurt dit mee als:
         *   Authorization: Bearer eyJhbGciOiJIUzI1NiJ9...
         *
         * Als er geen Authorization header is, of hij begint niet met "Bearer ",
         * dan is deze gebruiker niet ingelogd. We gaan dan gewoon verder (filterChain.doFilter)
         * en SecurityConfig.java zal de request blokkeren als de URL beschermd is.
         ***/
        String authHeader = request.getHeader("Authorization");

        if (authHeader != null && authHeader.startsWith("Bearer ")) {

            /***
             * Knip "Bearer " (7 tekens) weg om enkel de token zelf over te houden.
             * Voorbeeld:
             *   authHeader = "Bearer eyJhbGci..."
             *   token      = "eyJhbGci..."
             ***/
            String token = authHeader.substring(7);

            /***
             * Controleer of de token geldig is (niet verlopen, niet vervalst).
             * JwtService.isTokenValid() doet deze controle.
             ***/
            if (jwtService.isTokenValid(token)) {

                /***
                 * Haal de smartschoolId op uit de token.
                 * Dit is de unieke identifier van de gebruiker die we bij het
                 * aanmaken van de token hebben opgeslagen als "subject".
                 ***/
                String smartschoolId = jwtService.extractSmartschoolId(token);
                String role = jwtService.extractRole(token);

                List<SimpleGrantedAuthority> authorities = List.of(
                        new SimpleGrantedAuthority("ROLE_" + role.toUpperCase())
                );

                UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                        smartschoolId, null, authorities);

                /***
                 * Sla de herkende gebruiker op in de SecurityContext.
                 * De SecurityContext is een tijdelijke opslag per request.
                 * SecurityConfig.java leest hieruit of de gebruiker is ingelogd.
                 ***/
                SecurityContextHolder.getContext().setAuthentication(authToken);
            }
        }

        /***
         * Geef de request door aan het volgende onderdeel in de keten.
         * Dit is altijd nodig, of de token nu geldig was of niet.
         * Zonder deze regel zou de request nooit bij de controller aankomen.
         ***/
        filterChain.doFilter(request, response);
    }
}
