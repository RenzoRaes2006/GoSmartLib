package ap.be.backend.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

/**
 * Spring Security-filter dat bij elke inkomende HTTP-request de JWT-token
 * controleert en de identiteit van de gebruiker registreert.
 *
 * Dit filter wordt uitgevoerd vóór elke controller. Het leest de
 * Authorization-header, valideert de token via JwtService, en plaatst de
 * herkende gebruiker in de SecurityContextHolder. Op basis daarvan beslist
 * SecurityConfig of de request mag doorgaan naar de controller.
 *
 * Door OncePerRequestFilter te implementeren is gegarandeerd dat het filter
 * exact één keer per request draait, ook bij interne forwards.
 */
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;

    public JwtAuthenticationFilter(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    /**
     * Verwerkt de authenticatie voor één HTTP-request.
     *
     * Stap 1: Lees de Authorization-header. Ontbreekt deze of begint hij niet
     *         met "Bearer ", dan blijft de request anoniem en beslist
     *         SecurityConfig of toegang is toegestaan.
     *
     * Stap 2: Knip het voorvoegsel "Bearer " weg om de token zelf over te
     *         houden.
     *
     * Stap 3: Valideer de token via JwtService.isTokenValid.
     *
     * Stap 4: Lees bij een geldige token de smartschoolId en de rol uit de
     *         token. Vertaal de rol naar een Spring-authority (ROLE_STUDENT,
     *         ROLE_TEACHER, ROLE_BEHEERDER of ROLE_ADMIN) en sla het authenticatie-object
     *         op in de SecurityContextHolder.
     *
     * Stap 5: Geef de request altijd door aan het volgende filter in de keten,
     *         ongeacht of authenticatie is gelukt.
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String authHeader = request.getHeader("Authorization");

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);

            if (jwtService.isTokenValid(token)) {
                String smartschoolId = jwtService.extractSmartschoolId(token);
                String role = jwtService.extractRole(token);

                List<SimpleGrantedAuthority> authorities = List.of(
                        new SimpleGrantedAuthority("ROLE_" + role.toUpperCase())
                );

                UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                        smartschoolId, null, authorities);

                SecurityContextHolder.getContext().setAuthentication(authToken);
            }
        }

        filterChain.doFilter(request, response);
    }
}
