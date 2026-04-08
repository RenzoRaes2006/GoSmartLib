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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.*;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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
    @Test
    void givenPendingLoans_whenGetAlleAanvragen_thenReturnList() {
        // given: the repository returns a list of loans with status "AANGEVRAAGD"
        testUitlening.setStatus("AANGEVRAAGD");
        List<Uitlening> expectedAanvragen = Arrays.asList(testUitlening);
        given(uitleningRepository.findByStatus("AANGEVRAAGD")).willReturn(expectedAanvragen);

        // when: we fetch all pending requests
        List<Uitlening> actualAanvragen = gebruikerService.getAlleAanvragen();

        // then: the correct list is returned
        assertEquals(1, actualAanvragen.size());
        assertEquals("AANGEVRAAGD", actualAanvragen.get(0).getStatus());
        then(uitleningRepository).should(times(1)).findByStatus("AANGEVRAAGD");
    }

    @Test
    void givenValidLoanId_whenKeurAanvraagGoed_thenUpdateStatusAndDates() {
        // given: a valid pending loan exists
        testUitlening.setStatus("AANGEVRAAGD");
        given(uitleningRepository.findById(100L)).willReturn(Optional.of(testUitlening));

        // when: the librarian approves the loan
        gebruikerService.keurAanvraagGoed(100L);

        // then: status changes to GOEDGEKEURD and the loan is saved
        assertEquals("GOEDGEKEURD", testUitlening.getStatus());
        assertNotNull(testUitlening.getUitleenDatum());
        assertNotNull(testUitlening.getInleverDatum());
        then(uitleningRepository).should(times(1)).save(testUitlening);
    }

    @Test
    void givenValidLoanId_whenWeigerAanvraag_thenUpdateStatusAndRestoreStock() {
        // given: a valid pending loan exists and the book has a stock of 5
        testUitlening.setStatus("AANGEVRAAGD");
        testBoek.setAantalExemplaren(5); 
        given(uitleningRepository.findById(100L)).willReturn(Optional.of(testUitlening));

        // when: the librarian rejects the loan
        gebruikerService.weigerAanvraag(100L);

        // then: status is GEWEIGERD, it is marked as returned (so it doesn't count towards the limit)
        assertEquals("GEWEIGERD", testUitlening.getStatus());
        assertTrue(testUitlening.isIngeleverd());
        
        // then: the book stock is increased back to 6
        assertEquals(6, testBoek.getAantalExemplaren());
        
        // then: both the book and the loan are saved to the database
        then(boekRepository).should(times(1)).save(testBoek);
        then(uitleningRepository).should(times(1)).save(testUitlening);
    }

    @Test
    void givenInvalidLoanId_whenKeurAanvraagGoed_thenThrowException() {
        // given: an invalid loan ID that cannot be found
        Long invalidId = 999L;
        given(uitleningRepository.findById(invalidId)).willReturn(Optional.empty());

        // when & then: an exception is thrown
        assertThrows(ap.be.backend.exception.UitleningNotFoundException.class, () -> {
            gebruikerService.keurAanvraagGoed(invalidId);
        });
        
        // verify that save was never called
        then(uitleningRepository).should(never()).save(any(Uitlening.class));
    }

    @Test
    void givenInvalidLoanId_whenWeigerAanvraag_thenThrowException() {
        // given: an invalid loan ID that cannot be found
        Long invalidId = 999L;
        given(uitleningRepository.findById(invalidId)).willReturn(Optional.empty());

        // when & then: an exception is thrown
        assertThrows(ap.be.backend.exception.UitleningNotFoundException.class, () -> {
            gebruikerService.weigerAanvraag(invalidId);
        });

        // verify that save was never called on either repository
        then(boekRepository).should(never()).save(any(Boek.class));
        then(uitleningRepository).should(never()).save(any(Uitlening.class));
    }
}