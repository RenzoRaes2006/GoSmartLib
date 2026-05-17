package ap.be.backend;

import ap.be.backend.dto.LeenStatusResponse;
import ap.be.backend.entity.Boek;
import ap.be.backend.entity.User;
import ap.be.backend.entity.LeeslijstMap;
import ap.be.backend.entity.Uitlening;
import ap.be.backend.exception.InvalidImportDataException;
import ap.be.backend.exception.LeenVoorwaardeException;
import ap.be.backend.exception.OnvoldoendeVoorraadException;
import ap.be.backend.exception.ResourceNotFoundException;
import ap.be.backend.repository.BoekRepository;
import ap.be.backend.repository.UserRepository;
import ap.be.backend.repository.LeeslijstMapRepository;
import ap.be.backend.repository.UitleningRepository;
import ap.be.backend.service.GebruikerService;
import ap.be.backend.service.InstellingService;

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
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.*;

@ExtendWith(MockitoExtension.class)
public class GebruikerServiceTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private LeeslijstMapRepository leeslijstMapRepository;
    @Mock
    private BoekRepository boekRepository;
    @Mock
    private UitleningRepository uitleningRepository;

    @Mock
    private InstellingService instellingService;

    @InjectMocks
    private GebruikerService gebruikerService;

    private Boek testBoek;
    private User testGebruiker;
    private Uitlening testUitlening;

    @BeforeEach
    void setUp() {
        testBoek = new Boek();
        testBoek.setId(1L);
        testBoek.setTitel("Test Boek");

        testGebruiker = new User();
        testGebruiker.setId(1L);

        testUitlening = new Uitlening(testGebruiker, testBoek, LocalDate.now(), LocalDate.now().plusMonths(1));
        testUitlening.setId(100L);
    }

    @Test
    void testVerwijderBoekUitMap_VerwijdertMapAlsLeeg_TenzijFavorieten() {
        LeeslijstMap schoolMap = new LeeslijstMap();
        schoolMap.setNaam("School");
        schoolMap.getBoeken().add(testBoek);

        when(leeslijstMapRepository.findByUser_IdAndNaam(1L, "School"))
                .thenReturn(Optional.of(schoolMap));
        when(boekRepository.findById(1L)).thenReturn(Optional.of(testBoek));

        gebruikerService.verwijderBoekUitMap(1L, "School", 1L);

        verify(leeslijstMapRepository, times(1)).delete(schoolMap);
        verify(leeslijstMapRepository, never()).save(schoolMap);
    }

    @Test
    void givenUserWithActiveLoans_whenGetActieveLeningenVanGebruiker_thenReturnLoanList() {
        Long userId = 1L;
        List<Uitlening> expectedLoans = Arrays.asList(testUitlening);
        given(uitleningRepository.findByUser_IdAndIsIngeleverdFalse(userId)).willReturn(expectedLoans);

        List<Uitlening> actualLoans = gebruikerService.getActieveLeningenVanGebruiker(userId);

        assertEquals(1, actualLoans.size());
    }

    @Test
    void givenValidLoanId_whenKeurAanvraagGoed_thenUpdateStatusAndDates() {
        testUitlening.setStatus("AANGEVRAAGD");
        given(uitleningRepository.findById(100L)).willReturn(Optional.of(testUitlening));

        // CRUCIAAL: Vertel de mock hoeveel dagen hij moet teruggeven!
        when(instellingService.getInlevertermijnDagen()).thenReturn(14);

        gebruikerService.keurAanvraagGoed(100L);

        assertEquals("GOEDGEKEURD", testUitlening.getStatus());
        assertNotNull(testUitlening.getUitleenDatum());
        assertNotNull(testUitlening.getInleverDatum());
        then(uitleningRepository).should(times(1)).save(testUitlening);
    }

    @Test
    void givenValidLoanId_whenWeigerAanvraag_thenUpdateStatusAndRestoreStock() {
        testUitlening.setStatus("AANGEVRAAGD");
        testBoek.setAantalExemplaren(5);
        given(uitleningRepository.findById(100L)).willReturn(Optional.of(testUitlening));

        gebruikerService.weigerAanvraag(100L);

        assertEquals("GEWEIGERD", testUitlening.getStatus());
        assertTrue(testUitlening.isIngeleverd());
        assertEquals(6, testBoek.getAantalExemplaren());
    }

    @Test
    void givenNoOverdueBooks_whenCheckLeenStatus_thenReturnTrue() {
        Long userId = 1L;
        Long bookId = 2L;

        Boek mockBoek = new Boek();
        mockBoek.setAantalExemplaren(5);

        given(boekRepository.findById(bookId)).willReturn(Optional.of(mockBoek));
        given(uitleningRepository.existsByUser_IdAndIsIngeleverdFalseAndInleverDatumBefore(
                eq(userId), any(LocalDate.class))).willReturn(false);
        given(uitleningRepository.existsByUser_IdAndBoek_IdAndIsIngeleverdFalse(userId, bookId))
                .willReturn(false);
        given(instellingService.getLeenLimiet()).willReturn(3);
        given(uitleningRepository.findByUser_IdAndIsIngeleverdFalse(userId)).willReturn(List.of());

        LeenStatusResponse response = gebruikerService.checkLeenStatus(userId, bookId);

        assertTrue(response.isMagLenen(), "De gebruiker moet mogen lenen.");
        assertEquals("Beschikbaar om te reserveren", response.getReden());
    }

    @Test
    void givenLimietIsDrieEnGebruikerHeeftDrieLeningen_whenCheckLeenStatus_thenReturnsFalse() {
        Long gebruikerId = 1L;
        Long boekId = 100L;

        Boek boek = new Boek();
        boek.setId(boekId);
        boek.setAantalExemplaren(5);

        when(boekRepository.findById(boekId)).thenReturn(Optional.of(boek));
        when(uitleningRepository.existsByUser_IdAndBoek_IdAndIsIngeleverdFalse(gebruikerId, boekId))
                .thenReturn(false);
        when(uitleningRepository.existsByUser_IdAndIsIngeleverdFalseAndInleverDatumBefore(eq(gebruikerId),
                any())).thenReturn(false);

        when(instellingService.getLeenLimiet()).thenReturn(3);

        List<Uitlening> actieveLeningen = Arrays.asList(new Uitlening(), new Uitlening(), new Uitlening());
        when(uitleningRepository.findByUser_IdAndIsIngeleverdFalse(gebruikerId))
                .thenReturn(actieveLeningen);

        LeenStatusResponse response = gebruikerService.checkLeenStatus(gebruikerId, boekId);

        assertFalse(response.isMagLenen(), "De gebruiker mag niet lenen omdat de limiet is bereikt.");
        assertTrue(response.getReden().contains("limiet van 3 boeken bereikt"));
    }

    @Test
    void givenValidData_whenBevestigBulkUitlening_thenSavesUitleningenAndUpdateStock() {
        Long leraarId = 1L;
        List<Long> boekIds = Arrays.asList(10L, 11L);

        User leraar = new User();
        leraar.setId(leraarId);

        Boek boek1 = new Boek("De Hobbit", "Tolkien");
        boek1.setId(10L);
        boek1.setAantalExemplaren(3);

        Boek boek2 = new Boek("1984", "Orwell");
        boek2.setId(11L);
        boek2.setAantalExemplaren(1);

        when(userRepository.findById(leraarId)).thenReturn(Optional.of(leraar));
        when(boekRepository.findById(10L)).thenReturn(Optional.of(boek1));
        when(boekRepository.findById(11L)).thenReturn(Optional.of(boek2));

        // CRUCIAAL: Vertel de mock hoeveel dagen hij moet teruggeven!
        when(instellingService.getInlevertermijnDagen()).thenReturn(14);

        gebruikerService.bevestigBulkUitlening(leraarId, boekIds);

        assertEquals(2, boek1.getAantalExemplaren(), "Voorraad boek 1 moet met 1 verminderd zijn");
        assertEquals(0, boek2.getAantalExemplaren(), "Voorraad boek 2 moet met 1 verminderd zijn");

        verify(boekRepository, times(2)).save(any(Boek.class));
        verify(uitleningRepository, times(2)).save(any(Uitlening.class));
    }

    @Test
    void givenEmptyList_whenBevestigBulkUitlening_thenThrowsInvalidImportDataException() {
        assertThrows(InvalidImportDataException.class, () -> {
            gebruikerService.bevestigBulkUitlening(1L, Collections.emptyList());
        });
        verify(uitleningRepository, never()).save(any());
    }

    @Test
    void givenOutOfStockBook_whenBevestigBulkUitlening_thenThrowsOnvoldoendeVoorraadException() {
        Long leraarId = 1L;
        User leraar = new User();
        leraar.setId(leraarId);

        Boek boekUitVerkocht = new Boek("Clean Code", "Robert C. Martin");
        boekUitVerkocht.setId(5L);
        boekUitVerkocht.setAantalExemplaren(0);

        when(userRepository.findById(leraarId)).thenReturn(Optional.of(leraar));
        when(boekRepository.findById(5L)).thenReturn(Optional.of(boekUitVerkocht));

        assertThrows(OnvoldoendeVoorraadException.class, () -> {
            gebruikerService.bevestigBulkUitlening(leraarId, Arrays.asList(5L));
        });
    }
}