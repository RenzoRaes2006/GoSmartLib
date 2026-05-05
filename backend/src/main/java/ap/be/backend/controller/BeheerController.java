package ap.be.backend.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import ap.be.backend.service.GebruikerService;
import ap.be.backend.entity.Uitlening;

@RestController
@RequestMapping("/api/beheer")
public class BeheerController {

    private final GebruikerService gebruikerService;

    public BeheerController(GebruikerService gebruikerService) {
        this.gebruikerService = gebruikerService;
    }

    @GetMapping("/leningen/actief")
    public ResponseEntity<List<Uitlening>> getAlleActieveLeningenSysteem() {
        return ResponseEntity.ok(gebruikerService.getAlleActieveLeningenSysteem());
    }

    @GetMapping("/verlengingen/aanvragen")
    public ResponseEntity<List<Uitlening>> getAlleVerlengingAanvragen() {
        return ResponseEntity.ok(gebruikerService.getAlleVerlengingAanvragen());
    }

    @PutMapping("/{id}/verlenging/goedkeuren")
    public ResponseEntity<Void> keurVerlengingGoed(@PathVariable Long id) {
        gebruikerService.keurVerlengingGoed(id);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/{id}/verlenging/weigeren")
    public ResponseEntity<Void> weigerVerlenging(@PathVariable Long id) {
        gebruikerService.weigerVerlenging(id);
        return ResponseEntity.ok().build();
    }
}