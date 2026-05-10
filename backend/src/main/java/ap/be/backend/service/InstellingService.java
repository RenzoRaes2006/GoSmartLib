package ap.be.backend.service;

import org.springframework.stereotype.Service;

import ap.be.backend.entity.Instelling;
import ap.be.backend.repository.InstellingRepository;

/**
 * Beheert de configuratie-instellingen van de applicatie die in de database
 * worden opgeslagen, zoals het maximale aantal boeken dat een leerling
 * tegelijk mag lenen.
 */
@Service
public class InstellingService {
    private final InstellingRepository instellingRepository;

    public InstellingService(InstellingRepository instellingRepository) {
        this.instellingRepository = instellingRepository;
    }

    /**
     * Geeft de huidige uitleenlimiet terug.
     *
     * Geeft 0 terug als de instelling nog niet in de database staat.
     *
     * @return het maximale aantal boeken dat tegelijk geleend mag worden
     */
    public int getLeenLimiet() {
        return instellingRepository.findById("MAX_LEEN_LIMIET")
                .map(instelling -> Integer.parseInt(instelling.getWaarde()))
                .orElse(0);
    }

    /**
     * Slaat een nieuwe uitleenlimiet op in de database.
     *
     * Bestaat de instelling al, dan wordt de waarde bijgewerkt. Anders wordt
     * een nieuwe instelling aangemaakt.
     *
     * @param nieuweLimiet het nieuwe maximale aantal boeken dat tegelijk
     *                     geleend mag worden
     */
    public void updateLeenLimiet(int nieuweLimiet) {
        Instelling limietInstelling = instellingRepository.findById("MAX_LEEN_LIMIET")
                .orElse(new Instelling("MAX_LEEN_LIMIET", "0"));

        limietInstelling.setWaarde(String.valueOf(nieuweLimiet));
        instellingRepository.save(limietInstelling);
    }
}
