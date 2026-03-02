package ap.be.backend.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RestController;

import ap.be.backend.BoekRepository;
import ap.be.backend.entity.Boek;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@RestController
@CrossOrigin(origins = {"http://frontend.localhost", "http://localhost:4200"})
public class BoekController {
    @Autowired
    private BoekRepository boekRepository;

    @GetMapping("/api/boeken")
    public List<Boek> getAlleBoeken() {
        return boekRepository.findAll();
    }

    @GetMapping("/api/boeken/{id}")
    public Boek getBoekById(@PathVariable("id") Long id) {
        return boekRepository.findById(id).orElse(null);
    }

}
