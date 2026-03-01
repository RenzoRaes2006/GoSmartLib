package ap.be.backend.review.repository;

import java.util.List;

import org.springframework.data.repository.ListCrudRepository;

import ap.be.backend.review.entity.Review;

public interface ReviewRepository extends ListCrudRepository<Review, Long> {
    List<Review> findByBookId(long bookId);
}