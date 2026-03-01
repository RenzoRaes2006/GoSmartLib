package ap.be.backend.review.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
// import jakarta.persistence.JoinColumn;
// import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "reviews")
public class Review {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    // relaties voor later, we hebben geen user en book entities
    // @ManyToOne
    // @JoinColumn(name = "user_id", nullable = false)
    @Column(name = "user_id", nullable = false)
    long userId;

    // @ManyToOne
    // @JoinColumn(name = "book_id", nullable = false)
    @Column(name = "book_id", nullable = false)
    long bookId;

    @Column(name = "rating", nullable = false)
    int rating;

    @Column(name = "content", length = 1000)
    String content;

    public Review() {
    }

    public Review(long userId, long bookId, int rating, String content) {
        this.userId = userId;
        this.bookId = bookId;
        this.rating = rating;
        this.content = content;
    }

    public Long getId() {
        return id;
    }

    public long getUserId() {
        return userId;
    }

    public long getBookId() {
        return bookId;
    }

    public int getRating() {
        return rating;
    }

    public String getContent() {
        return content;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setUserId(long userId) {
        this.userId = userId;
    }

    public void setBookId(long bookId) {
        this.bookId = bookId;
    }

    public void setRating(int rating) {
        this.rating = rating;
    }

    public void setContent(String content) {
        this.content = content;
    }

    @Override
    public String toString() {
        return "Review{" +
                "id=" + id +
                ", userId=" + userId +
                ", bookId=" + bookId +
                ", rating=" + rating +
                ", content='" + content + '\'' +
                '}';
    }
}