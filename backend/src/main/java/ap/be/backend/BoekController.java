package ap.be.backend;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RestController;

import ap.be.backend.entity.Boek;

import org.springframework.web.bind.annotation.GetMapping;

@RestController
@CrossOrigin(origins = "http://frontend.localhost")
public class BoekController {
    @Autowired
    private BoekRepository boekRepository;

    @GetMapping("/api/boeken")
    @CrossOrigin(origins = "*", allowedHeaders = "*")
    public List<Boek> getAlleBoeken() {

        return boekRepository.findAll();
    }
    
}
