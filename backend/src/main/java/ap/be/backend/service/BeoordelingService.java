package ap.be.backend.service;

import java.util.List;

import org.springframework.stereotype.Service;

import ap.be.backend.dto.BeoordelingDTO;
import ap.be.backend.entity.Beoordeling;
import ap.be.backend.repository.BeoordelingRepository;

@Service
public class BeoordelingService {
    private final BeoordelingRepository beoordelingRepository;

    public BeoordelingService(BeoordelingRepository beoordelingRepository) {
        this.beoordelingRepository = beoordelingRepository;
    }

    public List<BeoordelingDTO> getReviewByBookId(Long bookId) {
        var beoordelingen = beoordelingRepository.findByBoekId(bookId);
        return beoordelingen.stream().map(this::assemble).toList();
    }

    public void beoordelingOpslaan(BeoordelingDTO dto) {
        beoordelingRepository.save(assemble(dto));
    }

    private BeoordelingDTO assemble(Beoordeling beoordeling) {
        return new BeoordelingDTO(
                beoordeling.getId(),
                beoordeling.getGebruikerId(),
                beoordeling.getBoekId(),
                beoordeling.getWaardering(),
                beoordeling.getInhoud());
    }

    private Beoordeling assemble(BeoordelingDTO dto) {
        Beoordeling beoordeling = new Beoordeling(
                dto.gebruikerId(),
                dto.boekId(),
                dto.waardering(),
                dto.inhoud());
        beoordeling.setId(dto.id()); 
        return beoordeling;
    }
}
