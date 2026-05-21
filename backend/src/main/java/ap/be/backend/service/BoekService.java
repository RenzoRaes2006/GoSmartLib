package ap.be.backend.service;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import ap.be.backend.dto.BoekDTO;
import ap.be.backend.dto.BoekenFilters;
import ap.be.backend.dto.BoekenSort;
import ap.be.backend.dto.PageRequestParams;
import ap.be.backend.dto.TagDTO;
import ap.be.backend.entity.Boek;
import ap.be.backend.entity.Tag;
import ap.be.backend.exception.InvalidYearRangeException;
import ap.be.backend.exception.IsbnAlreadyExistsException;
import ap.be.backend.mappers.BoekMapper;
import ap.be.backend.repository.BoekRepository;
import ap.be.backend.repository.TagRepository;

@Service
public class BoekService {
    private final BoekRepository boekRepository;
    private final TagRepository tagRepository;
    private final TagService tagService;

    public BoekService(BoekRepository boekRepository, TagRepository tagRepository, TagService tagService) {
        this.boekRepository = boekRepository;
        this.tagRepository = tagRepository;
        this.tagService = tagService;
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
        boek.setTags(resolveTags(boekDTO.tags()));
        boek.setPagina(boekDTO.pagina());
        boek.setBeschrijving(boekDTO.beschrijving());
        boek.setAantalExemplaren(boekDTO.aantalExemplaren());
        boek.setLocatie(isNotBlank(boekDTO.locatie()) ? boekDTO.locatie() : "Geen locatie");
        boek.setAfbeelding(isNotBlank(boekDTO.afbeelding()) ? boekDTO.afbeelding() : "");
        boek.setLeesniveau(isNotBlank(boekDTO.leesniveau()) ? boekDTO.leesniveau() : "Geen leesniveau");
        boek.setDidactischMateriaal(Boolean.TRUE.equals(boekDTO.didactischMateriaal()));
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
        Set<Tag> nieuweTags = resolveTags(boekDTO.tags());
        boek.getTags().clear();
        boek.getTags().addAll(nieuweTags);
        boek.setPagina(boekDTO.pagina());
        boek.setBeschrijving(boekDTO.beschrijving());
        boek.setAantalExemplaren(boekDTO.aantalExemplaren());
        boek.setLocatie(isNotBlank(boekDTO.locatie()) ? boekDTO.locatie() : "Geen locatie");
        boek.setAfbeelding(isNotBlank(boekDTO.afbeelding()) ? boekDTO.afbeelding() : "");
        boek.setLeesniveau(isNotBlank(boekDTO.leesniveau()) ? boekDTO.leesniveau() : "Geen leesniveau");
        boek.setDidactischMateriaal(Boolean.TRUE.equals(boekDTO.didactischMateriaal()));
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

    public Page<BoekDTO> filterBoeken(BoekenFilters filters, BoekenSort sort, PageRequestParams paging) {
        validateYearRange(filters.uitgaveJaarVan(), filters.uitgaveJaarTot());
        List<Long> tagIdsParam = (filters.tagIds() == null || filters.tagIds().isEmpty())
                ? null
                : tagService.expandWithSubtags(filters.tagIds());
        Page<Boek> page = boekRepository.filterBoeken(
                filters.zoekterm(),
                tagIdsParam,
                filters.uitgever(),
                filters.minPagina(),
                filters.maxPagina(),
                filters.beschikbaarAsBoolean(),
                filters.uitgaveJaarVan(),
                filters.uitgaveJaarTot(),
                filters.leesniveau(),
                filters.didactischMateriaal(),
                paging.toPageable(sort.toSort()));
        return page.map(BoekMapper::toDTO);
    }

    private Set<Tag> resolveTags(List<TagDTO> tagDtos) {
        if (tagDtos == null || tagDtos.isEmpty()) {
            return new HashSet<>();
        }
        Set<Tag> resolved = new HashSet<>();
        for (TagDTO dto : tagDtos) {
            if (dto == null || dto.id() == null) continue;
            tagRepository.findById(dto.id()).ifPresent(resolved::add);
        }
        return resolved;
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
