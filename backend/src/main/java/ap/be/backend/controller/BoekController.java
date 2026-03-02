package ap.be.backend.controller;

import java.util.List;
import java.util.ArrayList;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;

import ap.be.backend.dto.BoekDTO;
import ap.be.backend.entity.Boek;
import ap.be.backend.repository.BoekRepository;
import ap.be.backend.service.BoekService;
import jakarta.validation.Valid;

@RestController
@CrossOrigin(origins = "http://frontend.localhost")
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
        try {
            Boek nieuwBoek = boekService.addBoek(boekDTO);
            return ResponseEntity.status(HttpStatus.CREATED).body(nieuwBoek);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<String> handleValidationExceptions(MethodArgumentNotValidException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Ongeldige invoer: " + ex.getMessage());
    }

}
