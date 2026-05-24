package ap.be.backend.controller;

import ap.be.backend.config.SecurityUtils;
import ap.be.backend.dto.GebruikerCreateRequest;
import ap.be.backend.dto.GebruikerDTO;
import ap.be.backend.dto.KlasWijzigenRequest;
import ap.be.backend.dto.KlasWijzigingDTO;
import ap.be.backend.entity.Rol;
import ap.be.backend.entity.School;
import ap.be.backend.entity.User;
import ap.be.backend.exception.SchoolNotFoundException;
import ap.be.backend.mappers.GebruikerMapper;
import ap.be.backend.repository.SchoolRepository;
import ap.be.backend.repository.UitleningRepository;
import ap.be.backend.repository.UserRepository;
import ap.be.backend.service.GebruikerLifecycleService;
import ap.be.backend.service.OneRosterClient;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/leerlingen")
public class LeerlingController {

    private final UserRepository repo;
    private final UitleningRepository uitleningRepo;
    private final OneRosterClient oneRosterClient;
    private final GebruikerLifecycleService lifecycleService;
    private final SchoolRepository schoolRepository;

    public LeerlingController(UserRepository repo,
                              UitleningRepository uitlening,
                              OneRosterClient oneRosterClient,
                              GebruikerLifecycleService lifecycleService,
                              SchoolRepository schoolRepository) {
        this.repo = repo;
        this.uitleningRepo = uitlening;
        this.oneRosterClient = oneRosterClient;
        this.lifecycleService = lifecycleService;
        this.schoolRepository = schoolRepository;
    }

    @GetMapping
    public Page<GebruikerDTO> getLeerlingen(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size,
            @RequestParam(required = false) String klas,
            @RequestParam(required = false) String zoekterm) {
        Long schoolId = huidigeSchoolId();
        PageRequest pageable = PageRequest.of(page, size);

        // Geen zoekterm: paginering in de DB volstaat (efficiënt).
        if (zoekterm == null || zoekterm.isBlank()) {
            Page<User> users = repo.findLeerlingenMetFilters(schoolId, klas, pageable);
            Map<String, String[]> naamLookup = bouwNaamLookup(users.getContent());
            return users.map(u -> {
                String[] paar = naamLookup.getOrDefault(u.getSmartschoolId(), new String[]{null, null});
                return GebruikerMapper.toDTO(u, paar[0], paar[1]);
            });
        }

        // Wel zoekterm: namen staan niet in de DB, dus filteren we in-memory op
        // de live opgehaalde voor-/achternaam uit OneRoster.
        List<User> alleLeerlingen = repo.findLeerlingenVoorZoekopdracht(schoolId, klas);
        Map<String, String[]> naamLookup = bouwNaamLookup(alleLeerlingen);
        String term = zoekterm.toLowerCase().trim();

        List<GebruikerDTO> gefilterd = new ArrayList<>();
        for (User u : alleLeerlingen) {
            String[] paar = naamLookup.getOrDefault(u.getSmartschoolId(), new String[]{null, null});
            String voor = paar[0] == null ? "" : paar[0].toLowerCase();
            String achter = paar[1] == null ? "" : paar[1].toLowerCase();
            String volledig = (voor + " " + achter).trim();
            String ssid = u.getSmartschoolId() == null ? "" : u.getSmartschoolId().toLowerCase();
            if (voor.contains(term) || achter.contains(term) || volledig.contains(term) || ssid.contains(term)) {
                gefilterd.add(GebruikerMapper.toDTO(u, paar[0], paar[1]));
            }
        }

        int from = Math.min(page * size, gefilterd.size());
        int to = Math.min(from + size, gefilterd.size());
        return new PageImpl<>(gefilterd.subList(from, to), pageable, gefilterd.size());
    }

    /**
     * Geeft de klassen terug van de school van de ingelogde leerkracht.
     * Wordt live opgehaald uit OneRoster — er wordt geen klassenlijst
     * gepersisteerd. Bij OneRoster-fouten of ontbrekende credentials valt de
     * lijst leeg uit zodat de dropdown niet crasht.
     */
    @GetMapping("/klassen")
    @SuppressWarnings("unchecked")
    public List<String> getKlassen() {
        School school = huidigeSchool();
        if (school.getSmartschoolDomein() == null
                || school.getOauthClientId() == null
                || school.getOauthClientSecret() == null) {
            return repo.findDistinctKlassen();
        }
        try {
            Map<String, Object> data = oneRosterClient.haalLiveSchoolDataOp(
                    school.getSmartschoolDomein(),
                    school.getOauthClientId(),
                    school.getOauthClientSecret());
            List<Map<String, Object>> klassen = (List<Map<String, Object>>) data.getOrDefault("klassen", List.of());
            List<String> result = klassen.stream()
                    .map(k -> String.valueOf(k.getOrDefault("naam", "")))
                    .filter(n -> !n.isBlank())
                    .distinct()
                    .sorted()
                    .toList();
            return result.isEmpty() ? repo.findDistinctKlassen() : result;
        } catch (Exception e) {
            return repo.findDistinctKlassen();
        }
    }

