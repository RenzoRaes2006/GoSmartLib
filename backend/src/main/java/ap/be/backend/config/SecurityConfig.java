package ap.be.backend.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.config.Customizer;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

        @Autowired
        private OAuth2LoginSuccessHandler successHandler;

        // In SecurityConfig.java
        @Autowired
        private JwtAuthenticationFilter jwtAuthFilter;

        @Bean
        public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
                http
                                .cors(Customizer.withDefaults()) // 1. Activeer CORS voor Angular
                                .csrf(csrf -> csrf.disable()) // 2. Zet CSRF uit (nodig voor JWT)
                                .authorizeHttpRequests(auth -> auth
                                                .requestMatchers("/", "/login", "/error", "/api/boeken/**").permitAll()
                                                .anyRequest().authenticated())
                                .oauth2Login(oauth2 -> oauth2
                                                .successHandler(successHandler) // Hier koppelen we jouw logica
                                )
                                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

                return http.build();
        }
}
