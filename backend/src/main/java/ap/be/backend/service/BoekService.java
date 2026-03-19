package ap.be.backend.service;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.PageRequest;
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
    }

    public Boek updateBoek(Long id, BoekDTO boekDTO) {
        Optional<Boek> bestaandBoek = boekRepository.findById(id);
        if (bestaandBoek.isEmpty()) {
            return null;
        }

        Boek boek = bestaandBoek.get();
        boek.setTitel(boekDTO.titel());
        boek.setAuteur(boekDTO.auteur());
        boek.setIsbn(boekDTO.isbn());
        boek.setUitgever(boekDTO.uitgever());
        boek.setUitgaveJaar(boekDTO.uitgaveJaar());
        boek.setGenre(boekDTO.genre());
        boek.setPagina(boekDTO.pagina());
        boek.setBeschrijving(boekDTO.beschrijving());
        boek.setAantalExemplaren(boekDTO.aantalExemplaren());
        boek.setLocatie(boekDTO.locatie());
        boek.setAfbeelding(boekDTO.afbeelding());
        return boekRepository.save(boek);
    }

    public boolean deleteBoek(Long id) {
        if (boekRepository.existsById(id)) {
            boekRepository.deleteById(id);
            return true;
        }
        return false;
    }

    public Boek getBoekByIsbn(String isbn) {
        return boekRepository.findByIsbn(isbn.trim()).orElse(null);
    }

    public List<Boek> getPopulaireBoeken() {
        return boekRepository.findTop5PopulaireBoeken();
    }
}
