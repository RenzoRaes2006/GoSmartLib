package ap.be.backend.controller;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import ap.be.backend.dto.BoekDTO;
import ap.be.backend.entity.Boek;
import ap.be.backend.repository.BoekRepository;
import ap.be.backend.service.BoekService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PutMapping;

@RestController
@CrossOrigin(origins = { "http://frontend.localhost", "http://localhost:4200" })
public class BoekController {
    private final BoekService boekService;
    private final BoekRepository boekRepository;

    public BoekController(BoekService boekService, BoekRepository boekRepository) {
        this.boekService = boekService;
        this.boekRepository = boekRepository;
    }

    @GetMapping("/api/boeken")
    @CrossOrigin(origins = "*", allowedHeaders = "*")
    public List<Boek> getAlleBoeken(@RequestParam(required = false) String zoekterm) {
        Sort sort = Sort.by(Sort.Direction.ASC, "locatie");

        if (zoekterm != null && !zoekterm.isEmpty()) {
            return boekRepository.searchBoeken(zoekterm, sort);
        }

        return boekRepository.findAll(sort);
    }

    @PostMapping("/api/boeken")
    public ResponseEntity<Boek> postBoek(@Valid @RequestBody BoekDTO boekDTO) {
        Boek nieuwBoek = boekService.addBoek(boekDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(nieuwBoek);
    }

    @GetMapping("/api/boeken/{id}")
    public Boek getBoekById(@PathVariable("id") Long id) {
        return boekRepository.findById(id).orElse(null);

    }

    @GetMapping("/api/boeken/isbn/{isbn}")
    public ResponseEntity<Boek> getBoekByIsbn(@PathVariable("isbn") String isbn) {
        Boek boek = boekService.getBoekByIsbn(isbn);
        if (boek == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(boek);
    }

    @PutMapping("/api/boeken/{id}")
    public ResponseEntity<Boek> updateBoek(@PathVariable Long id, @Valid @RequestBody BoekDTO boekDTO) {
        Boek geupdate = boekService.updateBoek(id, boekDTO);
        if (geupdate == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(geupdate);
    }

    @DeleteMapping("/api/boeken/{id}")
    public ResponseEntity<String> deleteBoek(@PathVariable Long id) {
        boolean deleted = boekService.deleteBoek(id);
        if (!deleted) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok("Boek is verwijderd.");
    }

    @GetMapping("/api/boeken/paged")
    public Page<Boek> getBoekenPaged(@RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size) {
        return boekRepository.findAll(PageRequest.of(page, size, Sort.by("titel").ascending()));
    }

    @GetMapping("/api/boeken/top/populair")
    public ResponseEntity<List<Boek>> getPopulaireBoeken() {
        List<Boek> populaireBoeken = boekService.getPopulaireBoeken();
        return ResponseEntity.ok(populaireBoeken);
    }

}