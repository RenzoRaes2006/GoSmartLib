package ap.be.backend.repository;

import java.util.List;

import org.springframework.stereotype.Service;

import ap.be.backend.entity.Beoordeling;

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
