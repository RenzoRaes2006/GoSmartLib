package ap.be.backend.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import ap.be.backend.entity.Lesmateriaal;

public interface LesmateriaalRepository extends JpaRepository<Lesmateriaal, Long> {
    List<Lesmateriaal> findByBoekIdOrderByUploadedAtDesc(Long boekId);
}
