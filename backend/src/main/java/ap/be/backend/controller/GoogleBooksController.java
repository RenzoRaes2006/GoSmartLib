package ap.be.backend.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import ap.be.backend.dto.GoogleBoekDTO;
import ap.be.backend.service.GoogleBooksService;

@RestController
@RequestMapping("/api/googlebooks")
public class GoogleBooksController {

    private final GoogleBooksService googleBooksService;

    public GoogleBooksController(GoogleBooksService googleBooksService) {
        this.googleBooksService = googleBooksService;
    }

    @GetMapping("/isbn/{isbn}")
    public ResponseEntity<GoogleBoekDTO> zoekOpIsbn(@PathVariable String isbn) {
        GoogleBoekDTO boek = googleBooksService.zoekOpIsbn(isbn);
        if (boek == null) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(boek);
    }
}
