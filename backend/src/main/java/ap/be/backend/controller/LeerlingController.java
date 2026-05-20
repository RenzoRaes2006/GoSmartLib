package ap.be.backend.controller;

import ap.be.backend.config.SecurityUtils;
import ap.be.backend.dto.GebruikerDTO;
import ap.be.backend.entity.School;
import ap.be.backend.entity.User;
import ap.be.backend.mappers.GebruikerMapper;
import ap.be.backend.repository.UitleningRepository;
import ap.be.backend.repository.UserRepository;
import ap.be.backend.service.OneRosterClient;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/leerlingen")
public class LeerlingController {

    private final UserRepository repo;
    private final UitleningRepository uitleningRepo;
    private final OneRosterClient oneRosterClient;

    public LeerlingController(UserRepository repo,
                              UitleningRepository uitlening,
                              OneRosterClient oneRosterClient) {
        this.repo = repo;
        this.uitleningRepo = uitlening;
        this.oneRosterClient = oneRosterClient;
    }

    @GetMapping
    public Page<GebruikerDTO> getLeerlingen(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size,
            @RequestParam(required = false) String klas,
            @RequestParam(required = false) String zoekterm) {
        Long schoolId = huidigeSchoolId();
        Page<User> users = repo.findLeerlingenMetFilters(schoolId, klas, zoekterm, PageRequest.of(page, size));
        Map<String, String[]> naamLookup = bouwNaamLookup(users.getContent());
        return users.map(u -> {
            String[] paar = naamLookup.getOrDefault(u.getSmartschoolId(), new String[]{null, null});
            return GebruikerMapper.toDTO(u, paar[0], paar[1]);
        });
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
            return List.of();
        }
        try {
            Map<String, Object> data = oneRosterClient.haalLiveSchoolDataOp(
                    school.getSmartschoolDomein(),
                    school.getOauthClientId(),
                    school.getOauthClientSecret());
            List<Map<String, Object>> klassen = (List<Map<String, Object>>) data.getOrDefault("klassen", List.of());
            return klassen.stream()
                    .map(k -> String.valueOf(k.getOrDefault("naam", "")))
                    .filter(n -> !n.isBlank())
                    .distinct()
                    .sorted()
                    .toList();
        } catch (Exception e) {
            return List.of();
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
     * Bouwt een smartschoolId -> volledige-naam map door per unieke school
     * van de gevraagde users de live OneRoster-data op te halen. Namen worden
     * niet in de databank bewaard.
     */
    private Map<String, String[]> bouwNaamLookup(List<User> users) {
        Map<String, String[]> result = new HashMap<>();
        if (users == null || users.isEmpty()) return result;

        Map<Long, School> reedsGehaald = new HashMap<>();
        for (User u : users) {
            School school = u.getSchool();
            if (school == null) continue;
            if (reedsGehaald.containsKey(school.getId())) continue;
            reedsGehaald.put(school.getId(), school);
            vulNamenIn(school, result);
        }
        return result;
    }

    @SuppressWarnings("unchecked")
    private void vulNamenIn(School school, Map<String, String[]> doel) {
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
                    String voornaam = String.valueOf(u.getOrDefault("voorNaam", ""));
                    String achternaam = String.valueOf(u.getOrDefault("achternaam", ""));
                    if (!id.isBlank() && (!voornaam.isBlank() || !achternaam.isBlank())) {
                        doel.putIfAbsent(id, new String[]{voornaam, achternaam});
                    }
                }
            }
        } catch (Exception ignored) {
            // OneRoster onbereikbaar — geef gewoon geen namen mee
        }
    }
}
