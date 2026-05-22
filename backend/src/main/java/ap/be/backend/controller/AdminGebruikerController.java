package ap.be.backend.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import ap.be.backend.dto.GebruikerCreateRequest;
import ap.be.backend.dto.GebruikerDTO;
import ap.be.backend.entity.Rol;
import ap.be.backend.entity.School;
import ap.be.backend.entity.User;
import ap.be.backend.exception.GebruikerNotFoundException;
import ap.be.backend.exception.OngeldigeRolException;
import ap.be.backend.mappers.GebruikerMapper;
import ap.be.backend.repository.SchoolRepository;
import ap.be.backend.repository.UserRepository;
import ap.be.backend.service.GebruikerLifecycleService;
import ap.be.backend.service.OneRosterClient;

@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminGebruikerController {

    private final UserRepository userRepository;
    private final SchoolRepository schoolRepository;
    private final OneRosterClient oneRosterClient;
    private final GebruikerLifecycleService lifecycleService;

    public AdminGebruikerController(UserRepository userRepository,
                                    SchoolRepository schoolRepository,
                                    OneRosterClient oneRosterClient,
                                    GebruikerLifecycleService lifecycleService) {
        this.userRepository = userRepository;
        this.schoolRepository = schoolRepository;
        this.oneRosterClient = oneRosterClient;
        this.lifecycleService = lifecycleService;
    }

    /**
     * Lijst leerkrachten op met server-side paging. Standaard alleen actieve —
     * gebruik {@code inclVertrokken=true} om vertrokken leerkrachten ook te tonen
     * (bedoeld voor de lifecycle-UI). SchoolId optioneel: zonder schoolId worden
     * leerkrachten van alle scholen teruggegeven (zonder naam-lookup).
     */
    @GetMapping("/leerkrachten")
    public ResponseEntity<Page<GebruikerDTO>> getAlleLeerkrachten(
            @RequestParam(required = false) Long schoolId,
            @RequestParam(defaultValue = "false") boolean inclVertrokken,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size) {

        PageRequest pageable = PageRequest.of(page, size);
        Page<User> users;
        School school = null;
        if (schoolId != null) {
            Optional<School> schoolOpt = schoolRepository.findById(schoolId);
            if (schoolOpt.isEmpty()) return ResponseEntity.notFound().build();
            school = schoolOpt.get();
            users = userRepository.findLeerkrachtenVanSchool(schoolId, !inclVertrokken, pageable);
        } else {
            // Geen schoolId: lever alle leerkrachten paged terug. inclVertrokken-filter wordt
            // hier post-hoc toegepast op de paginarij — voldoende voor de niet-school-specifieke call
            // die geen lifecycle-UI bedient.
            users = userRepository.findByRol(Rol.LEERKRACHT, pageable);
            if (!inclVertrokken) {
                List<User> actieveOnly = users.stream().filter(User::isActief).toList();
                users = new org.springframework.data.domain.PageImpl<>(actieveOnly, pageable, users.getTotalElements());
            }
        }

        Set<String> idsOpPagina = users.getContent().stream()
                .map(User::getSmartschoolId)
                .collect(Collectors.toSet());
        Map<String, String[]> naamPerSmartschoolId = haalNamenOpVoorSchool(school, idsOpPagina);

        Page<GebruikerDTO> result = users.map(u -> {
            String[] paar = naamPerSmartschoolId.getOrDefault(u.getSmartschoolId(), new String[]{null, null});
            return GebruikerMapper.toDTO(u, paar[0], paar[1]);
        });
        return ResponseEntity.ok(result);
    }

    /**
     * Voeg handmatig een leerkracht toe (bv. nieuwe aanwerving die nog niet in
     * OneRoster zit). De smartschoolId moet uniek zijn.
     */
    @PostMapping("/leerkrachten")
    public ResponseEntity<GebruikerDTO> voegLeerkrachtToe(@RequestBody GebruikerCreateRequest request) {
        User aangemaakt = lifecycleService.voegHandmatigToe(
                request.smartschoolId(), request.schoolId(), request.klas(), Rol.LEERKRACHT);
        return ResponseEntity.status(HttpStatus.CREATED).body(GebruikerMapper.toDTO(aangemaakt));
    }

    /**
     * Markeer een leerkracht als vertrokken. Geblokkeerd als er nog open
     * uitleningen op zijn/haar naam staan (typisch klassikale bulk-uitleningen).
     */
    @PatchMapping("/leerkrachten/{id}/vertrek")
    public ResponseEntity<GebruikerDTO> markeerLeerkrachtVertrokken(@PathVariable Long id) {
        User user = lifecycleService.markeerVertrokken(id);
        return ResponseEntity.ok(GebruikerMapper.toDTO(user));
    }

    /**
     * Bouwt enkel een lookup voor de meegegeven smartschoolIds. Verwijdert de
     * verspilling van het hele school-resultaat in een Map zetten terwijl er
     * maar enkele rijen nodig zijn (pagineerlading).
     */
    @SuppressWarnings("unchecked")
    private Map<String, String[]> haalNamenOpVoorSchool(School school, Set<String> gewensteIds) {
        if (school == null
                || gewensteIds == null
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
