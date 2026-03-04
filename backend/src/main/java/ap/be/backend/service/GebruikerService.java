package ap.be.backend.service;

import org.springframework.stereotype.Service;

import ap.be.backend.dto.GebruikerDTO;
import ap.be.backend.entity.Gebruiker;
import ap.be.backend.repository.GebruikerRepository;

@Service
public class GebruikerService {
    private final GebruikerRepository gebruikerRepository;

    public GebruikerService(GebruikerRepository gebruikerRepository) {
        this.gebruikerRepository = gebruikerRepository;
    }

    public GebruikerDTO mapToDTO(Gebruiker gebruiker) {
        return new GebruikerDTO(
            gebruiker.getGebruikerID(),
            gebruiker.getSchoolId(),
            gebruiker.getVoorNaam(),
            gebruiker.getAchternaam(),
            gebruiker.getEmail(),
            gebruiker.getRol().toString()
        );
    }

    public Gebruiker getGebruikerById(Long id) {
        @SuppressWarnings("null")
        Long safeId = id;
        return gebruikerRepository.findById(safeId)
            .orElseThrow(() -> new IllegalArgumentException("Gebruiker met ID " + safeId + " niet gevonden"));
    }
}
