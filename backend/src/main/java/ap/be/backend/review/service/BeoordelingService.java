package ap.be.backend.review.service;

import java.util.List;

import org.springframework.stereotype.Service;

import ap.be.backend.review.entity.Beoordeling;
import ap.be.backend.review.repository.BeoordelingRepository;

@Service
public class BeoordelingService {
    private final BeoordelingRepository repo;

    public BeoordelingService(BeoordelingRepository reviewRepository) {
        this.repo = reviewRepository;
    }

    public void saveReview(Beoordeling review) {
        repo.save(review);
    }

    public void deleteReview(Beoordeling review) {
        repo.delete(review);
    }

    public List<Beoordeling> getReviewByBookId(Long id) {
        return repo.findByBoekId(id);
    }
}
