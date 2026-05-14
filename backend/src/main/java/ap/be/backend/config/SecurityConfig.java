package ap.be.backend.config;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Centrale beveiligingsconfiguratie van de applicatie.
 *
 * Productie/standaard: elke request wordt geauthenticeerd via JWT
 * (JwtAuthenticationFilter) of via OAuth2-login bij Smartschool.
 *
 * Dev-profiel ("dev"): OAuth2/JWT wordt vervangen door DevAuthFilter, dat een
 * vaste testgebruiker injecteert. Zo kan je lokaal werken zonder Smartschool.
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    private final OAuth2LoginSuccessHandler successHandler;
    private final JwtAuthenticationFilter jwtAuthFilter;

    public SecurityConfig(OAuth2LoginSuccessHandler successHandler, JwtAuthenticationFilter jwtAuthFilter) {
        this.successHandler = successHandler;
        this.jwtAuthFilter = jwtAuthFilter;
    }

    @Autowired
    private Environment env;

    @Autowired(required = false)
    private DevAuthFilter devAuthFilter;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        boolean isDev = List.of(env.getActiveProfiles()).contains("dev");

        http
                .cors(Customizer.withDefaults())
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/", "/login", "/error", "/api/boeken/**", "/api/auth/admin/login").permitAll()
                        .anyRequest().authenticated());

        if (isDev && devAuthFilter != null) {
            // Lokaal: enkel DevAuthFilter, geen OAuth2-redirect naar Smartschool,
            // geen JWT-check. Onbekende requests worden geweigerd met 401 i.p.v.
            // een redirect (zo blijft CORS werken voor de Angular dev-server).
            http
                    .addFilterBefore(devAuthFilter, UsernamePasswordAuthenticationFilter.class)
                    .exceptionHandling(e -> e.authenticationEntryPoint(
                            (req, res, ex) -> res.sendError(401, "Unauthorized (dev)")));
        } else {
            http
                    .oauth2Login(oauth2 -> oauth2.successHandler(successHandler))
                    .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);
        }

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
