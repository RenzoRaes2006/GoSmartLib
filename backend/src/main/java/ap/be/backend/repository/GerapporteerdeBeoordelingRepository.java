package ap.be.backend.repository;

import org.springframework.data.repository.ListCrudRepository;

import ap.be.backend.entity.GerapporteerdeBeoordeling;

public interface GerapporteerdeBeoordelingRepository extends ListCrudRepository<GerapporteerdeBeoordeling, Long> {

    boolean existsByBeoordelingIdAndUserMelder_Id(Long beoordelingId, Long userId);
}
