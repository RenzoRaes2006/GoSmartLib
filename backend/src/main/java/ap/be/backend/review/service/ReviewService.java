package ap.be.backend.review.service;

import java.util.List;

import org.springframework.stereotype.Service;

import ap.be.backend.review.entity.Review;
import ap.be.backend.review.repository.ReviewRepository;

@Service
public class ReviewService {
    private final ReviewRepository repo;

    public ReviewService(ReviewRepository reviewRepository) {
        this.repo = reviewRepository;
    }

    public void saveReview(Review review) {
        repo.save(review);
    }

    public void deleteReview(Review review) {
        repo.delete(review);
    }

    public List<Review> getReviewByBookId(Long id) {
        return repo.findByBookId(id);
    }
}
