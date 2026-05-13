package ap.be.backend.controller;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import ap.be.backend.dto.BoekDTO;
import ap.be.backend.dto.BoekenFilters;
import ap.be.backend.dto.BoekenSort;
import ap.be.backend.dto.PageRequestParams;
import ap.be.backend.entity.Boek;
import ap.be.backend.mappers.BoekMapper;
import ap.be.backend.service.BoekImportService;
import ap.be.backend.service.BoekService;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PutMapping;

@RestController
public class BoekController {
    private final BoekService boekService;
    private final BoekImportService boekImportService;

    public BoekController(BoekService boekService, BoekImportService boekImportService) {
        this.boekService = boekService;
        this.boekImportService = boekImportService;
    }

    @GetMapping("/api/boeken")
    public Page<BoekDTO> getAlleBoeken(
            @ModelAttribute BoekenFilters filters,
            @ModelAttribute BoekenSort sort,
            @ModelAttribute PageRequestParams paging) {
        return boekService.filterBoeken(filters, sort, paging);
    }

    @PreAuthorize("hasRole('BEHEERDER')")
    @PostMapping("/api/boeken")
    public ResponseEntity<BoekDTO> postBoek(@Valid @RequestBody BoekDTO boekDTO) {
        Boek nieuwBoek = boekService.addBoek(boekDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(BoekMapper.toDTO(nieuwBoek));
    }

    @GetMapping("/api/boeken/{id}")
    public ResponseEntity<BoekDTO> getBoekById(@PathVariable("id") Long id) {
        Boek boek = boekService.getBoekById(id);
        if (boek == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(BoekMapper.toDTO(boek));
    }

    @GetMapping("/api/boeken/isbn/{isbn}")
    public ResponseEntity<BoekDTO> getBoekByIsbn(@PathVariable("isbn") String isbn) {
        Boek boek = boekService.getBoekByIsbn(isbn);
        if (boek == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(BoekMapper.toDTO(boek));
    }

    @PreAuthorize("hasRole('BEHEERDER')")
    @PutMapping("/api/boeken/{id}")
    public ResponseEntity<BoekDTO> updateBoek(@PathVariable Long id, @Valid @RequestBody BoekDTO boekDTO) {
        Boek geupdate = boekService.updateBoek(id, boekDTO);
        if (geupdate == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(BoekMapper.toDTO(geupdate));
    }

    @PreAuthorize("hasRole('BEHEERDER')")
    @DeleteMapping("/api/boeken/{id}")
    public ResponseEntity<String> deleteBoek(@PathVariable Long id) {
        boolean deleted = boekService.deleteBoek(id);
        if (!deleted) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok("Boek is verwijderd.");
    }

    @GetMapping("/api/boeken/paged")
    public Page<BoekDTO> getBoekenPaged(@RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size) {
        return boekService.getBoekenPaged(page, size).map(BoekMapper::toDTO);
    }

    @GetMapping("/api/boeken/top/populair")
    public ResponseEntity<List<BoekDTO>> getPopulaireBoeken() {
        List<BoekDTO> populaireBoeken = boekService.getPopulaireBoeken().stream()
                .map(BoekMapper::toDTO)
                .toList();
        return ResponseEntity.ok(populaireBoeken);
    }

    @PreAuthorize("hasRole('BEHEERDER')")
    @PostMapping("/api/boeken/import/preview")
    public ResponseEntity<?> previewImporteerBoeken(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body("Het geüploade bestand is leeg.");
        }

        try {
            List<Boek> previewLijst = boekImportService.parseerVoorPreview(file);
            return ResponseEntity.ok(previewLijst);
        } catch (ap.be.backend.exception.InvalidImportDataException e) {

            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Er is een onverwachte systeemfout opgetreden.");
        }
    }

    @PreAuthorize("hasRole('BEHEERDER')")
    @PostMapping("/api/boeken/import/bevestig")
    public ResponseEntity<String> bevestigImport(@RequestBody List<Boek> boekenLijst) {
        if (boekenLijst == null || boekenLijst.isEmpty()) {
            return ResponseEntity.badRequest().body("Lijst om te importeren is leeg.");
        }
        int totaalGeimporteerd = boekImportService.bevestigImport(boekenLijst);
        return ResponseEntity.ok("Succesvol " + totaalGeimporteerd + " boeken geïmporteerd!");
    }

}