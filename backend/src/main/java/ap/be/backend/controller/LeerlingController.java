package ap.be.backend.controller;

import ap.be.backend.entity.Gebruiker;
import ap.be.backend.repository.GebruikerRepository;
import ap.be.backend.repository.UitleningRepository;

import java.util.Map;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/leerlingen")
public class LeerlingController {

    private final GebruikerRepository repo;
    private final UitleningRepository uitleningRepo;

    public LeerlingController(GebruikerRepository repo, UitleningRepository uitlening) {
        this.repo = repo;
        this.uitleningRepo = uitlening;
    }

    @GetMapping
    public Page<Gebruiker> getLeerlingen(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size,
            @RequestParam(required = false) String klas,
            @RequestParam(required = false) String zoekterm) {
        return repo.findLeerlingenMetFilters(klas, zoekterm, PageRequest.of(page, size));
    }

    @GetMapping("/{id}/stats")
public Map<String, Object> getLeerlingStats(@PathVariable Long id) {
    long totaal = uitleningRepo.countByGebruiker_GebruikerID(id);
    long opTijd = uitleningRepo.countOnTime(id);
    
    return Map.of(
        "totaalGeleend", totaal,
        "opTijdTerug", opTijd,
        "teLaat", totaal - opTijd
    );
}

}
