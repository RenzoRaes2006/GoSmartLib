package ap.be.backend.controller;

import java.util.List;

import org.springframework.data.domain.Page;
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
import org.springframework.web.multipart.MultipartFile;

import ap.be.backend.dto.BoekDTO;
import ap.be.backend.entity.Boek;
import ap.be.backend.service.BoekImportService;
import ap.be.backend.service.BoekService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.data.domain.Sort;

@RestController
// @CrossOrigin(origins = { "http://frontend.localhost", "http://localhost:4200"
// })
public class BoekController {
    private final BoekService boekService;
    private final BoekImportService boekImportService;

    public BoekController(BoekService boekService, BoekImportService boekImportService) {
        this.boekService = boekService;
        this.boekImportService = boekImportService;
    }

    @GetMapping("/api/boeken")
    @CrossOrigin(origins = "*", allowedHeaders = "*")
    public List<Boek> getAlleBoeken(
            @RequestParam(required = false) String zoekterm,
            @RequestParam(required = false) List<String> genre,
            @RequestParam(required = false) String uitgever,
            @RequestParam(required = false) Integer minPagina,
            @RequestParam(required = false) Integer maxPagina,
            @RequestParam(required = false) String sorteerOp,
            @RequestParam(required = false) String sorteerRichting,
            @RequestParam(required = false) String beschikbaar,
            @RequestParam(required = false) Integer uitgaveJaarVan,
            @RequestParam(required = false) Integer uitgaveJaarTot,
            @RequestParam(required = false) String leesniveau) {

        Sort sort;
        if (sorteerOp != null && sorteerRichting != null) {
            sort = Sort.by(Sort.Direction.fromString(sorteerRichting), sorteerOp);
        } else {
            sort = Sort.by(Sort.Direction.ASC, "id");
        }

        return boekService.filterBoeken(zoekterm, genre, uitgever, minPagina, maxPagina,
                beschikbaar != null ? Boolean.parseBoolean(beschikbaar) : null, uitgaveJaarVan, uitgaveJaarTot,
                leesniveau, sort);
    }

    @PostMapping("/api/boeken")
    public ResponseEntity<Boek> postBoek(@Valid @RequestBody BoekDTO boekDTO) {
        Boek nieuwBoek = boekService.addBoek(boekDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(nieuwBoek);
    }

    @GetMapping("/api/boeken/{id}")
    public Boek getBoekById(@PathVariable("id") Long id) {
        return boekService.getBoekById(id);
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
        return boekService.getBoekenPaged(page, size);
    }

    @GetMapping("/api/boeken/top/populair")
    public ResponseEntity<List<Boek>> getPopulaireBoeken() {
        List<Boek> populaireBoeken = boekService.getPopulaireBoeken();
        return ResponseEntity.ok(populaireBoeken);
    }

    @PostMapping("/api/boeken/import")
    @CrossOrigin(origins = "*", allowedHeaders = "*")
    public ResponseEntity<?> importeerBoeken(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("Het geüploade bestand is leeg.");
        }

        int aantalGeimporteerd = boekImportService.importeerBestand(file);

        return ResponseEntity.ok("Aantal geïmporteerde boeken: " + aantalGeimporteerd);
    }

}