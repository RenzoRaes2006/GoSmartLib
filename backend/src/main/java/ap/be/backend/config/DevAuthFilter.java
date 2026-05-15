package ap.be.backend.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Profile;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

/**
 * Alleen actief onder profile "dev". Zet een vaste fake-gebruiker in de
 * SecurityContext, zodat afgeschermde endpoints lokaal benaderbaar zijn
 * zonder Smartschool-login.
 *
 * Rol opgeven via header X-Dev-Role (STUDENT/TEACHER/BEHEERDER/ADMIN), default
 * BEHEERDER.
 */
@Component
@Profile("dev")
public class DevAuthFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest req, HttpServletResponse res, FilterChain chain)
            throws ServletException, IOException {

        String role = req.getHeader("X-Dev-Role");
        System.out.println("DevAuthFilter: authenticating as dev-user-001 with role " + role);

        if (role == null || role.isBlank())
            role = "BEHEERDER";

        System.out.println();
        System.out.println("DevAuthFilter: authenticating as dev-user-001 with role " + role);

        var authorities = List.of(new SimpleGrantedAuthority("ROLE_" + role.toUpperCase()));
        var auth = new UsernamePasswordAuthenticationToken("12357", null, authorities);

        SecurityContextHolder.getContext().setAuthentication(auth);
        chain.doFilter(req, res);
    }
}