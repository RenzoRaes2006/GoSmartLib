package ap.be.backend.service;

import java.util.List;

import org.springframework.stereotype.Service;

import ap.be.backend.dto.BeoordelingDTO;
import ap.be.backend.entity.Beoordeling;
import ap.be.backend.entity.Boek;
import ap.be.backend.entity.Gebruiker;
import ap.be.backend.exception.BoekNotFoundException;
import ap.be.backend.mappers.BeoordelingMapper;
import ap.be.backend.repository.BeoordelingRepository;
import ap.be.backend.repository.BoekRepository;
import ap.be.backend.repository.GebruikerRepository;
import ap.be.backend.repository.GerapporteerdeBeoordelingRepository;

@Service
public class BeoordelingService {
    private final BeoordelingRepository beoordelingRepository;
    private final BoekRepository boekRepository;
    private final GerapporteerdeBeoordelingRepository gerapporteerdeBeoordelingRepository;
    private final GebruikerRepository gebruikerRepository;


    public BeoordelingService(BeoordelingRepository beoordelingRepository, BoekRepository boekRepository,
            GerapporteerdeBeoordelingRepository gerapporteerdeBeoordelingRepository, GebruikerRepository gebruikerRepository) {
        this.beoordelingRepository = beoordelingRepository;
        this.boekRepository = boekRepository;
        this.gerapporteerdeBeoordelingRepository = gerapporteerdeBeoordelingRepository;
        this.gebruikerRepository = gebruikerRepository;
    }

    public List<BeoordelingDTO> getBeoordelingenOpBoekId(Long bookId) {
        if (!boekRepository.existsById(bookId)) {
            throw new BoekNotFoundException("Boek met id " + bookId + " niet gevonden");
        }

        List<Beoordeling> beoordelingen = beoordelingRepository.findByBoekId(bookId);
        Long tijdelijkGebruikerId = 1L; // TODO: Vervang dit met de daadwerkelijke gebruiker ID uit de beveiligingscontext of sessie

        return beoordelingen.stream().map(beoordeling -> {
            boolean isGerapporteerd = gerapporteerdeBeoordelingRepository
                    .existsByBeoordelingIdAndGebruiker_GebruikerID(beoordeling.getId(), tijdelijkGebruikerId);
            return BeoordelingMapper.toDTO(beoordeling, isGerapporteerd);
        }).toList();
    }

    public void beoordelingOpslaan(BeoordelingDTO dto) {
        Boek boek = boekRepository.findById(dto.boekId()).orElseThrow(
                () -> new BoekNotFoundException(
                        "Kan beoordeling niet opslaan: Boek met id " + dto.boekId() + " is niet gevonden."));

        // TIJDELIJKE ID (TODO: later vervangen door ID uit Spring Security context)
        Long tijdelijkGebruikerId = 1L; 

        // OPHALEN UIT DB
        Gebruiker gebruiker = gebruikerRepository.findById(tijdelijkGebruikerId).orElseThrow(
                () -> new RuntimeException("Gebruiker niet gevonden met id: " + tijdelijkGebruikerId));


        Beoordeling beoordeling = BeoordelingMapper.toEntity(dto, boek, gebruiker); // TODO: Vervang null met daadwerkelijke Gebruiker object op basis van dto.gebruikerId()
        beoordelingRepository.save(beoordeling);
    }
}
