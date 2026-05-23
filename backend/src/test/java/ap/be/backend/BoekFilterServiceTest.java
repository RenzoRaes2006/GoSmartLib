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
import ap.be.backend.exception.InvalidYearRangeException;
import ap.be.backend.repository.BoekRepository;
import ap.be.backend.repository.CampusRepository;
import ap.be.backend.repository.TagRepository;
import ap.be.backend.service.BoekService;
import ap.be.backend.service.TagService;

@ExtendWith(MockitoExtension.class)
public class BoekFilterServiceTest {

    @Mock
    private BoekRepository boekRepository;

    @Mock
    private TagRepository tagRepository;

    @Mock
    private TagService tagService;

    @Mock
    private CampusRepository campusRepository;

    @InjectMocks
    private BoekService boekService;

    private final BoekenSort defaultSort = new BoekenSort(null, null);
    private final PageRequestParams defaultPaging = new PageRequestParams(0, 10);

    private BoekenFilters filters(
            String zoekterm, List<Long> tagIds, String uitgever,
            Integer minPagina, Integer maxPagina, String beschikbaar,
            Integer jaarVan, Integer jaarTot, String leesniveau,
            Boolean didactisch) {
        return new BoekenFilters(zoekterm, tagIds, uitgever, minPagina, maxPagina,
                beschikbaar, jaarVan, jaarTot, leesniveau, didactisch, null, null);
    }

    private BoekenFilters filtersMetCampus(Long... campusIds) {
        return new BoekenFilters(null, null, null, null, null, null, null, null, null, null,
                campusIds == null ? null : java.util.Arrays.asList(campusIds), null);
    }

    private BoekenFilters filtersMetMinSterren(Integer minSterren) {
        return new BoekenFilters(null, null, null, null, null, null, null, null, null, null,
                null, minSterren);
    }

    private void mockRepo(Page<Boek> returnPage) {
        when(boekRepository.filterBoeken(any(), any(), any(), any(), any(),
                any(), any(), any(), any(), any(), any(), any(), any(), any()))
                .thenReturn(returnPage);
    }

    @Test
    void givenGeenFilters_whenFilterBoeken_thenRoeptRepositoryAanMetNullTagIds() {
        Page<Boek> verwacht = new PageImpl<>(List.of(new Boek("Boek A", "Auteur A")));
        mockRepo(verwacht);

        Page<BoekDTO> resultaat = boekService.filterBoeken(
                filters(null, null, null, null, null, null, null, null, null, null),
                defaultSort, defaultPaging);

        assertEquals(verwacht.getContent().size(), resultaat.getContent().size());
        verify(boekRepository).filterBoeken(isNull(), isNull(), isNull(), isNull(), isNull(),
                isNull(), isNull(), isNull(), isNull(), isNull(), isNull(), isNull(), isNull(), any());
    }

    @Test
    void givenTagIds_whenFilterBoeken_thenGeeftLijstAanRepository() {
        when(tagService.expandWithSubtags(any())).thenAnswer(inv -> inv.getArgument(0));
        List<Long> tagIds = List.of(1L, 2L);
        when(tagService.expandWithSubtags(tagIds)).thenReturn(tagIds);
        mockRepo(new PageImpl<>(List.of(new Boek("Socrates", "Plato"))));

        boekService.filterBoeken(
                filters(null, tagIds, null, null, null, null, null, null, null, null),
                defaultSort, defaultPaging);

        verify(boekRepository).filterBoeken(any(), eq(tagIds), any(), any(), any(),
                any(), any(), any(), any(), any(), any(), any(), any(), any());
    }

    @Test
    void givenLegeTagIdsLijst_whenFilterBoeken_thenRoeptRepositoryAanMetNull() {
        List<Long> legeIds = Collections.emptyList();
        mockRepo(new PageImpl<>(List.of()));

        boekService.filterBoeken(
                filters(null, legeIds, null, null, null, null, null, null, null, null),
                defaultSort, defaultPaging);

        verify(boekRepository).filterBoeken(any(), isNull(), any(), any(), any(),
                any(), any(), any(), any(), any(), any(), any(), any(), any());
    }

    @Test
    void givenDidactischFilter_whenFilterBoeken_thenGeeftParametersDoorAanRepository() {
        mockRepo(new PageImpl<>(List.of(new Boek("Didactisch Boek", "Auteur"))));

        boekService.filterBoeken(
                filters(null, null, null, null, null, null, null, null, null, true),
                defaultSort, defaultPaging);

        verify(boekRepository).filterBoeken(any(), any(), any(), any(), any(),
                any(), any(), any(), any(), eq(true), any(), any(), any(), any());
    }

