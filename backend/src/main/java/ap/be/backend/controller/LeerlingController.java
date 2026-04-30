package ap.be.backend.controller;

import ap.be.backend.entity.Gebruiker;
import ap.be.backend.repository.GebruikerRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/leerlingen")
@CrossOrigin(origins = "*") // replace with your frontend url in production
public class LeerlingController {

    private final GebruikerRepository repo;

    public LeerlingController(GebruikerRepository repo) {
        this.repo = repo;
    }

    @GetMapping
    public Page<Gebruiker> getLeerlingen(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size,
            @RequestParam(required = false) String klas,
            @RequestParam(required = false) String zoekterm) {
        return repo.findLeerlingenMetFilters(klas, zoekterm, PageRequest.of(page, size));
    }

}
