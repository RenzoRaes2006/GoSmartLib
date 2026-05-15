package ap.be.backend.controller;

import ap.be.backend.config.JwtService;
import ap.be.backend.dto.AdminLoginRequest;
import ap.be.backend.dto.WachtwoordWijzigenRequest;
import ap.be.backend.entity.AdminGebruiker;
import ap.be.backend.repository.AdminGebruikerRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/auth")
public class AdminAuthController {

    private final AdminGebruikerRepository adminGebruikerRepository;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;

    public AdminAuthController(AdminGebruikerRepository adminGebruikerRepository, JwtService jwtService,
            PasswordEncoder passwordEncoder) {
        this.adminGebruikerRepository = adminGebruikerRepository;
        this.jwtService = jwtService;
        this.passwordEncoder = passwordEncoder;
    }

    @PostMapping("/admin/login")
    public ResponseEntity<?> adminLogin(@RequestBody AdminLoginRequest request) {
        Optional<AdminGebruiker> adminOpt = adminGebruikerRepository.findByGebruikersnaam(request.getGebruikersnaam());

        if (adminOpt.isEmpty() || !passwordEncoder.matches(request.getWachtwoord(), adminOpt.get().getWachtwoordHash())) {
            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("fout", "Ongeldig gebruikersnaam of wachtwoord"));
        }

        AdminGebruiker admin = adminOpt.get();

        if (admin.isMoetWachtwoordWijzigen()) {
            String resetToken = jwtService.generatePasswordResetToken(admin.getGebruikersnaam());
            return ResponseEntity.ok(Map.of(
                    "moetWachtwoordWijzigen", true,
                    "tijdelijkToken", resetToken
            ));
        }

        String token = jwtService.generateToken(
                "admin_" + admin.getGebruikersnaam(),
                "admin",
                admin.getNaam(),
                "nvt"
        );

        return ResponseEntity.ok(Map.of("token", token));
    }

    @PostMapping("/admin/wachtwoord-wijzigen")
    public ResponseEntity<?> wachtwoordWijzigen(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestBody WachtwoordWijzigenRequest request) {

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("fout", "Geen geldig token"));
        }
        String resetToken = authHeader.substring(7);

        if (!jwtService.isTokenValid(resetToken)
                || !"wachtwoord-wijzigen".equals(jwtService.extractScope(resetToken))) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("fout", "Token niet geldig voor wachtwoord wijzigen"));
        }

        String nieuw = request.getNieuwWachtwoord();
        if (nieuw == null || nieuw.length() < 8) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("fout", "Wachtwoord moet minstens 8 tekens lang zijn"));
        }

        String gebruikersnaam = jwtService.extractSmartschoolId(resetToken);
        Optional<AdminGebruiker> adminOpt = adminGebruikerRepository.findByGebruikersnaam(gebruikersnaam);
        if (adminOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("fout", "Admin niet gevonden"));
        }

        AdminGebruiker admin = adminOpt.get();
        admin.setWachtwoordHash(passwordEncoder.encode(nieuw));
        admin.setMoetWachtwoordWijzigen(false);
        adminGebruikerRepository.save(admin);

        String token = jwtService.generateToken(
                "admin_" + admin.getGebruikersnaam(),
                "admin",
                admin.getNaam(),
                "nvt"
        );
        return ResponseEntity.ok(Map.of("token", token));
    }
}
