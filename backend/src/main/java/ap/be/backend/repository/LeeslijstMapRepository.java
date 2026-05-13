package ap.be.backend.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import ap.be.backend.entity.LeeslijstMap;

@Repository
public interface LeeslijstMapRepository extends JpaRepository<LeeslijstMap, Long> {

    List<LeeslijstMap> findByUser_Id(Long userId);

    Optional<LeeslijstMap> findByUser_IdAndNaam(Long userId, String naam);
}
