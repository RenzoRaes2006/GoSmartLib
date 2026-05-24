package ap.be.backend.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import ap.be.backend.entity.KlasWijziging;

@Repository
public interface KlasWijzigingRepository extends JpaRepository<KlasWijziging, Long> {

    List<KlasWijziging> findByLeerling_IdOrderByGewijzigdOpDesc(Long leerlingId);

    Page<KlasWijziging> findByLeerling_IdOrderByGewijzigdOpDesc(Long leerlingId, Pageable pageable);
}
