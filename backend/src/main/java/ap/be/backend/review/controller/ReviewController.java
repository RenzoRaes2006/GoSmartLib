package ap.be.backend.review.controller;

import java.util.List;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import ap.be.backend.review.entity.Beoordeling;
import ap.be.backend.review.service.BeoordelingService;

@RestController
@CrossOrigin(origins = {"http://frontend.localhost", "http://localhost:4200"})
@RequestMapping("/api")
public class ReviewController {
    private final BeoordelingService reviewService;

    public ReviewController(BeoordelingService reviewService) {
        this.reviewService = reviewService;
    }

    @GetMapping("/books/{id}/reviews")
    public List<Beoordeling> getReviewsByBookId(@PathVariable("id") Long id) {
        List<Beoordeling> reviews = reviewService.getReviewByBookId(id);
        return reviews;
    }

    @PostMapping("/reviews")
    public Beoordeling addReview(@RequestBody Beoordeling beoordeling) {
        reviewService.saveReview(beoordeling);
        return beoordeling;
    }
}
