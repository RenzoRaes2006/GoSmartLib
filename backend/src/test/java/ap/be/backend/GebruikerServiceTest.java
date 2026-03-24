package ap.be.backend;

import ap.be.backend.entity.Boek;
import ap.be.backend.entity.Gebruiker;
import ap.be.backend.entity.LeeslijstMap;
import ap.be.backend.entity.Uitlening;
import ap.be.backend.repository.BoekRepository;
import ap.be.backend.repository.LeeslijstMapRepository;
import ap.be.backend.repository.UitleningRepository;
import ap.be.backend.service.GebruikerService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.BDDMockito.*;

@ExtendWith(MockitoExtension.class)
public class GebruikerServiceTest {

    @Mock
    private LeeslijstMapRepository leeslijstMapRepository;

    @Mock
    private BoekRepository boekRepository;

    @Mock
    private UitleningRepository uitleningRepository;

    @InjectMocks
    private GebruikerService gebruikerService;

    private Boek testBoek;
    private Gebruiker testGebruiker;
    private Uitlening testUitlening;

    @BeforeEach
    void setUp() {
        // initialize basic test data for maps
        testBoek = new Boek();
        testBoek.setId(1L);
        testBoek.setTitel("Test Boek");

        // initialize basic test data for loans
        testGebruiker = new Gebruiker();
        testGebruiker.setGebruikerID(1L);

        testUitlening = new Uitlening(testGebruiker, testBoek, LocalDate.now(), LocalDate.now().plusMonths(1));
        testUitlening.setId(100L);
    }

    @Test
    void testVerwijderBoekUitMap_VerwijdertMapAlsLeeg_TenzijFavorieten() {
        // arrange: create a normal map "School" with 1 book
        LeeslijstMap schoolMap = new LeeslijstMap();
        schoolMap.setNaam("School");
        schoolMap.getBoeken().add(testBoek);

        when(leeslijstMapRepository.findByGebruiker_GebruikerIDAndNaam(1L, "School"))
                .thenReturn(Optional.of(schoolMap));
        when(boekRepository.findById(1L)).thenReturn(Optional.of(testBoek));

        // act: remove the book
        gebruikerService.verwijderBoekUitMap(1L, "School", 1L);

        // assert: map is empty now, so delete() must be called
        verify(leeslijstMapRepository, times(1)).delete(schoolMap);
        verify(leeslijstMapRepository, never()).save(schoolMap);
    }

    @Test
    void testVerwijderBoekUitMap_BehoudtMapAlsLeeg_OmdatHetFavorietenIs() {
        // arrange: create the "Favorieten" map with 1 book
        LeeslijstMap favoMap = new LeeslijstMap();
        favoMap.setNaam("Favorieten");
        favoMap.getBoeken().add(testBoek);

        when(leeslijstMapRepository.findByGebruiker_GebruikerIDAndNaam(1L, "Favorieten"))
                .thenReturn(Optional.of(favoMap));
        when(boekRepository.findById(1L)).thenReturn(Optional.of(testBoek));

        // act: remove the book
        gebruikerService.verwijderBoekUitMap(1L, "Favorieten", 1L);

        // assert: map is empty now, but should NOT be deleted, save() must be called
        verify(leeslijstMapRepository, never()).delete(favoMap);
        verify(leeslijstMapRepository, times(1)).save(favoMap);
    }

    @Test
    void givenUserWithActiveLoans_whenGetActieveLeningenVanGebruiker_thenReturnLoanList() {
        // given
        Long userId = 1L;
        List<Uitlening> expectedLoans = Arrays.asList(testUitlening);
        given(uitleningRepository.findByGebruiker_GebruikerIDAndIsIngeleverdFalse(userId)).willReturn(expectedLoans);

        // when
        List<Uitlening> actualLoans = gebruikerService.getActieveLeningenVanGebruiker(userId);

        // then
        assertEquals(1, actualLoans.size());
        assertEquals("Test Boek", actualLoans.get(0).getBoek().getTitel());
        then(uitleningRepository).should(times(1)).findByGebruiker_GebruikerIDAndIsIngeleverdFalse(userId);
    }

    @Test
    void givenUserWithNoActiveLoans_whenGetActieveLeningenVanGebruiker_thenReturnEmptyList() {
        // given
        Long userId = 2L;
        given(uitleningRepository.findByGebruiker_GebruikerIDAndIsIngeleverdFalse(userId)).willReturn(Collections.emptyList());

        // when
        List<Uitlening> actualLoans = gebruikerService.getActieveLeningenVanGebruiker(userId);

        // then
        assertTrue(actualLoans.isEmpty());
        then(uitleningRepository).should(times(1)).findByGebruiker_GebruikerIDAndIsIngeleverdFalse(userId);
    }
}