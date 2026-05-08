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

/***
 * BESTAND: SecurityConfig.java
 *
 * WAT DOET DIT BESTAND?
 * Dit is het "toegangspoort"-bestand van de backend.
 * Elke HTTP-request die binnenkomt, passeert langs de regels die hier zijn ingesteld.
 * Hier beslis je:
 *   - Welke pagina's mag iedereen zien (zonder in te loggen)?
 *   - Welke pagina's zijn enkel voor ingelogde gebruikers?
 *   - Hoe logt een gebruiker in? (via Smartschool OAuth2)
 *   - Hoe controleer je of iemand al ingelogd is? (via JWT-token)
 *
 * VOLGORDE VAN VERWERKING:
 * Elke request doorloopt dit pad:
 *   1. JwtAuthenticationFilter (check: heeft deze request een geldige token?)
 *   2. Spring Security regels (mag deze gebruiker deze URL bezoeken?)
 *   3. De echte controller (bv. BoekController, GebruikerController...)
 ***/
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

        /***
         * filterChain = de volledige beveiligingsconfiguratie als één aaneengesloten keten.
         * Spring roept deze methode op bij het opstarten om te weten hoe beveiliging werkt.
         ***/
        @Bean
        public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
                http
                                /***
                                 * CORS (Cross-Origin Resource Sharing):
                                 * De frontend (Angular) draait op een andere URL dan de backend.
                                 * Zonder CORS zou de browser de requests blokkeren.
                                 * withDefaults() gebruikt de CORS-instellingen uit CorsConfig.java.
                                 ***/
                                .cors(Customizer.withDefaults())

                                /***
                                 * CSRF uitschakelen:
                                 * CSRF-bescherming is bedoeld voor traditionele websites met sessies.
                                 * Wij gebruiken JWT-tokens (stateless) -> CSRF is niet nodig en zou
                                 * onze API-calls vanuit Angular blokkeren.
                                 ***/
                                .csrf(csrf -> csrf.disable())

                                /***
                                 * TOEGANGSREGELS per URL:
                                 * - /api/boeken/** -> iedereen mag dit zien (ook niet-ingelogden)
                                 * - alle andere URLs -> je moet ingelogd zijn (geldige JWT-token)
                                 ***/
                                .authorizeHttpRequests(auth -> auth
                                                .requestMatchers("/", "/login", "/error", "/api/boeken/**", "/api/auth/admin/login").permitAll()
                                                .anyRequest().authenticated())

                                /***
                                 * OAuth2 LOGIN via Smartschool:
                                 * Spring regelt zelf de redirect naar Smartschool en het ophalen
                                 * van de gebruikersdata. Zodra dat klaar is, roept Spring onze
                                 * successHandler aan (OAuth2LoginSuccessHandler.java).
                                 ***/
                                .oauth2Login(oauth2 -> oauth2
                                                .successHandler(successHandler)
                                )

                                /***
                                 * JWT FILTER TOEVOEGEN:
                                 * Voeg onze JwtAuthenticationFilter toe VÓÓR de standaard
                                 * username/password filter van Spring. Zo wordt bij elke request
                                 * eerst de JWT-token gecontroleerd.
                                 ***/
                                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

                return http.build();
        }

        @Bean
        public PasswordEncoder passwordEncoder() {
                return new BCryptPasswordEncoder();
        }
}