    @Test
    void givenJaarVanKleinerDan1900_whenFilterBoeken_thenGooitInvalidYearRangeException() {
        assertThrows(InvalidYearRangeException.class, () -> boekService.filterBoeken(
                filters(null, null, null, null, null, null, 1899, null, null, null),
                defaultSort, defaultPaging));
    }

    @Test
    void givenJaarVanGroterDanJaarTot_whenFilterBoeken_thenGooitInvalidYearRangeException() {
        assertThrows(InvalidYearRangeException.class, () -> boekService.filterBoeken(
                filters(null, null, null, null, null, null, 2020, 2010, null, null),
                defaultSort, defaultPaging));
    }

    @Test
    void givenLeesniveauFilter_whenFilterBoeken_thenGeeftLeesniveauAanRepository() {
        String niveau = "A (eenvoudig taalgebruik)";
        mockRepo(new PageImpl<>(List.of()));

        boekService.filterBoeken(
                filters(null, null, null, null, null, null, null, null, niveau, null),
                defaultSort, defaultPaging);

        verify(boekRepository).filterBoeken(any(), any(), any(), any(), any(),
                any(), any(), any(), eq(niveau), any(), any(), any(), any(), any());
    }

    @Test
    void givenZoektermFilter_whenFilterBoeken_thenGeeftZoektermAanRepository() {
        String term = "alchemist";
        mockRepo(new PageImpl<>(List.of()));

        boekService.filterBoeken(
                filters(term, null, null, null, null, null, null, null, null, null),
                defaultSort, defaultPaging);

        verify(boekRepository).filterBoeken(eq(term), any(), any(), any(), any(),
                any(), any(), any(), any(), any(), any(), any(), any(), any());
    }

    @Test
    void givenPaginaBereik_whenFilterBoeken_thenGeeftMinEnMaxPaginaAanRepository() {
        mockRepo(new PageImpl<>(List.of()));

        boekService.filterBoeken(
                filters(null, null, null, 100, 300, null, null, null, null, null),
                defaultSort, defaultPaging);

        verify(boekRepository).filterBoeken(any(), any(), any(), eq(100), eq(300),
                any(), any(), any(), any(), any(), any(), any(), any(), any());
    }

    @Test
    void givenCampusFilter_whenFilterBoeken_thenGeeftCampusIdsAanRepository() {
        Long campusId = 42L;
        mockRepo(new PageImpl<>(Collections.<Boek>emptyList()));

        boekService.filterBoeken(filtersMetCampus(campusId), defaultSort, defaultPaging);

        verify(boekRepository).filterBoeken(any(), any(), any(), any(), any(),
                any(), any(), any(), any(), any(), any(), eq(List.of(campusId)), any(), any());
    }

    @Test
    void givenMeerdereCampusFilters_whenFilterBoeken_thenGeeftLijstAanRepository() {
        mockRepo(new PageImpl<>(Collections.<Boek>emptyList()));

        boekService.filterBoeken(filtersMetCampus(1L, 2L), defaultSort, defaultPaging);

        verify(boekRepository).filterBoeken(any(), any(), any(), any(), any(),
                any(), any(), any(), any(), any(), any(), eq(List.of(1L, 2L)), any(), any());
    }

    @Test
    void givenLegeCampusFilter_whenFilterBoeken_thenGeeftNullAanRepository() {
        mockRepo(new PageImpl<>(Collections.<Boek>emptyList()));

        boekService.filterBoeken(filtersMetCampus(), defaultSort, defaultPaging);

        verify(boekRepository).filterBoeken(any(), any(), any(), any(), any(),
                any(), any(), any(), any(), any(), any(), isNull(), any(), any());
    }

    @Test
    void givenSchoolId_whenFilterBoeken_thenGeeftSchoolIdAanRepository() {
        Long schoolId = 7L;
        mockRepo(new PageImpl<>(Collections.<Boek>emptyList()));

        boekService.filterBoeken(filters(null, null, null, null, null, null, null, null, null, null),
                defaultSort, defaultPaging, schoolId);

        verify(boekRepository).filterBoeken(any(), any(), any(), any(), any(),
                any(), any(), any(), any(), any(), eq(schoolId), any(), any(), any());
    }

    @Test
    void givenMinSterrenFilter_whenFilterBoeken_thenGeeftMinSterrenAanRepository() {
        mockRepo(new PageImpl<>(Collections.<Boek>emptyList()));

        boekService.filterBoeken(filtersMetMinSterren(4), defaultSort, defaultPaging);

        verify(boekRepository).filterBoeken(any(), any(), any(), any(), any(),
                any(), any(), any(), any(), any(), any(), any(), eq(4), any());
    }
}
