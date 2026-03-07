package ap.be.backend.service;

import org.springframework.stereotype.Service;

import ap.be.backend.dto.GebruikerDTO;
import ap.be.backend.entity.Boek;
import ap.be.backend.entity.Gebruiker;
import ap.be.backend.entity.LeeslijstMap;
import ap.be.backend.entity.Rol;
import ap.be.backend.repository.BoekRepository;
import ap.be.backend.repository.GebruikerRepository;
import ap.be.backend.repository.LeeslijstMapRepository;

@Service
public class GebruikerService {
    private final GebruikerRepository gebruikerRepository;
    private final LeeslijstMapRepository leeslijstMapRepository;
    private final BoekRepository boekRepository;

    public GebruikerService(GebruikerRepository gebruikerRepository, LeeslijstMapRepository leeslijstMapRepository,
            BoekRepository boekRepository) {
        this.gebruikerRepository = gebruikerRepository;
        this.leeslijstMapRepository = leeslijstMapRepository;
        this.boekRepository = boekRepository;
    }

    public GebruikerDTO mapToDTO(Gebruiker gebruiker) {
        return new GebruikerDTO(
                gebruiker.getGebruikerID(),
                gebruiker.getSchoolId(),
                gebruiker.getVoorNaam(),
                gebruiker.getAchternaam(),
                gebruiker.getEmail(),
                gebruiker.getRol().toString());
    }

    public Gebruiker maakGebruiker(GebruikerDTO nieuwGebruikerDTO) {

        Gebruiker nieuweGebruiker = new Gebruiker();
        nieuweGebruiker.setSchoolId(nieuwGebruikerDTO.schoolId());
        nieuweGebruiker.setVoorNaam(nieuwGebruikerDTO.voorNaam());
        nieuweGebruiker.setAchternaam(nieuwGebruikerDTO.achternaam());
        nieuweGebruiker.setEmail(nieuwGebruikerDTO.email());
        nieuweGebruiker.setRol(Rol.valueOf(nieuwGebruikerDTO.rol().toUpperCase()));

        Gebruiker opgeslagenGebruiker = gebruikerRepository.save(nieuweGebruiker);

        LeeslijstMap favorietenMap = new LeeslijstMap("Favorieten", opgeslagenGebruiker);
        leeslijstMapRepository.save(favorietenMap);

        return opgeslagenGebruiker;
    }

    public Gebruiker getGebruikerById(Long id) {
        return gebruikerRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Gebruiker met ID " + id + " niet gevonden"));
    }

    public void voegBoekToeAanMap(Long gebruikerId, String mapNaam, Long boekId) {
        Gebruiker gebruiker = getGebruikerById(gebruikerId);
        Boek boek = boekRepository.findById(boekId)
                .orElseThrow(() -> new IllegalArgumentException("Boek met ID " + boekId + " niet gevonden"));

        LeeslijstMap map = leeslijstMapRepository.findByGebruiker_GebruikerIDAndNaam(gebruikerId, mapNaam)
                .orElseGet(() -> {
                    LeeslijstMap nieuweMap = new LeeslijstMap(mapNaam, gebruiker);
                    return leeslijstMapRepository.save(nieuweMap);
                });

        map.voegBoekToe(boek);
        leeslijstMapRepository.save(map);
    }
}
