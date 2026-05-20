package ap.be.backend.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import ap.be.backend.dto.GebruikerDTO;
import ap.be.backend.entity.Rol;
import ap.be.backend.entity.School;
import ap.be.backend.entity.User;
import ap.be.backend.exception.GebruikerNotFoundException;
import ap.be.backend.exception.OngeldigeRolException;
import ap.be.backend.mappers.GebruikerMapper;
import ap.be.backend.repository.SchoolRepository;
import ap.be.backend.repository.UserRepository;
import ap.be.backend.service.OneRosterClient;

@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminGebruikerController {

    private final UserRepository userRepository;
    private final SchoolRepository schoolRepository;
    private final OneRosterClient oneRosterClient;

    public AdminGebruikerController(UserRepository userRepository,
                                    SchoolRepository schoolRepository,
                                    OneRosterClient oneRosterClient) {
        this.userRepository = userRepository;
        this.schoolRepository = schoolRepository;
        this.oneRosterClient = oneRosterClient;
    }

    @GetMapping("/leerkrachten")
    public ResponseEntity<List<GebruikerDTO>> getAlleLeerkrachten(
            @RequestParam(required = false) Long schoolId) {

        List<User> users;
        School school = null;
        if (schoolId != null) {
            Optional<School> schoolOpt = schoolRepository.findById(schoolId);
            if (schoolOpt.isEmpty()) return ResponseEntity.notFound().build();
            school = schoolOpt.get();
            users = userRepository.findByRolAndSchool_Id(Rol.LEERKRACHT, schoolId);
        } else {
            users = userRepository.findByRol(Rol.LEERKRACHT);
        }

        Map<String, String[]> naamPerSmartschoolId = haalNamenOpVoorSchool(school);

        List<GebruikerDTO> result = users.stream()
                .map(u -> {
                    String[] paar = naamPerSmartschoolId.getOrDefault(u.getSmartschoolId(), new String[]{null, null});
                    return GebruikerMapper.toDTO(u, paar[0], paar[1]);
                })
                .toList();
        return ResponseEntity.ok(result);
    }

    @SuppressWarnings("unchecked")
    private Map<String, String[]> haalNamenOpVoorSchool(School school) {
        if (school == null
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
                    String voornaam = String.valueOf(u.getOrDefault("voorNaam", ""));
                    String achternaam = String.valueOf(u.getOrDefault("achternaam", ""));
                    if (!id.isBlank() && (!voornaam.isBlank() || !achternaam.isBlank())) {
                        lookup.put(id, new String[]{voornaam, achternaam});
                    }
                }
            }
            return lookup;
        } catch (Exception e) {
            return Map.of();
        }
    }

    public record BeheerderToewijzingRequest(Boolean isBeheerder) {
    }

    @PatchMapping("/gebruikers/{id}/beheerder")
    public ResponseEntity<GebruikerDTO> setBeheerder(@PathVariable Long id,
            @RequestBody BeheerderToewijzingRequest request) {
        if (request == null || request.isBeheerder() == null) {
            throw new OngeldigeRolException("Veld isBeheerder is verplicht");
        }

        User user = userRepository.findById(id)
                .orElseThrow(() -> new GebruikerNotFoundException("Gebruiker met id " + id + " niet gevonden"));

        if (user.getRol() != Rol.LEERKRACHT) {
            throw new OngeldigeRolException("Enkel een LEERKRACHT kan beheerder worden");
        }

        user.setBeheerder(request.isBeheerder());
        return ResponseEntity.ok(GebruikerMapper.toDTO(userRepository.save(user)));
    }
}
