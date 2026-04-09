package ap.be.backend.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import ap.be.backend.dto.BoekDTO;
import ap.be.backend.entity.Boek;
import ap.be.backend.exception.InvalidYearRangeException;
import ap.be.backend.exception.IsbnAlreadyExistsException;
import ap.be.backend.repository.BoekRepository;
import org.springframework.transaction.annotation.Transactional;

@Service
public class BoekService {
    private final BoekRepository boekRepository;

    public BoekService(BoekRepository boekRepository) {
        this.boekRepository = boekRepository;
    }

    public Boek addBoek(BoekDTO boekDTO) {
        String trimmedIsbn = boekDTO.isbn().trim();

        if (boekRepository.findByIsbn(trimmedIsbn).isPresent()) {
            throw new IsbnAlreadyExistsException("Boek met ISBN " + trimmedIsbn + " bestaat al.");
        }

        Boek boek = new Boek();
        boek.setTitel(boekDTO.titel());
        boek.setAuteur(boekDTO.auteur());
        boek.setIsbn(trimmedIsbn);
        boek.setUitgever(isNotBlank(boekDTO.uitgever()) ? boekDTO.uitgever() : "Geen uitgever");
        boek.setUitgaveJaar(isNotBlank(boekDTO.uitgaveJaar()) ? boekDTO.uitgaveJaar() : "Geen uitgavejaar");
        boek.setGenre(boekDTO.genre() != null && !boekDTO.genre().isEmpty() ? boekDTO.genre() : new ArrayList<>());
        boek.setThematischeTag(boekDTO.thematischeTag() != null && !boekDTO.thematischeTag().isEmpty() ? boekDTO.thematischeTag() : new ArrayList<>());
        boek.setPagina(boekDTO.pagina());
        boek.setBeschrijving(boekDTO.beschrijving());
        boek.setAantalExemplaren(boekDTO.aantalExemplaren());
        boek.setLocatie(isNotBlank(boekDTO.locatie()) ? boekDTO.locatie() : "Geen locatie");
        boek.setAfbeelding(isNotBlank(boekDTO.afbeelding()) ? boekDTO.afbeelding() : "");
        boek.setLeesniveau(isNotBlank(boekDTO.leesniveau()) ? boekDTO.leesniveau() : "Geen leesniveau");
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
        boek.setUitgever(isNotBlank(boekDTO.uitgever()) ? boekDTO.uitgever() : "Geen uitgever");
        boek.setUitgaveJaar(isNotBlank(boekDTO.uitgaveJaar()) ? boekDTO.uitgaveJaar() : "Geen uitgavejaar");
        boek.setGenre(boekDTO.genre() != null && !boekDTO.genre().isEmpty() ? boekDTO.genre() : new ArrayList<>());
        boek.setThematischeTag(boekDTO.thematischeTag() != null && !boekDTO.thematischeTag().isEmpty() ? boekDTO.thematischeTag() : new ArrayList<>());
        boek.setPagina(boekDTO.pagina());
        boek.setBeschrijving(boekDTO.beschrijving());
        boek.setAantalExemplaren(boekDTO.aantalExemplaren());
        boek.setLocatie(isNotBlank(boekDTO.locatie()) ? boekDTO.locatie() : "Geen locatie");
        boek.setAfbeelding(isNotBlank(boekDTO.afbeelding()) ? boekDTO.afbeelding() : "");
        boek.setLeesniveau(isNotBlank(boekDTO.leesniveau()) ? boekDTO.leesniveau() : "Geen leesniveau");
        return boekRepository.save(boek);
    }

    @Transactional
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

    public Page<Boek> filterBoeken(String zoekterm, List<String> genres, List<String> thematischeTags, String uitgever,
            Integer minPagina,
            Integer maxPagina,
            Boolean beschikbaarheid, Integer uitgaveJaarVan, Integer uitgaveJaarTot, String leesniveau, Integer page,
            Integer size,
            Sort sort) {
        validateYearRange(uitgaveJaarVan, uitgaveJaarTot);
        PageRequest pageable = PageRequest.of(page != null ? page : 0, size != null ? size : 10, sort);
        List<String> genresParam = (genres == null || genres.isEmpty()) ? null : genres;
        List<String> tagsParam = (thematischeTags == null || thematischeTags.isEmpty()) ? null : thematischeTags;
        return boekRepository.filterBoeken(zoekterm, genresParam, tagsParam, uitgever, minPagina, maxPagina,
                beschikbaarheid,
                uitgaveJaarVan, uitgaveJaarTot, leesniveau, pageable);
    }

    private void validateYearRange(Integer uitgaveJaarVan, Integer uitgaveJaarTot) {
        int currentYear = java.time.Year.now().getValue();

        if (uitgaveJaarVan != null && uitgaveJaarVan < 1900) {
            throw new InvalidYearRangeException("Jaar 'van' moet na 1900 liggen");
        }

        if (uitgaveJaarTot != null && uitgaveJaarTot < 1900) {
            throw new InvalidYearRangeException("Jaar 'tot' moet na 1900 liggen");
        }

        if (uitgaveJaarVan != null && uitgaveJaarVan > currentYear) {
            throw new InvalidYearRangeException("Jaar 'van' mag niet in de toekomst liggen");
        }

        if (uitgaveJaarTot != null && uitgaveJaarTot > currentYear) {
            throw new InvalidYearRangeException("Jaar 'tot' mag niet in de toekomst liggen");
        }

        if (uitgaveJaarVan != null && uitgaveJaarTot != null && uitgaveJaarVan > uitgaveJaarTot) {
            throw new InvalidYearRangeException("Jaar 'van' moet kleiner of gelijk zijn aan jaar 'tot'");
        }
    }

    public Boek getBoekById(Long id) {
        return boekRepository.findById(id).orElse(null);
    }

    public Page<Boek> getBoekenPaged(int page, int size) {
        return boekRepository.findAll(PageRequest.of(page, size, Sort.by("titel").ascending()));
    }

    public List<Boek> getPopulaireBoeken() {
        return boekRepository.findTop5PopulaireBoeken();
    }

    private boolean isNotBlank(String value) {
        return value != null && !value.trim().isEmpty();
    }

}
