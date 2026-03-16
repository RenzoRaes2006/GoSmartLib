package ap.be.backend.service;

import java.util.List;

import org.springframework.stereotype.Service;

import ap.be.backend.dto.BeoordelingDTO;
import ap.be.backend.mappers.BeoordelingMapper;
import ap.be.backend.repository.BeoordelingRepository;
import ap.be.backend.repository.BoekRepository;
import ap.be.backend.repository.GerapporteerdeBeoordelingRepository;

@Service
public class BeoordelingService {
    private final BeoordelingRepository beoordelingRepository;
    private final BoekRepository boekRepository;

    public BeoordelingService(BeoordelingRepository beoordelingRepository, BoekRepository boekRepository) {
        this.beoordelingRepository = beoordelingRepository;
        this.boekRepository = boekRepository;
    }

    public List<BeoordelingDTO> getBeoordelingenOpBoekId(Long bookId) {
        var beoordelingen = beoordelingRepository.findByBoekId(bookId);

        return beoordelingen.stream().map(BeoordelingMapper::toDTO).toList();
    }

    public void beoordelingOpslaan(BeoordelingDTO dto) {
        var boek = boekRepository.findById(dto.boekId()).orElseThrow(
                () -> new RuntimeException("Boek niet gevonden"));
        var beoordeling = BeoordelingMapper.toEntity(dto, boek);
        beoordelingRepository.save(beoordeling);
    }

}
