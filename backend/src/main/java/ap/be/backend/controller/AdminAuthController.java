package ap.be.backend.controller;

import ap.be.backend.config.JwtService;
import ap.be.backend.dto.AdminLoginRequest;
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

        String token = jwtService.generateToken(
                "admin_" + admin.getGebruikersnaam(),
                "beheerder",
                admin.getNaam(),
                "nvt"
        );

        return ResponseEntity.ok(Map.of("token", token));
    }
}
