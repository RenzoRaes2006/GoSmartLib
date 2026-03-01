package ap.be.backend;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import ap.be.backend.entity.TestEntity;

@RestController
@CrossOrigin(origins = "http://frontend.localhost")
public class TestController {

    @Autowired
    private TestRepository repository;

    @GetMapping("/")
    public String home() {
        return "Hello World! De backend werkt!";
    }

    @PostMapping("/save")
    public TestEntity opslaan(@RequestBody TestEntity data) {
        return repository.save(data);
    }
}