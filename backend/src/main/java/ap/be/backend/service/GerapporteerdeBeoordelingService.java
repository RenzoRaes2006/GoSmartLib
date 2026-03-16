package ap.be.backend.service;

import org.springframework.stereotype.Service;

import ap.be.backend.entity.GerapporteerdeBeoordeling;
import ap.be.backend.repository.BeoordelingRepository;
import ap.be.backend.repository.GerapporteerdeBeoordelingRepository;

@Service
public class GerapporteerdeBeoordelingService {
    private final GerapporteerdeBeoordelingRepository gerapporteerdeBeoordelingRepository;
    private final BeoordelingRepository beoordelingRepository;

    public GerapporteerdeBeoordelingService(GerapporteerdeBeoordelingRepository gerapporteerdeBeoordelingRepository, BeoordelingRepository beoordelingRepository) {
        this.gerapporteerdeBeoordelingRepository = gerapporteerdeBeoordelingRepository;
        this.beoordelingRepository = beoordelingRepository;
    }

    public void rapporteerBeoordeling(Long id, String reden) {
        var beoordeling = beoordelingRepository.findById(id).orElseThrow(
                () -> new RuntimeException("Beoordeling niet gevonden"));
        var gerapporteerdeBeoordeling = new GerapporteerdeBeoordeling();
        gerapporteerdeBeoordeling.setBeoordeling(beoordeling);
        gerapporteerdeBeoordeling.setReden(reden);

        gerapporteerdeBeoordelingRepository.save(gerapporteerdeBeoordeling);
    }
}