    private School huidigeSchool() {
        User huidige = SecurityUtils.getCurrentUser(repo);
        if (huidige.getSchool() == null) {
            throw new IllegalStateException("Ingelogde gebruiker heeft geen school toegewezen.");
        }
        return huidige.getSchool();
    }

    private Long huidigeSchoolId() {
        return huidigeSchool().getId();
    }

    /**
     * Admin-only: klassen van een specifieke school — bedoeld om de klas-suggesties
     * te vullen in het admin "nieuwe gebruiker"-form. Probeert eerst OneRoster
     * (volledige officiële klassenlijst), valt terug op DB als OneRoster onbereikbaar
     * is of de school geen credentials heeft.
     */
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/admin/klassen")
    @SuppressWarnings("unchecked")
    public List<String> getKlassenVoorSchool(@RequestParam Long schoolId) {
        School school = schoolRepository.findById(schoolId)
                .orElseThrow(() -> new SchoolNotFoundException(schoolId));
        if (school.getSmartschoolDomein() == null
                || school.getOauthClientId() == null
                || school.getOauthClientSecret() == null) {
            return repo.findDistinctKlassenBySchoolId(schoolId);
        }
        try {
            Map<String, Object> data = oneRosterClient.haalLiveSchoolDataOp(
                    school.getSmartschoolDomein(),
                    school.getOauthClientId(),
                    school.getOauthClientSecret());
            List<Map<String, Object>> klassen = (List<Map<String, Object>>) data.getOrDefault("klassen", List.of());
            List<String> result = klassen.stream()
                    .map(k -> String.valueOf(k.getOrDefault("naam", "")))
                    .filter(n -> !n.isBlank())
                    .distinct()
                    .sorted()
                    .toList();
            return result.isEmpty() ? repo.findDistinctKlassenBySchoolId(schoolId) : result;
        } catch (Exception e) {
            return repo.findDistinctKlassenBySchoolId(schoolId);
        }
    }

    /**
     * Admin-only: lijst alle leerlingen van een school inclusief vertrokkenen,
     * gesorteerd op actief-status. Server-side paging. Bedoeld voor de admin lifecycle-UI.
     */
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/admin")
    public Page<GebruikerDTO> getAlleLeerlingenInclVertrokken(
            @RequestParam Long schoolId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size) {
        Page<User> users = repo.findAlleLeerlingenInclVertrokken(schoolId, PageRequest.of(page, size));
        Map<String, String[]> naamLookup = bouwNaamLookup(users.getContent());
        return users.map(u -> {
            String[] paar = naamLookup.getOrDefault(u.getSmartschoolId(), new String[]{null, null});
            return GebruikerMapper.toDTO(u, paar[0], paar[1]);
        });
    }

    /**
     * Admin-only: voeg handmatig een leerling toe (bv. een nieuwe inschrijving
     * die nog niet in OneRoster staat). De smartschoolId moet uniek zijn.
     */
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public ResponseEntity<GebruikerDTO> voegLeerlingToe(@RequestBody GebruikerCreateRequest request) {
        User aangemaakt = lifecycleService.voegHandmatigToe(
                request.smartschoolId(), request.schoolId(), request.klas(), Rol.LEERLING);
        return ResponseEntity.status(HttpStatus.CREATED).body(GebruikerMapper.toDTO(aangemaakt));
    }

    /**
     * Admin-only: markeer een leerling als vertrokken. Geblokkeerd als er nog
     * open uitleningen op zijn/haar naam staan.
     */
    @PreAuthorize("hasRole('ADMIN')")
    @PatchMapping("/{id}/vertrek")
    public ResponseEntity<GebruikerDTO> markeerLeerlingVertrokken(@PathVariable Long id) {
        User user = lifecycleService.markeerVertrokken(id);
        return ResponseEntity.ok(GebruikerMapper.toDTO(user));
    }

