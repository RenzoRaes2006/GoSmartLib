package ap.be.backend.config;

import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

/**
 * Configureert CORS (Cross-Origin Resource Sharing) voor de applicatie.
 *
 * Zonder CORS-configuratie blokkeren browsers API-calls van de Angular-frontend
 * naar de backend, omdat ze op verschillende domeinen draaien. Deze klasse
 * staat expliciet toe welke origins, HTTP-methoden en headers zijn toegestaan.
 *
 * De toegestane origins worden ingelezen uit application.properties via
 * app.cors.allowed-origins.
 */
@Configuration
public class CorsConfig {

    @Value("${app.cors.allowed-origins}")
    private List<String> allowedOrigins;

    /**
     * Registreert de CORS-configuratie voor alle endpoints.
     *
     * Toegestane methoden: GET, POST, PUT, PATCH, DELETE, OPTIONS.
     * Toegestane headers: alle headers.
     * Credentials (cookies, Authorization-header) zijn toegestaan.
     *
     * @return de geconfigureerde UrlBasedCorsConfigurationSource
     */
    @Bean
    public UrlBasedCorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(allowedOrigins);
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("*"));
        config.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}
