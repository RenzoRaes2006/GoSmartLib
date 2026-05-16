package ap.be.backend.repository;

import ap.be.backend.entity.KlassLeeslijst;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface KlassLeeslijstRepository extends JpaRepository<KlassLeeslijst, Long> {
    List<KlassLeeslijst> findByLeerkrachtIdOrderByAangemaaktOpDesc(Long leerkrachtId);

    List<KlassLeeslijst> findByKlasAndZichtbaarTrueOrderByAangemaaktOpDesc(String klas);

    List<KlassLeeslijst> findByLeerlingIdAndZichtbaarTrueOrderByAangemaaktOpDesc(Long leerlingId);
}
