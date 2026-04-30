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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Sort;

import ap.be.backend.entity.Boek;
import ap.be.backend.entity.DidactischeSubcategorie;
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

    // Helper om de repository mock met 13 parameters te definiëren (conform BoekRepository)
    private void mockRepo(Page<Boek> returnPage) {
        when(boekRepository.filterBoeken(any(), any(), any(), any(), any(),
                any(), any(), any(), any(), any(), any(), any(), any()))
                .thenReturn(returnPage);
    }

    // --- GENRE FILTERS ---

    @Test
    void givenGeenFilters_whenFilterBoeken_thenRoeptRepositoryAanMetNullGenres() {
        // Given
        Page<Boek> verwacht = new PageImpl<>(List.of(new Boek("Boek A", "Auteur A")));
        mockRepo(verwacht);

        // When (15 parameters voor de service)
        Page<Boek> resultaat = boekService.filterBoeken(null, null, null, null, null,
                null, null, null, null, null, null, null, 0, 10, defaultSort);

        // Then
        assertEquals(verwacht.getContent(), resultaat.getContent());
        verify(boekRepository).filterBoeken(isNull(), isNull(), isNull(), isNull(), isNull(),
                isNull(), isNull(), isNull(), isNull(), isNull(), isNull(), isNull(), any());
    }

    @Test
    void givenEnkelGenreInLijst_whenFilterBoeken_thenGeeftLijstAanRepository() {
        // Given
        List<String> genres = List.of("Filosofie");
        Page<Boek> verwacht = new PageImpl<>(List.of(new Boek("Socrates", "Plato")));
        mockRepo(verwacht);

        // When
        Page<Boek> resultaat = boekService.filterBoeken(null, genres, null, null, null,
                null, null, null, null, null, null, null, 0, 10, defaultSort);

        // Then
        verify(boekRepository).filterBoeken(any(), eq(genres), any(), any(), any(),
                any(), any(), any(), any(), any(), any(), any(), any());
    }

    @Test
    void givenLegeGenresLijst_whenFilterBoeken_thenRoeptRepositoryAanMetNull() {
        // Given
        List<String> legeGenres = Collections.emptyList();
        mockRepo(new PageImpl<>(List.of()));

        // When
        boekService.filterBoeken(null, legeGenres, null, null, null,
                null, null, null, null, null, null, null, 0, 10, defaultSort);

        // Then - lege lijst moet door de service naar null worden omgezet voor de repo
        verify(boekRepository).filterBoeken(any(), isNull(), any(), any(), any(),
                any(), any(), any(), any(), any(), any(), any(), any());
    }

    // --- DIDACTISCH MATERIAAL (NIEUW) ---

    @Test
    void givenDidactischFilter_whenFilterBoeken_thenGeeftParametersDoorAanRepository() {
        // Given
        Page<Boek> verwacht = new PageImpl<>(List.of(new Boek("Didactisch Boek", "Auteur")));
        mockRepo(verwacht);

        // When
        boekService.filterBoeken(null, null, null, null, null,
                null, null, null, null, null, true, DidactischeSubcategorie.LESMETHODES, 0, 10, defaultSort);

        // Then
        verify(boekRepository).filterBoeken(any(), any(), any(), any(), any(),
                any(), any(), any(), any(), any(), eq(true), eq(DidactischeSubcategorie.LESMETHODES), any());
    }

    // --- JAAR VALIDATIE ---

    @Test
    void givenJaarVanKleinerDan1900_whenFilterBoeken_thenGooitInvalidYearRangeException() {
        // When & Then
        assertThrows(InvalidYearRangeException.class, () -> 
            boekService.filterBoeken(null, null, null, null, null, 
                null, null, 1899, null, null, null, null, 0, 10, defaultSort));
    }

    @Test
    void givenJaarVanGroterDanJaarTot_whenFilterBoeken_thenGooitInvalidYearRangeException() {
        // When & Then
        assertThrows(InvalidYearRangeException.class, () -> 
            boekService.filterBoeken(null, null, null, null, null, 
                null, null, 2020, 2010, null, null, null, 0, 10, defaultSort));
    }

    // --- OVERIGE FILTERS ---

    @Test
    void givenLeesniveauFilter_whenFilterBoeken_thenGeeftLeesniveauAanRepository() {
        // Given
        String niveau = "A (eenvoudig taalgebruik)";
        mockRepo(new PageImpl<>(List.of()));

        // When
        boekService.filterBoeken(null, null, null, null, null,
                null, null, null, null, niveau, null, null, 0, 10, defaultSort);

        // Then
        verify(boekRepository).filterBoeken(any(), any(), any(), any(), any(),
                any(), any(), any(), any(), eq(niveau), any(), any(), any());
    }

    @Test
    void givenZoektermFilter_whenFilterBoeken_thenGeeftZoektermAanRepository() {
        // Given
        String term = "alchemist";
        mockRepo(new PageImpl<>(List.of()));

        // When
        boekService.filterBoeken(term, null, null, null, null,
                null, null, null, null, null, null, null, 0, 10, defaultSort);

        // Then
        verify(boekRepository).filterBoeken(eq(term), any(), any(), any(), any(),
                any(), any(), any(), any(), any(), any(), any(), any());
    }

    @Test
    void givenPaginaBereik_whenFilterBoeken_thenGeeftMinEnMaxPaginaAanRepository() {
        // Given
        mockRepo(new PageImpl<>(List.of()));

        // When
        boekService.filterBoeken(null, null, null, null, 100, 300,
                null, null, null, null, null, null, 0, 10, defaultSort);

        // Then
        verify(boekRepository).filterBoeken(any(), any(), any(), any(), eq(100), eq(300),
                any(), any(), any(), any(), any(), any(), any());
    }
}