package ap.be.backend;

import ap.be.backend.dto.LeenStatusResponse;
import ap.be.backend.entity.Boek;
import ap.be.backend.entity.Exemplaar;
import ap.be.backend.entity.ExemplaarStatus;
import ap.be.backend.entity.User;
import ap.be.backend.entity.LeeslijstMap;
import ap.be.backend.entity.Uitlening;
import ap.be.backend.exception.InvalidImportDataException;
import ap.be.backend.exception.OnvoldoendeVoorraadException;
import ap.be.backend.repository.BoekRepository;
import ap.be.backend.repository.UserRepository;
import ap.be.backend.repository.LeeslijstMapRepository;
import ap.be.backend.repository.UitleningRepository;
import ap.be.backend.service.ExemplaarService;
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

    @Mock
    private ExemplaarService exemplaarService;

    @InjectMocks
    private GebruikerService gebruikerService;

    private Boek testBoek;
    private Exemplaar testExemplaar;
    private User testGebruiker;
    private Uitlening testUitlening;

    @BeforeEach
    void setUp() {
        testBoek = new Boek();
        testBoek.setId(1L);
        testBoek.setTitel("Test Boek");

        testExemplaar = new Exemplaar(testBoek, 1, ExemplaarStatus.UITGELEEND);

        testGebruiker = new User();
        testGebruiker.setId(1L);

        testUitlening = new Uitlening(testGebruiker, testExemplaar, LocalDate.now(), LocalDate.now().plusMonths(1));
        testUitlening.setId(100L);
    }

    @Test
    void givenLegeMap_whenVerwijderBoekUitMap_thenMapVerwijderd() {
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
        given(uitleningRepository.findById(100L)).willReturn(Optional.of(testUitlening));

        gebruikerService.weigerAanvraag(100L);

        assertEquals("GEWEIGERD", testUitlening.getStatus());
        assertTrue(testUitlening.isIngeleverd());
        verify(exemplaarService, times(1)).neemTerug(testExemplaar);
    }

    @Test
    void givenNoOverdueBooks_whenCheckLeenStatus_thenReturnTrue() {
        Long userId = 1L;
        Long bookId = 2L;

        Boek mockBoek = new Boek();

        given(boekRepository.findById(bookId)).willReturn(Optional.of(mockBoek));
        given(exemplaarService.isBeschikbaar(mockBoek)).willReturn(true);
        given(uitleningRepository.existsByUser_IdAndIsIngeleverdFalseAndInleverDatumBefore(
                eq(userId), any(LocalDate.class))).willReturn(false);
        given(uitleningRepository.existsByUser_IdAndExemplaar_Boek_IdAndIsIngeleverdFalse(userId, bookId))
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

        when(boekRepository.findById(boekId)).thenReturn(Optional.of(boek));
        when(exemplaarService.isBeschikbaar(boek)).thenReturn(true);
        when(uitleningRepository.existsByUser_IdAndExemplaar_Boek_IdAndIsIngeleverdFalse(gebruikerId, boekId))
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

        Boek boek2 = new Boek("1984", "Orwell");
        boek2.setId(11L);

        when(userRepository.findById(leraarId)).thenReturn(Optional.of(leraar));
        when(boekRepository.findById(10L)).thenReturn(Optional.of(boek1));
        when(boekRepository.findById(11L)).thenReturn(Optional.of(boek2));
        when(exemplaarService.isBeschikbaar(boek1)).thenReturn(true);
        when(exemplaarService.isBeschikbaar(boek2)).thenReturn(true);
        when(exemplaarService.leenUit(boek1)).thenReturn(new Exemplaar(boek1, 1, ExemplaarStatus.UITGELEEND));
        when(exemplaarService.leenUit(boek2)).thenReturn(new Exemplaar(boek2, 1, ExemplaarStatus.UITGELEEND));

        // CRUCIAAL: Vertel de mock hoeveel dagen hij moet teruggeven!
        when(instellingService.getInlevertermijnDagen()).thenReturn(14);

        gebruikerService.bevestigBulkUitlening(leraarId, boekIds);

        verify(exemplaarService, times(1)).leenUit(boek1);
        verify(exemplaarService, times(1)).leenUit(boek2);
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

        when(userRepository.findById(leraarId)).thenReturn(Optional.of(leraar));
        when(boekRepository.findById(5L)).thenReturn(Optional.of(boekUitVerkocht));
        when(exemplaarService.isBeschikbaar(boekUitVerkocht)).thenReturn(false);

        assertThrows(OnvoldoendeVoorraadException.class, () -> {
            gebruikerService.bevestigBulkUitlening(leraarId, Arrays.asList(5L));
        });
    }
}