package ap.be.backend.controller;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import ap.be.backend.entity.School;
import ap.be.backend.repository.SchoolRepository;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/scholen")
@CrossOrigin(origins = "*") // allow angular to communicate with this backend
public class SchoolController {

    @Autowired
    private SchoolRepository schoolRepository;

    // 1. fetch all registered schools for the dashboard table
    @GetMapping
    public List<School> getAlleScholen() {
        return schoolRepository.findAll();
    }

    // 2. save a new school (only the basic info, no students)
    @PostMapping
    public ResponseEntity<School> maakSchoolAan(@RequestBody School nieuweSchool) {
        School opgeslagenSchool = schoolRepository.save(nieuweSchool);
        System.out.println("New school saved: " + opgeslagenSchool.getNaam());
        return ResponseEntity.ok(opgeslagenSchool);
    }

    // 3. fetch live data (single source of truth - this is never saved to our database)
    @GetMapping("/{id}/live-data")
    public ResponseEntity<?> getLiveSmartschoolData(@PathVariable Long id) {
        
        Optional<School> schoolOpt = schoolRepository.findById(id);
        
        if (schoolOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        School school = schoolOpt.get();
        String smartschoolSleutel = school.getApiCode();

        System.out.println("Connecting to smartschool api for " + school.getNaam() + " with key: " + smartschoolSleutel);
        
        // simulation of the smartschool data that gets generated on-the-fly
        List<Map<String, Object>> liveKlassen = List.of(
                Map.of("id", 101, "naam", "1A"),
                Map.of("id", 102, "naam", "1B"),
                Map.of("id", 103, "naam", "2A")
        );

        List<Map<String, Object>> liveLeerlingen = List.of(
                Map.of("id", 5001, "voorNaam", "Amira", "achternaam", "El Idrissi", "klas", "2A"),
                Map.of("id", 5002, "voorNaam", "Bram", "achternaam", "Peeters", "klas", "1A")
        );

        // build the response for the frontend
        Map<String, Object> response = Map.of(
                "schoolInfo", Map.of("naam", school.getNaam(), "adres", school.getAdres()),
                "liveSmartschoolData", Map.of(
                        "klassen", liveKlassen,
                        "leerlingen", liveLeerlingen
                )
        );

        return ResponseEntity.ok(response);
    }

    // 4. delete a school from the database
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> verwijderSchool(@PathVariable Long id) {
        if (!schoolRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        
        System.out.println("Deleting school with ID: " + id);
        schoolRepository.deleteById(id);
        
        // return 204 no content (successful deletion)
        return ResponseEntity.noContent().build();
    }
}
