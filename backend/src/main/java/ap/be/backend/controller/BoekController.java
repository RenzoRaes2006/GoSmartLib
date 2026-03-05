package ap.be.backend.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import ap.be.backend.dto.BoekDTO;
import ap.be.backend.entity.Boek;
import ap.be.backend.repository.BoekRepository;
import ap.be.backend.service.BoekService;
import jakarta.validation.Valid;

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
    public List<Boek> getAlleBoeken() {

        return boekRepository.findAll();
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
}