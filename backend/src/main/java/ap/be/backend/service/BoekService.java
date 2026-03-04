package ap.be.backend.service;

<<<<<<< HEAD
import java.util.Arrays;
import java.util.List;
=======
>>>>>>> 02973030103b9ffe3e07b1f896294c71a57390af
import org.springframework.stereotype.Service;

import ap.be.backend.dto.BoekDTO;
import ap.be.backend.entity.Boek;
import ap.be.backend.exception.IsbnAlreadyExistsException;
import ap.be.backend.repository.BoekRepository;

@Service
public class BoekService {
    private final BoekRepository boekRepository;

    public BoekService(BoekRepository boekRepository) {
        this.boekRepository = boekRepository;
    }

    public Boek addBoek(BoekDTO boekDTO) {
        // Trim ISBN to avoid whitespace issues
        String trimmedIsbn = boekDTO.isbn().trim();

        if (boekRepository.findByIsbn(trimmedIsbn).isPresent()) {
            throw new IsbnAlreadyExistsException("Boek met ISBN " + trimmedIsbn + " bestaat al.");
        }

        Boek boek = new Boek();
        boek.setTitel(boekDTO.titel());
        boek.setAuteur(boekDTO.auteur());
        boek.setIsbn(trimmedIsbn);
        boek.setUitgever(boekDTO.uitgever());
        boek.setUitgaveJaar(boekDTO.uitgaveJaar());
        boek.setGenre(boekDTO.genre());
        boek.setPagina(boekDTO.pagina());
        boek.setBeschrijving(boekDTO.beschrijving());
        boek.setAantalExemplaren(boekDTO.aantalExemplaren());
        boek.setLocatie(boekDTO.locatie());
        boek.setAfbeelding(boekDTO.afbeelding());
        return boekRepository.save(boek);
<<<<<<< HEAD
    }

    public BoekDTO mapToDTO(Boek boek) {
        return new BoekDTO(
            boek.getId(),
            boek.getTitel(),
            boek.getAuteur(),
            boek.getIsbn(),
            boek.getUitgever(),
            boek.getUitgaveJaar(),
            boek.getPagina(),
            boek.getGenre().isEmpty() ? "" : boek.getGenre().get(0),
            boek.getBeschrijving(),
            boek.getAantalExemplaren(),
            boek.getLocatie(),
            boek.getAfbeelding()
        );
    }

    public List<BoekDTO> getAllBoeken() {
        return boekRepository.findAll().stream()
            .map(this::mapToDTO)
            .toList();
=======
>>>>>>> 02973030103b9ffe3e07b1f896294c71a57390af
    }
}
