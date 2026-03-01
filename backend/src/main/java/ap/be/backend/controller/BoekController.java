package ap.be.backend.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;

import ap.be.backend.dto.BoekDTO;
import ap.be.backend.entity.Boek;
import ap.be.backend.service.BoekService;
import jakarta.validation.Valid;

import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@Controller
public class BoekController {
    private final BoekService boekService;

    public BoekController(BoekService boekService) {
        this.boekService = boekService;
    }

    @PostMapping("/boeken")
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
