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

import ap.be.backend.dto.BoekDTO;
import ap.be.backend.dto.BoekenFilters;
import ap.be.backend.dto.BoekenSort;
import ap.be.backend.dto.PageRequestParams;
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

    private final BoekenSort defaultSort = new BoekenSort(null, null);
    private final PageRequestParams defaultPaging = new PageRequestParams(0, 10);

    private BoekenFilters filters(
            String zoekterm, List<String> genre, List<String> tag, String uitgever,
            Integer minPagina, Integer maxPagina, String beschikbaar,
            Integer jaarVan, Integer jaarTot, String leesniveau,
            Boolean didactisch, DidactischeSubcategorie subcat) {
        return new BoekenFilters(zoekterm, genre, tag, uitgever, minPagina, maxPagina,
                beschikbaar, jaarVan, jaarTot, leesniveau, didactisch, subcat);
    }

    private void mockRepo(Page<Boek> returnPage) {
        when(boekRepository.filterBoeken(any(), any(), any(), any(), any(),
                any(), any(), any(), any(), any(), any(), any(), any()))
                .thenReturn(returnPage);
    }

    @Test
    void givenGeenFilters_whenFilterBoeken_thenRoeptRepositoryAanMetNullGenres() {
        Page<Boek> verwacht = new PageImpl<>(List.of(new Boek("Boek A", "Auteur A")));
        mockRepo(verwacht);

        Page<BoekDTO> resultaat = boekService.filterBoeken(
                filters(null, null, null, null, null, null, null, null, null, null, null, null),
                defaultSort, defaultPaging);

        assertEquals(verwacht.getContent().size(), resultaat.getContent().size());
        verify(boekRepository).filterBoeken(isNull(), isNull(), isNull(), isNull(), isNull(),
                isNull(), isNull(), isNull(), isNull(), isNull(), isNull(), isNull(), any());
    }

    @Test
    void givenEnkelGenreInLijst_whenFilterBoeken_thenGeeftLijstAanRepository() {
        List<String> genres = List.of("Filosofie");
        mockRepo(new PageImpl<>(List.of(new Boek("Socrates", "Plato"))));

        boekService.filterBoeken(
                filters(null, genres, null, null, null, null, null, null, null, null, null, null),
                defaultSort, defaultPaging);

        verify(boekRepository).filterBoeken(any(), eq(genres), any(), any(), any(),
                any(), any(), any(), any(), any(), any(), any(), any());
    }

    @Test
    void givenLegeGenresLijst_whenFilterBoeken_thenRoeptRepositoryAanMetNull() {
        List<String> legeGenres = Collections.emptyList();
        mockRepo(new PageImpl<>(List.of()));

        boekService.filterBoeken(
                filters(null, legeGenres, null, null, null, null, null, null, null, null, null, null),
                defaultSort, defaultPaging);

        verify(boekRepository).filterBoeken(any(), isNull(), any(), any(), any(),
                any(), any(), any(), any(), any(), any(), any(), any());
    }

    @Test
    void givenDidactischFilter_whenFilterBoeken_thenGeeftParametersDoorAanRepository() {
        mockRepo(new PageImpl<>(List.of(new Boek("Didactisch Boek", "Auteur"))));

        boekService.filterBoeken(
                filters(null, null, null, null, null, null, null, null, null, null, true,
                        DidactischeSubcategorie.LESMETHODES),
                defaultSort, defaultPaging);

        verify(boekRepository).filterBoeken(any(), any(), any(), any(), any(),
                any(), any(), any(), any(), any(), eq(true), eq(DidactischeSubcategorie.LESMETHODES), any());
    }

    @Test
    void givenJaarVanKleinerDan1900_whenFilterBoeken_thenGooitInvalidYearRangeException() {
        assertThrows(InvalidYearRangeException.class, () -> boekService.filterBoeken(
                filters(null, null, null, null, null, null, null, 1899, null, null, null, null),
                defaultSort, defaultPaging));
    }

    @Test
    void givenJaarVanGroterDanJaarTot_whenFilterBoeken_thenGooitInvalidYearRangeException() {
        assertThrows(InvalidYearRangeException.class, () -> boekService.filterBoeken(
                filters(null, null, null, null, null, null, null, 2020, 2010, null, null, null),
                defaultSort, defaultPaging));
    }

    @Test
    void givenLeesniveauFilter_whenFilterBoeken_thenGeeftLeesniveauAanRepository() {
        String niveau = "A (eenvoudig taalgebruik)";
        mockRepo(new PageImpl<>(List.of()));

        boekService.filterBoeken(
                filters(null, null, null, null, null, null, null, null, null, niveau, null, null),
                defaultSort, defaultPaging);

        verify(boekRepository).filterBoeken(any(), any(), any(), any(), any(),
                any(), any(), any(), any(), eq(niveau), any(), any(), any());
    }

    @Test
    void givenZoektermFilter_whenFilterBoeken_thenGeeftZoektermAanRepository() {
        String term = "alchemist";
        mockRepo(new PageImpl<>(List.of()));

        boekService.filterBoeken(
                filters(term, null, null, null, null, null, null, null, null, null, null, null),
                defaultSort, defaultPaging);

        verify(boekRepository).filterBoeken(eq(term), any(), any(), any(), any(),
                any(), any(), any(), any(), any(), any(), any(), any());
    }

    @Test
    void givenPaginaBereik_whenFilterBoeken_thenGeeftMinEnMaxPaginaAanRepository() {
        mockRepo(new PageImpl<>(List.of()));

        boekService.filterBoeken(
                filters(null, null, null, null, 100, 300, null, null, null, null, null, null),
                defaultSort, defaultPaging);

        verify(boekRepository).filterBoeken(any(), any(), any(), any(), eq(100), eq(300),
                any(), any(), any(), any(), any(), any(), any());
    }
}
