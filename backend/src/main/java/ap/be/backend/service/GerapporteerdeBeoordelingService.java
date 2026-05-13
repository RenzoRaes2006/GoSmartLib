package ap.be.backend.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;

import ap.be.backend.dto.GerapporteerdeBeoordelingAdminDTO;
import ap.be.backend.entity.GerapporteerdeBeoordeling;
import ap.be.backend.exception.BeoordelingNotFoundException;
import ap.be.backend.exception.DuplicateReportException;
import ap.be.backend.exception.GebruikerNotFoundException;
import ap.be.backend.exception.RapportageNotFoundException;
import ap.be.backend.mappers.GerapporteerdeBeoordelingMapper;
import ap.be.backend.repository.BeoordelingRepository;
import ap.be.backend.repository.GerapporteerdeBeoordelingRepository;
import ap.be.backend.repository.UserRepository;

@Service
public class GerapporteerdeBeoordelingService {
    private final GerapporteerdeBeoordelingRepository gerapporteerdeBeoordelingRepository;
    private final BeoordelingRepository beoordelingRepository;
    private final UserRepository userRepository;

    public GerapporteerdeBeoordelingService(GerapporteerdeBeoordelingRepository gerapporteerdeBeoordelingRepository,
            BeoordelingRepository beoordelingRepository, UserRepository userRepository) {
        this.gerapporteerdeBeoordelingRepository = gerapporteerdeBeoordelingRepository;
        this.beoordelingRepository = beoordelingRepository;
        this.userRepository = userRepository;
    }

    public void rapporteerBeoordeling(Long beoordelingId, Long userId, String reden) {

        if (reden == null || reden.trim().isEmpty()) {
            throw new IllegalArgumentException("Reden mag niet leeg zijn.");
        }

        if (gerapporteerdeBeoordelingRepository.existsByBeoordelingIdAndUserMelder_Id(beoordelingId, userId)) {
            throw new DuplicateReportException("Je hebt deze beoordeling al gerapporteerd.");
        }

        var beoordeling = beoordelingRepository.findById(beoordelingId)
                .orElseThrow(
                        () -> new BeoordelingNotFoundException("Beoordeling niet gevonden met id: " + beoordelingId));

        var user = userRepository.findById(userId)
                .orElseThrow(() -> new GebruikerNotFoundException("User niet gevonden met id: " + userId));

        GerapporteerdeBeoordeling gerapporteerdeBeoordeling = new GerapporteerdeBeoordeling();
        gerapporteerdeBeoordeling.setBeoordeling(beoordeling);
        gerapporteerdeBeoordeling.setReden(reden);
        gerapporteerdeBeoordeling.setUserMelder(user);
        gerapporteerdeBeoordeling.setDatumGemeld(LocalDateTime.now());

        gerapporteerdeBeoordelingRepository.save(gerapporteerdeBeoordeling);
    }

    public List<GerapporteerdeBeoordelingAdminDTO> getAlleGerapporteerdeBeoordelingen() {
        return gerapporteerdeBeoordelingRepository.findAll().stream()
                .map(GerapporteerdeBeoordelingMapper::toAdminDTO)
                .toList();
    }

    public void verwijderRapportage(Long rapportageId) {
        if (!gerapporteerdeBeoordelingRepository.existsById(rapportageId)) {
            throw new RapportageNotFoundException("Rapportage niet gevonden met id: " + rapportageId);
        }

        gerapporteerdeBeoordelingRepository.deleteById(rapportageId);
    }
}
