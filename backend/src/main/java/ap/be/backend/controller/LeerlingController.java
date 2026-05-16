package ap.be.backend.controller;

import ap.be.backend.dto.GebruikerDTO;
import ap.be.backend.mappers.GebruikerMapper;
import ap.be.backend.repository.UitleningRepository;
import ap.be.backend.repository.UserRepository;

import java.util.Map;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/leerlingen")
public class LeerlingController {

    private final UserRepository repo;
    private final UitleningRepository uitleningRepo;

    public LeerlingController(UserRepository repo, UitleningRepository uitlening) {
        this.repo = repo;
        this.uitleningRepo = uitlening;
    }

    @GetMapping
    public Page<GebruikerDTO> getLeerlingen(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size,
            @RequestParam(required = false) String klas,
            @RequestParam(required = false) String zoekterm) {
        return repo.findLeerlingenMetFilters(klas, zoekterm, PageRequest.of(page, size))
                .map(GebruikerMapper::toDTO);
    }

    /**
     * Geeft alle unieke klassen terug waar er gebruikers in zitten. Handig om
     * een dropdown te vullen bij het aanmaken/zoeken van klas-gebonden data.
     */
    @GetMapping("/klassen")
    public java.util.List<String> getKlassen() {
        return repo.findDistinctKlassen();
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
}
