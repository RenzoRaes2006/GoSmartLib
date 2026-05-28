package ap.be.backend.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

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
import ap.be.backend.entity.School;
import ap.be.backend.entity.User;
import ap.be.backend.exception.GebruikerNotFoundException;
import ap.be.backend.exception.OngeldigeRolException;
import ap.be.backend.mappers.GebruikerMapper;
import ap.be.backend.repository.UserRepository;
import ap.be.backend.service.GebruikerService;
import ap.be.backend.service.OneRosterClient;

@RestController
@RequestMapping("/api/beheer")
@PreAuthorize("hasRole('BEHEERDER')")
public class BeheerController {

    private final GebruikerService gebruikerService;
    private final UserRepository userRepository;
    private final OneRosterClient oneRosterClient;

    public BeheerController(GebruikerService gebruikerService, UserRepository userRepository,
                            OneRosterClient oneRosterClient) {
        this.gebruikerService = gebruikerService;
        this.userRepository = userRepository;
        this.oneRosterClient = oneRosterClient;
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
        School school = beheerder.getSchool();
        if (school == null) {
            return ResponseEntity.ok(List.of());
        }
        List<User> leerkrachten = userRepository.findByRolAndSchool_Id(Rol.LEERKRACHT, school.getId())
                .stream()
                .filter(User::isActief)
                .toList();

        Set<String> ids = leerkrachten.stream().map(User::getSmartschoolId).collect(Collectors.toSet());
        Map<String, String[]> namenLookup = haalNamenOp(school, ids);

        List<GebruikerDTO> result = leerkrachten.stream()
                .map(u -> {
                    String[] paar = namenLookup.getOrDefault(u.getSmartschoolId(), new String[]{null, null});
                    return GebruikerMapper.toDTO(u, paar[0], paar[1]);
                })
                .toList();
        return ResponseEntity.ok(result);
    }

    @SuppressWarnings("unchecked")
    private Map<String, String[]> haalNamenOp(School school, Set<String> gewensteIds) {
        if (gewensteIds == null || gewensteIds.isEmpty()
                || school.getSmartschoolDomein() == null
                || school.getOauthClientId() == null
                || school.getOauthClientSecret() == null) {
            return Map.of();
        }
        try {
            Map<String, Object> data = oneRosterClient.haalLiveSchoolDataOp(
                    school.getSmartschoolDomein(),
                    school.getOauthClientId(),
                    school.getOauthClientSecret());

            Map<String, String[]> lookup = new HashMap<>();
            for (String sleutel : new String[]{"leerkrachten", "leerlingen"}) {
                List<Map<String, Object>> lijst = (List<Map<String, Object>>) data.getOrDefault(sleutel, List.of());
                for (Map<String, Object> u : lijst) {
                    String id = String.valueOf(u.getOrDefault("id", ""));
                    if (id.isBlank() || !gewensteIds.contains(id)) continue;
                    String voornaam = String.valueOf(u.getOrDefault("voorNaam", ""));
                    String achternaam = String.valueOf(u.getOrDefault("achternaam", ""));
                    if (!voornaam.isBlank() || !achternaam.isBlank()) {
                        lookup.put(id, new String[]{voornaam, achternaam});
                    }
                }
            }
            return lookup;
        } catch (Exception e) {
            return Map.of();
        }
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