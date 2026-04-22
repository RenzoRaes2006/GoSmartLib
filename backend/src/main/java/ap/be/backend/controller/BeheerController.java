package ap.be.backend.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import ap.be.backend.service.GebruikerService;
import ap.be.backend.entity.Uitlening;

@RestController
@RequestMapping("/api/beheer") // adjust this base path if your frontend expects something else
public class BeheerController {

    private final GebruikerService gebruikerService;

    public BeheerController(GebruikerService gebruikerService) {
        this.gebruikerService = gebruikerService;
    }

    @GetMapping("/leningen/actief")
    public ResponseEntity<List<Uitlening>> getAlleActieveLeningenSysteem() {
        return ResponseEntity.ok(gebruikerService.getAlleActieveLeningenSysteem());
    }
}