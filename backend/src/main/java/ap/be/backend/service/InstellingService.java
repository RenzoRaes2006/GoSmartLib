package ap.be.backend.service;

import org.springframework.stereotype.Service;

import ap.be.backend.entity.Instelling;
import ap.be.backend.repository.InstellingRepository;

@Service
public class InstellingService {
    private final InstellingRepository instellingRepository;

    public InstellingService(InstellingRepository instellingRepository) {
        this.instellingRepository = instellingRepository;
    }

    public int getLeenLimiet() {
        return instellingRepository.findById("MAX_LEEN_LIMIET")
                .map(instelling -> Integer.parseInt(instelling.getWaarde()))
                .orElse(0); // Default value if not set
    }

    public void updateLeenLimiet(int nieuweLimiet) {
        Instelling limietInstelling = instellingRepository.findById("MAX_LEEN_LIMIET")
                .orElse(new Instelling("MAX_LEEN_LIMIET", "0"));

        limietInstelling.setWaarde(String.valueOf(nieuweLimiet));
        instellingRepository.save(limietInstelling);
    }
}
