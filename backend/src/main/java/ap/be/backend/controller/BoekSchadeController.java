package ap.be.backend.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import ap.be.backend.dto.BoekSchadeDTO;
import ap.be.backend.service.BoekSchadeService;

@RestController
@RequestMapping("/api/schade")
@PreAuthorize("hasRole('BEHEERDER')")
public class BoekSchadeController {

    private final BoekSchadeService boekSchadeService;

    public BoekSchadeController(BoekSchadeService boekSchadeService) {
        this.boekSchadeService = boekSchadeService;
    }

    @GetMapping
    public ResponseEntity<List<BoekSchadeDTO>> getAlleSchade() {
        return ResponseEntity.ok(boekSchadeService.getAlle());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> verwijderSchade(@PathVariable Long id) {
        boolean verwijderd = boekSchadeService.verwijder(id);
        return verwijderd ? ResponseEntity.noContent().build() : ResponseEntity.notFound().build();
    }
}
