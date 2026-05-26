package ap.be.backend.controller;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import ap.be.backend.dto.AuditLogDTO;
import ap.be.backend.entity.AuditActie;
import ap.be.backend.entity.AuditLog;
import ap.be.backend.entity.School;
import ap.be.backend.entity.User;
import ap.be.backend.repository.AuditLogRepository;
import ap.be.backend.repository.SchoolRepository;
import ap.be.backend.repository.UserRepository;
import ap.be.backend.service.OneRosterClient;

@RestController
@RequestMapping("/api/admin/audit-log")
@PreAuthorize("hasRole('ADMIN')")
public class AuditLogController {

    private final AuditLogRepository repository;
    private final UserRepository userRepository;
    private final SchoolRepository schoolRepository;
    private final OneRosterClient oneRosterClient;

    public AuditLogController(AuditLogRepository repository,
                              UserRepository userRepository,
                              SchoolRepository schoolRepository,
                              OneRosterClient oneRosterClient) {
        this.repository = repository;
        this.userRepository = userRepository;
        this.schoolRepository = schoolRepository;
        this.oneRosterClient = oneRosterClient;
    }

    @GetMapping
    public ResponseEntity<Page<AuditLogDTO>> zoek(
            @RequestParam(required = false) AuditActie actie,
            @RequestParam(required = false) String entiteitType,
            @RequestParam(required = false) Long actorId,
            @RequestParam(required = false) LocalDateTime vanaf,
            @RequestParam(required = false) LocalDateTime tot,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "25") int size) {

        PageRequest pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "tijdstip"));
        Page<AuditLog> rijen = repository.zoek(actie, entiteitType, actorId, vanaf, tot, pageable);

        Map<Long, String> userLabels = bouwUserLabels(rijen);
        Map<Long, String> schoolLabels = bouwSchoolLabels(rijen);

        Page<AuditLogDTO> result = rijen.map(r -> AuditLogDTO.from(r,
                bepaalLabel(r, userLabels, schoolLabels)));
        return ResponseEntity.ok(result);
    }

    /**
     * Bouwt voor alle Gebruiker-rijen op de pagina een label: voornaam achternaam
     * via OneRoster (live, niet opgeslagen — zie data-constraint), of smartschoolId
     * als fallback. Per school één OneRoster-call, niet per leerling.
     */
    private Map<Long, String> bouwUserLabels(Page<AuditLog> rijen) {
        Set<Long> userIds = new HashSet<>();
        for (AuditLog r : rijen.getContent()) {
            if ("Gebruiker".equals(r.getEntiteitType()) && r.getEntiteitId() != null) {
                userIds.add(r.getEntiteitId());
            }
        }
        if (userIds.isEmpty()) return Map.of();

        List<User> users = new ArrayList<>();
        userRepository.findAllById(userIds).forEach(users::add);

        // Groepeer per school zodat we OneRoster maar één keer per school aanspreken.
        Map<Long, List<User>> perSchool = new HashMap<>();
        Map<Long, User> users_zonderSchool = new HashMap<>();
        for (User u : users) {
            if (u.getSchool() != null) {
                perSchool.computeIfAbsent(u.getSchool().getId(), k -> new ArrayList<>()).add(u);
            } else {
                users_zonderSchool.put(u.getId(), u);
            }
        }

        Map<Long, String> result = new HashMap<>();
        // Fallback: smartschoolId voor users zonder school
        users_zonderSchool.forEach((id, u) -> result.put(id, u.getSmartschoolId()));

        for (Map.Entry<Long, List<User>> entry : perSchool.entrySet()) {
            School school = schoolRepository.findById(entry.getKey()).orElse(null);
            Set<String> ids = new HashSet<>();
            for (User u : entry.getValue()) {
                ids.add(u.getSmartschoolId());
            }
            Map<String, String> namen = haalNamenOp(school, ids);
            for (User u : entry.getValue()) {
                String naam = namen.get(u.getSmartschoolId());
                result.put(u.getId(), naam != null ? naam : u.getSmartschoolId());
            }
        }
        return result;
    }

    private Map<Long, String> bouwSchoolLabels(Page<AuditLog> rijen) {
        Set<Long> ids = new HashSet<>();
        for (AuditLog r : rijen.getContent()) {
            if ("School".equals(r.getEntiteitType()) && r.getEntiteitId() != null) {
                ids.add(r.getEntiteitId());
            }
        }
        if (ids.isEmpty()) return Map.of();
        Map<Long, String> result = new HashMap<>();
        schoolRepository.findAllById(ids).forEach(s -> result.put(s.getId(), s.getNaam()));
        return result;
    }

    @SuppressWarnings("unchecked")
    private Map<String, String> haalNamenOp(School school, Set<String> gewensteIds) {
        if (school == null
                || gewensteIds.isEmpty()
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
            Map<String, String> lookup = new HashMap<>();
            for (String sleutel : new String[]{"leerkrachten", "leerlingen"}) {
                List<Map<String, Object>> lijst = (List<Map<String, Object>>) data.getOrDefault(sleutel, List.of());
                for (Map<String, Object> u : lijst) {
                    String id = String.valueOf(u.getOrDefault("id", ""));
                    if (id.isBlank() || !gewensteIds.contains(id)) continue;
                    String voornaam = String.valueOf(u.getOrDefault("voorNaam", ""));
                    String achternaam = String.valueOf(u.getOrDefault("achternaam", ""));
                    String volledig = (voornaam + " " + achternaam).trim();
                    if (!volledig.isBlank()) {
                        lookup.put(id, volledig);
                    }
                }
            }
            return lookup;
        } catch (Exception e) {
            return Map.of();
        }
    }

    private String bepaalLabel(AuditLog r, Map<Long, String> users, Map<Long, String> schools) {
        if (r.getEntiteitId() == null) return r.getEntiteitType();
        if ("Gebruiker".equals(r.getEntiteitType())) {
            String label = users.get(r.getEntiteitId());
            return label != null ? label : "Gebruiker #" + r.getEntiteitId();
        }
        if ("School".equals(r.getEntiteitType())) {
            String label = schools.get(r.getEntiteitId());
            return label != null ? label : "School #" + r.getEntiteitId();
        }
        return r.getEntiteitType() + " #" + r.getEntiteitId();
    }
}
