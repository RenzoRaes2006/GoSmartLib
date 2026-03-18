package ap.be.backend.service;

import org.springframework.stereotype.Service;

import ap.be.backend.entity.GerapporteerdeBeoordeling;
import ap.be.backend.exception.BeoordelingNotFoundException;
import ap.be.backend.exception.DuplicateReportException;
import ap.be.backend.exception.GebruikerNotFoundException;
import ap.be.backend.repository.BeoordelingRepository;
import ap.be.backend.repository.GebruikerRepository;
import ap.be.backend.repository.GerapporteerdeBeoordelingRepository;

@Service
public class GerapporteerdeBeoordelingService {
    private final GerapporteerdeBeoordelingRepository gerapporteerdeBeoordelingRepository;
    private final BeoordelingRepository beoordelingRepository;
    private final GebruikerRepository gebruikerRepository;

    public GerapporteerdeBeoordelingService(GerapporteerdeBeoordelingRepository gerapporteerdeBeoordelingRepository,
            BeoordelingRepository beoordelingRepository, GebruikerRepository gebruikerRepository) {
        this.gerapporteerdeBeoordelingRepository = gerapporteerdeBeoordelingRepository;
        this.beoordelingRepository = beoordelingRepository;
        this.gebruikerRepository = gebruikerRepository;
    }

    public void rapporteerBeoordeling(Long beoordelingId, Long gebruikerId, String reden) {
        
        if (reden == null || reden.trim().isEmpty()) {
            throw new IllegalArgumentException("Reden mag niet leeg zijn.");
        }

        if (gerapporteerdeBeoordelingRepository.existsByBeoordelingIdAndGebruiker_GebruikerID(beoordelingId, gebruikerId)) {
            throw new DuplicateReportException("Je hebt deze beoordeling al gerapporteerd.");
        }

        var beoordeling = beoordelingRepository.findById(beoordelingId)
                .orElseThrow(() -> new BeoordelingNotFoundException("Beoordeling niet gevonden met id: " + beoordelingId));

        var gebruiker = gebruikerRepository.findById(gebruikerId)
                .orElseThrow(() -> new GebruikerNotFoundException("Gebruiker niet gevonden met id: " + gebruikerId));

        GerapporteerdeBeoordeling gerapporteerdeBeoordeling = new GerapporteerdeBeoordeling();
        gerapporteerdeBeoordeling.setBeoordeling(beoordeling);
        gerapporteerdeBeoordeling.setReden(reden);
        gerapporteerdeBeoordeling.setGebruiker(gebruiker);

        gerapporteerdeBeoordelingRepository.save(gerapporteerdeBeoordeling);
    }
}
