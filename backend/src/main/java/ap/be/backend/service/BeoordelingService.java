package ap.be.backend.service;

import java.util.List;

import org.springframework.stereotype.Service;

import ap.be.backend.config.SecurityUtils;
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
    private final FilterService filterService;

    public BeoordelingService(BeoordelingRepository beoordelingRepository, BoekRepository boekRepository,
            GerapporteerdeBeoordelingRepository gerapporteerdeBeoordelingRepository,
            GebruikerRepository gebruikerRepository, FilterService filterService) {
        this.beoordelingRepository = beoordelingRepository;
        this.boekRepository = boekRepository;
        this.gerapporteerdeBeoordelingRepository = gerapporteerdeBeoordelingRepository;
        this.gebruikerRepository = gebruikerRepository;
        this.filterService = filterService;
    }

    public List<BeoordelingDTO> getBeoordelingenOpBoekId(Long bookId) {
        if (!boekRepository.existsById(bookId)) {
            throw new BoekNotFoundException("Boek met id " + bookId + " niet gevonden");
        }

        List<Beoordeling> beoordelingen = beoordelingRepository.findByBoekId(bookId);
        Long huidigeGebruikerId = SecurityUtils.getCurrentGebruiker(gebruikerRepository).getId();

        return beoordelingen.stream().map(beoordeling -> {
            boolean isGerapporteerd = gerapporteerdeBeoordelingRepository
                    .existsByBeoordelingIdAndGebruikerMelder_GebruikerID(beoordeling.getId(), huidigeGebruikerId);
            return BeoordelingMapper.toDTO(beoordeling, isGerapporteerd);
        }).toList();
    }

    public void beoordelingOpslaan(BeoordelingDTO dto) {
        if (filterService.bevatScheldwoorden(dto.inhoud())) {
            throw new IllegalArgumentException("De beoordeling bevat ongepast taalgebruik en is geweigerd.");
        }

        Boek boek = boekRepository.findById(dto.boekId()).orElseThrow(
                () -> new BoekNotFoundException(
                        "Kan beoordeling niet opslaan: Boek met id " + dto.boekId() + " is niet gevonden."));

        Gebruiker gebruiker = SecurityUtils.getCurrentGebruiker(gebruikerRepository);

        Beoordeling beoordeling = BeoordelingMapper.toEntity(dto, boek, gebruiker);
        beoordelingRepository.save(beoordeling);

        double gemiddelde = beoordelingRepository.findByBoekId(boek.getId()).stream()
                .mapToInt(Beoordeling::getWaardering)
                .average()
                .orElse(0.0);
        boek.setPopulariteit(gemiddelde);
        boekRepository.save(boek);
    }

    public void verwijderBeoordeling(Long beoordelingId) {
        if (!beoordelingRepository.existsById(beoordelingId)) {
            throw new IllegalArgumentException("Beoordeling niet gevonden met id: " + beoordelingId);
        }

        beoordelingRepository.deleteById(beoordelingId);
    }
}
