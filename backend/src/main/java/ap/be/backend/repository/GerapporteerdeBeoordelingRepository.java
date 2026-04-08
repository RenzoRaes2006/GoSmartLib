package ap.be.backend.repository;

import org.springframework.data.repository.ListCrudRepository;

import ap.be.backend.entity.GerapporteerdeBeoordeling;

public interface GerapporteerdeBeoordelingRepository extends ListCrudRepository<GerapporteerdeBeoordeling, Long> {

    // Controleert of een gebruiker al een beoordeling heeft gerapporteerd
    boolean existsByBeoordelingIdAndGebruikerMelder_GebruikerID(Long beoordelingId, Long gebruikerId);
}
