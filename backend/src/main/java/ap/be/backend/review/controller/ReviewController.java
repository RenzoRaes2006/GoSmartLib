package ap.be.backend.review.controller;

import java.util.List;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import ap.be.backend.review.entity.Review;
import ap.be.backend.review.service.ReviewService;

@RestController
@CrossOrigin(origins = {"http://frontend.localhost", "http://localhost:4200"})
@RequestMapping("/api")
public class ReviewController {
    private final ReviewService reviewService;

    public ReviewController(ReviewService reviewService) {
        this.reviewService = reviewService;
    }

    @GetMapping("/books/{id}/reviews")
    public List<Review> getReviewsByBookId(@PathVariable("id") Long id) {
        List<Review> reviews = reviewService.getReviewByBookId(id);
        return reviews;
    }

    @PostMapping("/reviews")
    public Review addReview(@RequestBody Review review) {
        reviewService.saveReview(review);
        return review;
    }
}
