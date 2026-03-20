package ap.be.backend;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Sort;

import ap.be.backend.entity.Boek;
import ap.be.backend.exception.InvalidYearRangeException;
import ap.be.backend.repository.BoekRepository;
import ap.be.backend.service.BoekService;

@ExtendWith(MockitoExtension.class)
public class BoekFilterServiceTest {

    @Mock
    private BoekRepository boekRepository;

    @InjectMocks
    private BoekService boekService;

    private final Sort defaultSort = Sort.by(Sort.Direction.ASC, "id");

    // --- GENRE FILTERS ---

    @Test
    void givenGeenFilters_whenFilterBoeken_thenRoeptRepositoryAanMetNullGenres() {
        // Given
        List<Boek> verwacht = List.of(new Boek("Boek A", "Auteur A"));
        when(boekRepository.filterBoeken(null, null, null, null, null, null, null, null, null, null, defaultSort))
                .thenReturn(verwacht);

        // When
        List<Boek> resultaat = boekService.filterBoeken(null, null, null, null, null,
                null, null, null, null, null, defaultSort);

        // Then
        assertEquals(verwacht, resultaat);
        verify(boekRepository).filterBoeken(null, null, null, null, null, null, null, null, null, null, defaultSort);
    }

    @Test
    void givenEnkelGenreInLijst_whenFilterBoeken_thenGeeftLijstAanRepository() {
        // Given
        List<String> genres = List.of("Filosofie");
        List<Boek> verwacht = List.of(new Boek("Socrates", "Plato"));
        when(boekRepository.filterBoeken(null, genres, null, null, null, null, null, null, null, null, defaultSort))
                .thenReturn(verwacht);

        // When
        List<Boek> resultaat = boekService.filterBoeken(null, genres, null, null, null,
                null, null, null, null, null, defaultSort);

        // Then
        assertEquals(verwacht, resultaat);
        verify(boekRepository).filterBoeken(null, genres, null, null, null, null, null, null, null, null, defaultSort);
    }

    @Test
    void givenHoofdgenrePlusSubgenres_whenFilterBoeken_thenGeeftVolleLijstAanRepository() {
        // Given - frontend stuurt hoofdgenre + alle subgenres als lijst
        List<String> genres = List.of("Non-fictie algemeen", "Filosofie", "Psychologie", "Geschiedenis");
        List<Boek> verwacht = List.of(
                new Boek("Sapiens", "Harari"),
                new Boek("Denken snel en langzaam", "Kahneman"));
        when(boekRepository.filterBoeken(null, genres, null, null, null, null, null, null, null, null, defaultSort))
                .thenReturn(verwacht);

        // When
        List<Boek> resultaat = boekService.filterBoeken(null, genres, null, null, null,
                null, null, null, null, null, defaultSort);

        // Then
        assertEquals(2, resultaat.size());
        verify(boekRepository).filterBoeken(null, genres, null, null, null, null, null, null, null, null, defaultSort);
    }

    @Test
    void givenLegeGenresLijst_whenFilterBoeken_thenRoeptRepositoryAanMetNull() {
        // Given - lege lijst moet omgezet worden naar null (geen genre-filter)
        List<String> legeGenres = Collections.emptyList();
        List<Boek> verwacht = List.of(new Boek("Alle Boeken", "Iedereen"));
        when(boekRepository.filterBoeken(null, null, null, null, null, null, null, null, null, null, defaultSort))
                .thenReturn(verwacht);

        // When
        List<Boek> resultaat = boekService.filterBoeken(null, legeGenres, null, null, null,
                null, null, null, null, null, defaultSort);

        // Then - lege lijst wordt als null doorgegeven
        assertEquals(verwacht, resultaat);
        verify(boekRepository).filterBoeken(null, null, null, null, null, null, null, null, null, null, defaultSort);
    }

    // --- JAAR VALIDATIE ---

    @Test
    void givenJaarVanKleinerDan1900_whenFilterBoeken_thenGooitInvalidYearRangeException() {
        // When & Then
        assertThrows(InvalidYearRangeException.class, () ->
                boekService.filterBoeken(null, null, null, null, null, null, 1899, null, null, null, defaultSort));
        verify(boekRepository, never()).filterBoeken(any(), any(), any(), any(), any(),
                any(), any(), any(), any(), any(), any());
    }

    @Test
    void givenJaarTotInDeToekomst_whenFilterBoeken_thenGooitInvalidYearRangeException() {
        // Given
        int toekomstjaar = java.time.Year.now().getValue() + 1;

        // When & Then
        assertThrows(InvalidYearRangeException.class, () ->
                boekService.filterBoeken(null, null, null, null, null, null, null, toekomstjaar, null, null, defaultSort));
        verify(boekRepository, never()).filterBoeken(any(), any(), any(), any(), any(),
                any(), any(), any(), any(), any(), any());
    }

