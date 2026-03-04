package ap.be.backend.service;

import java.util.List;

import org.springframework.stereotype.Service;

import ap.be.backend.dto.BeoordelingDTO;
import ap.be.backend.mappers.BeoordelingMapper;
import ap.be.backend.repository.BeoordelingRepository;
import ap.be.backend.repository.BoekRepository;

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
<<<<<<< HEAD
        @SuppressWarnings("null")
        Beoordeling beoordeling = assemble(dto);
        beoordelingRepository.save(beoordeling);
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
=======
        var boek = boekRepository.findById(dto.boekId()).orElseThrow(
                () -> new RuntimeException("Boek niet gevonden"));
        var beoordeling = BeoordelingMapper.toEntity(dto, boek);
        beoordelingRepository.save(beoordeling);
>>>>>>> 02973030103b9ffe3e07b1f896294c71a57390af
    }
}
