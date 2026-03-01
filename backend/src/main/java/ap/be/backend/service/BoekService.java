package ap.be.backend.service;

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
        boek.setPublicatieJaar(boekDTO.publicatieJaar());
        boek.setGenre(boekDTO.genre());
        boek.setAantalPaginas(boekDTO.aantalPaginas());
        boek.setBeschrijving(boekDTO.beschrijving());
        boek.setAantalExemplaren(boekDTO.aantalExemplaren());
        boek.setLocatie(boekDTO.locatie());
        boek.setCoverUrl(boekDTO.coverUrl());
        return boekRepository.save(boek);

    }
}