    /**
     * Admin-only: zet een vertrokken leerling terug op actief.
     */
    @PreAuthorize("hasRole('ADMIN')")
    @PatchMapping("/{id}/reactiveer")
    public ResponseEntity<GebruikerDTO> reactiveerLeerling(@PathVariable Long id) {
        User user = lifecycleService.reactiveer(id);
        return ResponseEntity.ok(GebruikerMapper.toDTO(user));
    }

    /**
     * Admin-only: wijzig de klas van een leerling. Schrijft automatisch een
     * historiek-rij. No-op als de nieuwe klas gelijk is aan de huidige.
     */
    @PreAuthorize("hasRole('ADMIN')")
    @PatchMapping("/{id}/klas")
    public ResponseEntity<GebruikerDTO> wijzigKlas(@PathVariable Long id,
                                                   @RequestBody KlasWijzigenRequest request) {
        User user = lifecycleService.wijzigKlas(id, request.nieuweKlas());
        return ResponseEntity.ok(GebruikerMapper.toDTO(user));
    }

    /**
     * Admin-only: geef de klas-historiek van een leerling, paged, nieuwste eerst.
     */
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/{id}/klas-historiek")
    public Page<KlasWijzigingDTO> getKlasHistoriek(
            @PathVariable Long id,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return lifecycleService.getHistoriek(id, PageRequest.of(page, size))
                .map(w -> new KlasWijzigingDTO(
                        w.getId(),
                        w.getVanKlas(),
                        w.getNaarKlas(),
                        w.getGewijzigdOp(),
                        w.getDoorWie() == null ? null : w.getDoorWie().getSmartschoolId(),
                        w.getBron()));
    }

    @GetMapping("/{id}/stats")
    public Map<String, Object> getLeerlingStats(@PathVariable Long id) {
        long totaal = uitleningRepo.countByUser_Id(id);
        long opTijd = uitleningRepo.countOnTime(id);

        return Map.of(
                "totaalGeleend", totaal,
                "opTijdTerug", opTijd,
                "teLaat", totaal - opTijd);
    }

    /**
     * Bouwt een smartschoolId -> volledige-naam map voor enkel de meegegeven users
     * (typisch de huidige pagina). Filtert het OneRoster-resultaat zodat de map
     * niet onnodig opgeblazen wordt met 100en namen die niemand toont.
     */
    private Map<String, String[]> bouwNaamLookup(List<User> users) {
        Map<String, String[]> result = new HashMap<>();
        if (users == null || users.isEmpty()) return result;

        Set<String> gewensteIds = new HashSet<>();
        Map<Long, School> reedsGehaald = new HashMap<>();
        for (User u : users) {
            if (u.getSmartschoolId() != null) gewensteIds.add(u.getSmartschoolId());
            School school = u.getSchool();
            if (school == null) continue;
            reedsGehaald.putIfAbsent(school.getId(), school);
        }
        for (School school : reedsGehaald.values()) {
            vulNamenIn(school, result, gewensteIds);
        }
        return result;
    }

    @SuppressWarnings("unchecked")
    private void vulNamenIn(School school, Map<String, String[]> doel, Set<String> gewensteIds) {
        if (school.getSmartschoolDomein() == null
                || school.getOauthClientId() == null
                || school.getOauthClientSecret() == null) {
            return;
        }
        try {
            Map<String, Object> data = oneRosterClient.haalLiveSchoolDataOp(
                    school.getSmartschoolDomein(),
                    school.getOauthClientId(),
                    school.getOauthClientSecret());
            for (String sleutel : new String[]{"leerlingen", "leerkrachten"}) {
                List<Map<String, Object>> lijst = (List<Map<String, Object>>) data.getOrDefault(sleutel, List.of());
                for (Map<String, Object> u : lijst) {
                    String id = String.valueOf(u.getOrDefault("id", ""));
                    if (id.isBlank() || !gewensteIds.contains(id)) continue;
                    String voornaam = String.valueOf(u.getOrDefault("voorNaam", ""));
                    String achternaam = String.valueOf(u.getOrDefault("achternaam", ""));
                    if (!voornaam.isBlank() || !achternaam.isBlank()) {
                        doel.putIfAbsent(id, new String[]{voornaam, achternaam});
                    }
                }
            }
        } catch (Exception ignored) {
            // OneRoster onbereikbaar — geef gewoon geen namen mee
        }
    }
}
