package ap.be.backend.service;

import java.util.Arrays;
import java.util.List;
import org.springframework.stereotype.Service;

import ap.be.backend.dto.BoekDTO;
import ap.be.backend.entity.Boek;
import ap.be.backend.repository.BoekRepository;

@Service
public class BoekService {
    private final BoekRepository boekRepository;

    public BoekService(BoekRepository boekRepository) {
        this.boekRepository = boekRepository;
    }

    public Boek addBoek(BoekDTO boekDTO) {
        if (boekRepository.findByIsbn(boekDTO.isbn()).isPresent()) {
            throw new IllegalArgumentException("Boek met ISBN " + boekDTO.isbn() + " bestaat al.");
        }

        Boek boek = new Boek();
        boek.setTitel(boekDTO.titel());
        boek.setAuteur(boekDTO.auteur());
        boek.setIsbn(boekDTO.isbn());
        boek.setUitgever(boekDTO.uitgever());
        boek.setUitgaveJaar(boekDTO.uitgaveJaar());
        boek.setGenre(Arrays.asList(boekDTO.genre()));
        boek.setPagina(boekDTO.pagina());
        boek.setBeschrijving(boekDTO.beschrijving());
        boek.setAantalExemplaren(boekDTO.aantalExemplaren());
        boek.setLocatie(boekDTO.locatie());
        boek.setAfbeelding(boekDTO.afbeelding());
        return boekRepository.save(boek);
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
    }
}