    @Test
    void givenJaarVanGroterDanJaarTot_whenFilterBoeken_thenGooitInvalidYearRangeException() {
        // When & Then
        assertThrows(InvalidYearRangeException.class, () ->
                boekService.filterBoeken(null, null, null, null, null, null, 2020, 2010, null, null, defaultSort));
        verify(boekRepository, never()).filterBoeken(any(), any(), any(), any(), any(),
                any(), any(), any(), any(), any(), any());
    }

    @Test
    void givenGeldigJaarBereik_whenFilterBoeken_thenRoeptRepositoryAanMetJaren() {
        // Given
        List<Boek> verwacht = List.of(new Boek("Modern Boek", "Auteur"));
        when(boekRepository.filterBoeken(null, null, null, null, null, null, 2000, 2020, null, null, defaultSort))
                .thenReturn(verwacht);

        // When
        List<Boek> resultaat = boekService.filterBoeken(null, null, null, null, null,
                null, 2000, 2020, null, null, defaultSort);

        // Then
        assertEquals(verwacht, resultaat);
        verify(boekRepository).filterBoeken(null, null, null, null, null, null, 2000, 2020, null, null, defaultSort);
    }

    // --- OVERIGE FILTERS ---

    @Test
    void givenBeschikbaarFilter_whenFilterBoeken_thenGeeftBeschikbaarFlagAanRepository() {
        // Given
        Boek beschikbaarBoek = new Boek("Beschikbaar Boek", "Auteur");
        beschikbaarBoek.setAantalExemplaren(3);
        List<Boek> verwacht = List.of(beschikbaarBoek);
        when(boekRepository.filterBoeken(null, null, null, null, null, true, null, null, null, null, defaultSort))
                .thenReturn(verwacht);

        // When
        List<Boek> resultaat = boekService.filterBoeken(null, null, null, null, null,
                true, null, null, null, null, defaultSort);

        // Then
        assertEquals(1, resultaat.size());
        verify(boekRepository).filterBoeken(null, null, null, null, null, true, null, null, null, null, defaultSort);
    }

    @Test
    void givenLeesniveauFilter_whenFilterBoeken_thenGeeftLeesniveauAanRepository() {
        // Given
        Boek boek = new Boek("Eenvoudig Boek", "Auteur");
        boek.setLeesniveau("A (eenvoudig taalgebruik)");
        List<Boek> verwacht = List.of(boek);
        when(boekRepository.filterBoeken(null, null, null, null, null, null, null, null,
                null, "A (eenvoudig taalgebruik)", defaultSort))
                .thenReturn(verwacht);

        // When
        List<Boek> resultaat = boekService.filterBoeken(null, null, null, null, null,
                null, null, null, null, "A (eenvoudig taalgebruik)", defaultSort);

        // Then
        assertEquals(1, resultaat.size());
        assertEquals("A (eenvoudig taalgebruik)", resultaat.get(0).getLeesniveau());
    }

    @Test
    void givenZoektermFilter_whenFilterBoeken_thenGeeftZoektermAanRepository() {
        // Given
        Boek boek = new Boek("De Alchemist", "Paulo Coelho");
        List<Boek> verwacht = List.of(boek);
        when(boekRepository.filterBoeken("alchemist", null, null, null, null, null, null, null, null, null, defaultSort))
                .thenReturn(verwacht);

        // When
        List<Boek> resultaat = boekService.filterBoeken("alchemist", null, null, null, null,
                null, null, null, null, null, defaultSort);

        // Then
        assertEquals(1, resultaat.size());
        assertEquals("De Alchemist", resultaat.get(0).getTitel());
    }

    @Test
    void givenPopulariteitFilter_whenFilterBoeken_thenGeeftPopulariteitAanRepository() {
        // Given
        List<Boek> verwacht = List.of(new Boek("Top Boek", "Auteur"));
        when(boekRepository.filterBoeken(null, null, null, null, null, null, null, null, "1", null, defaultSort))
                .thenReturn(verwacht);

        // When
        List<Boek> resultaat = boekService.filterBoeken(null, null, null, null, null,
                null, null, null, "1", null, defaultSort);

        // Then
        assertEquals(1, resultaat.size());
        verify(boekRepository).filterBoeken(null, null, null, null, null, null, null, null, "1", null, defaultSort);
    }

    @Test
    void givenPaginaBereik_whenFilterBoeken_thenGeeftMinEnMaxPaginaAanRepository() {
        // Given
        List<Boek> verwacht = List.of(new Boek("Kort Boek", "Auteur"));
        when(boekRepository.filterBoeken(null, null, null, 100, 300, null, null, null, null, null, defaultSort))
                .thenReturn(verwacht);

        // When
        List<Boek> resultaat = boekService.filterBoeken(null, null, null, 100, 300,
                null, null, null, null, null, defaultSort);

        // Then
        assertEquals(1, resultaat.size());
        verify(boekRepository).filterBoeken(null, null, null, 100, 300, null, null, null, null, null, defaultSort);
    }
}
