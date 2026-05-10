package ap.be.backend.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import ap.be.backend.dto.SchoolDTO;
import ap.be.backend.entity.School;
import ap.be.backend.mappers.SchoolMapper;
import ap.be.backend.repository.SchoolRepository;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Behandelt API-endpoints voor het beheren van scholen in het systeem.
 *
 * Scholen worden opgeslagen in de eigen database. Leerlingen- en klassendata
 * worden niet opgeslagen, maar live opgehaald uit Smartschool en alleen
 * doorgegeven aan de frontend.
 */
@RestController
@RequestMapping("/api/scholen")
public class SchoolController {

    private final SchoolRepository schoolRepository;

    public SchoolController(SchoolRepository schoolRepository) {
        this.schoolRepository = schoolRepository;
    }

    /**
     * Geeft alle geregistreerde scholen terug.
     *
     * @return lijst van scholen als DTO's
     */
    @GetMapping
    public List<SchoolDTO> getAlleScholen() {
        return schoolRepository.findAll().stream().map(SchoolMapper::toDTO).toList();
    }

    /**
     * Slaat een nieuwe school op in de database.
     *
     * Enkel basisgegevens worden opgeslagen (naam, adres, apiCode). Leerlingen
     * en klassen worden niet opgeslagen.
     *
     * @param nieuweSchool de gegevens van de nieuwe school
     * @return de opgeslagen school als DTO
     */
    @PostMapping
    public ResponseEntity<SchoolDTO> maakSchoolAan(@RequestBody School nieuweSchool) {
        School opgeslagenSchool = schoolRepository.save(nieuweSchool);
        return ResponseEntity.ok(SchoolMapper.toDTO(opgeslagenSchool));
    }

    /**
     * Geeft live leerlingen- en klassendata terug van een school.
     *
     * Deze data wordt nooit opgeslagen in de database. Het is een momentopname
     * van de Smartschool-data op het moment van de aanvraag.
     *
     * LET OP: de huidige implementatie geeft gesimuleerde testdata terug.
     * De echte Smartschool-koppeling moet nog worden geïmplementeerd.
     *
     * @param id het database-ID van de school
     * @return een object met schoolinfo en live Smartschool-data, of 404 als
     *         de school niet bestaat
     */
    @GetMapping("/{id}/live-data")
    public ResponseEntity<?> getLiveSmartschoolData(@PathVariable Long id) {

        Optional<School> schoolOpt = schoolRepository.findById(id);

        if (schoolOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        School school = schoolOpt.get();

        List<Map<String, Object>> liveKlassen = List.of(
                Map.of("id", 101, "naam", "1A"),
                Map.of("id", 102, "naam", "1B"),
                Map.of("id", 103, "naam", "2A")
        );

        List<Map<String, Object>> liveLeerlingen = List.of(
                Map.of("id", 5001, "voorNaam", "Amira", "achternaam", "El Idrissi", "klas", "2A"),
                Map.of("id", 5002, "voorNaam", "Bram", "achternaam", "Peeters", "klas", "1A")
        );

        Map<String, Object> response = Map.of(
                "schoolInfo", Map.of("naam", school.getNaam(), "adres", school.getAdres()),
                "liveSmartschoolData", Map.of(
                        "klassen", liveKlassen,
                        "leerlingen", liveLeerlingen
                )
        );

        return ResponseEntity.ok(response);
    }

    /**
     * Verwijdert een school uit de database.
     *
     * @param id het ID van de te verwijderen school
     * @return 204 No Content bij succes, 404 als de school niet bestaat
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> verwijderSchool(@PathVariable Long id) {
        if (!schoolRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }

        schoolRepository.deleteById(id);

        return ResponseEntity.noContent().build();
    }
}
