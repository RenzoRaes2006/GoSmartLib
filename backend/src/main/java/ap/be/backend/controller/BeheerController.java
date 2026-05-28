package ap.be.backend.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import ap.be.backend.config.SecurityUtils;
import ap.be.backend.dto.GebruikerDTO;
import ap.be.backend.dto.UitleningSummaryDTO;
import ap.be.backend.entity.Rol;
import ap.be.backend.entity.User;
import ap.be.backend.exception.GebruikerNotFoundException;
import ap.be.backend.exception.OngeldigeRolException;
import ap.be.backend.mappers.GebruikerMapper;
import ap.be.backend.repository.UserRepository;
import ap.be.backend.service.GebruikerService;

@RestController
@RequestMapping("/api/beheer")
@PreAuthorize("hasRole('BEHEERDER')")
public class BeheerController {

    private final GebruikerService gebruikerService;
    private final UserRepository userRepository;

    public BeheerController(GebruikerService gebruikerService, UserRepository userRepository) {
        this.gebruikerService = gebruikerService;
        this.userRepository = userRepository;
    }

    @GetMapping("/leningen/actief")
    public ResponseEntity<List<UitleningSummaryDTO>> getAlleActieveLeningenSysteem() {
        return ResponseEntity.ok(gebruikerService.getAlleActieveLeningenSysteemDTO());
    }

    @GetMapping("/verlengingen/aanvragen")
    public ResponseEntity<List<UitleningSummaryDTO>> getAlleVerlengingAanvragen() {
        return ResponseEntity.ok(gebruikerService.getAlleVerlengingAanvragenDTO());
    }

    @PutMapping("/{id}/verlenging/goedkeuren")
    public ResponseEntity<Void> keurVerlengingGoed(@PathVariable Long id) {
        gebruikerService.keurVerlengingGoed(id);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/{id}/verlenging/weigeren")
    public ResponseEntity<Void> weigerVerlenging(@PathVariable Long id) {
        gebruikerService.weigerVerlenging(id);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/leerkrachten")
    public ResponseEntity<List<GebruikerDTO>> getLeerkrachtenVanMijnSchool() {
        User beheerder = SecurityUtils.getCurrentUser(userRepository);
        if (beheerder.getSchool() == null) {
            return ResponseEntity.ok(List.of());
        }
        Long schoolId = beheerder.getSchool().getId();
        List<GebruikerDTO> leerkrachten = userRepository.findByRolAndSchool_Id(Rol.LEERKRACHT, schoolId)
                .stream()
                .filter(User::isActief)
                .map(GebruikerMapper::toDTO)
                .toList();
        return ResponseEntity.ok(leerkrachten);
    }

    public record BeheerderToewijzingRequest(Boolean isBeheerder) {}

    @PatchMapping("/gebruikers/{id}/beheerder")
    public ResponseEntity<GebruikerDTO> setBeheerderVanMijnSchool(
            @PathVariable Long id,
            @RequestBody BeheerderToewijzingRequest request) {
        if (request == null || request.isBeheerder() == null) {
            throw new OngeldigeRolException("Veld isBeheerder is verplicht");
        }

        User huidigeBeheerder = SecurityUtils.getCurrentUser(userRepository);
        User doelUser = userRepository.findById(id)
                .orElseThrow(() -> new GebruikerNotFoundException("Gebruiker met id " + id + " niet gevonden"));

        Long mijnSchoolId = huidigeBeheerder.getSchool() != null ? huidigeBeheerder.getSchool().getId() : null;
        Long doelSchoolId = doelUser.getSchool() != null ? doelUser.getSchool().getId() : null;
        if (mijnSchoolId == null || !mijnSchoolId.equals(doelSchoolId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        if (doelUser.getRol() != Rol.LEERKRACHT) {
            throw new OngeldigeRolException("Enkel een LEERKRACHT kan beheerder worden");
        }

        doelUser.setBeheerder(request.isBeheerder());
        return ResponseEntity.ok(GebruikerMapper.toDTO(userRepository.save(doelUser)));
    }
}