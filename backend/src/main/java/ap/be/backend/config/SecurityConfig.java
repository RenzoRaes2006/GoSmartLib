package ap.be.backend.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.config.Customizer;

/**
 * Centrale beveiligingsconfiguratie van de applicatie.
 *
 * Elke binnenkomende HTTP-request doorloopt drie stappen:
 *   1. JwtAuthenticationFilter — controleert of er een geldige JWT-token aanwezig is.
 *   2. De toegangsregels in filterChain — bepaalt of de gebruiker de gevraagde
 *      URL mag bezoeken.
 *   3. De controller — verwerkt de eigenlijke request.
 *
 * EnableMethodSecurity zorgt ervoor dat @PreAuthorize-annotaties op controllers
 * en services gerespecteerd worden.
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

    /**
     * Definieert de volledige beveiligingsketen van de applicatie.
     *
     * CORS: staat requests toe van de Angular-frontend, die op een andere
     * URL draait dan de backend. De toegestane origins staan in CorsConfig.
     *
     * CSRF: uitgeschakeld omdat de applicatie stateless is en JWT gebruikt.
     * CSRF-bescherming is alleen nodig bij traditionele sessie-gebaseerde
     * webapplicaties.
     *
     * Toegangsregels:
     *   - Openbaar toegankelijk: /, /login, /error, /api/boeken/**, /api/auth/admin/login.
     *   - Alle overige endpoints vereisen een geldige JWT-token.
     *
     * OAuth2-login: Spring regelt de redirect naar Smartschool en het ophalen
     * van de gebruikersgegevens. Na een succesvolle login roept Spring
     * OAuth2LoginSuccessHandler aan.
     *
     * JWT-filter: JwtAuthenticationFilter wordt toegevoegd vóór het standaard
     * Spring-authenticatiefilter, zodat elke request eerst op een geldige token
     * wordt gecontroleerd.
     *
     * @param http het door Spring geleverde HttpSecurity-object
     * @return de geconfigureerde SecurityFilterChain
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .cors(Customizer.withDefaults())
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/", "/login", "/error", "/api/boeken/**", "/api/auth/admin/login").permitAll()
                        .anyRequest().authenticated())
                .oauth2Login(oauth2 -> oauth2
                        .successHandler(successHandler))
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    /**
     * Levert de BCrypt-wachtwoordversleuteling die gebruikt wordt bij de
     * beheerder-login.
     *
     * @return een BCryptPasswordEncoder-instantie
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
