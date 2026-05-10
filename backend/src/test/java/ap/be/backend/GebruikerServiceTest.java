package ap.be.backend;

import ap.be.backend.dto.LeenStatusResponse;
import ap.be.backend.entity.Boek;
import ap.be.backend.entity.Gebruiker;
import ap.be.backend.entity.LeeslijstMap;
import ap.be.backend.entity.Uitlening;
import ap.be.backend.exception.InvalidImportDataException;
import ap.be.backend.exception.LeenVoorwaardeException;
import ap.be.backend.exception.OnvoldoendeVoorraadException;
import ap.be.backend.exception.ResourceNotFoundException;
import ap.be.backend.repository.BoekRepository;
import ap.be.backend.repository.GebruikerRepository;
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
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class GebruikerServiceTest {

    @Mock
    private GebruikerRepository gebruikerRepository;
    @Mock
    private LeeslijstMapRepository leeslijstMapRepository;

    @Mock
    private BoekRepository boekRepository;
    @Mock
    private InstellingService instellingService;

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
        given(uitleningRepository.findByGebruiker_GebruikerIDAndIsIngeleverdFalse(userId))
                .willReturn(Collections.emptyList());

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

        // then: status is GEWEIGERD, it is marked as returned (so it doesn't count
        // towards the limit)
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

    @Test
    void givenUser_whenGetLeengeschiedenis_thenReturnOrderedList() {
        // given: a user with a history of loans
        Long userId = 1L;
        List<Uitlening> expectedHistory = Arrays.asList(testUitlening);

        // mock the specific repository call we created for the history
        given(uitleningRepository.findByGebruiker_GebruikerIDOrderByUitleenDatumDesc(userId))
                .willReturn(expectedHistory);

        // when: we fetch the history
        List<Uitlening> actualHistory = gebruikerService.getLeengeschiedenisVanGebruiker(userId);

        // then: it returns the correct list and calls the correct repository method
        assertEquals(1, actualHistory.size());
        then(uitleningRepository).should(times(1)).findByGebruiker_GebruikerIDOrderByUitleenDatumDesc(userId);
    }

    // --- NIEUWE TESTS VOOR VERLENGEN (FEATURE 55) ---
    @Test
    void verlengUitlening_Succesvol_Telt14DagenOpEnZetVerlengdOpTrue() {
        // Arrange
        testUitlening.setStatus("GOEDGEKEURD");

        // Stel een uitlening in met een geldige inleverdatum over één maand.
        testUitlening.setInleverDatum(LocalDate.now().plusMonths(1));
        LocalDate origineleDatum = testUitlening.getInleverDatum();

        when(uitleningRepository.findById(100L)).thenReturn(Optional.of(testUitlening));

        // Act
        gebruikerService.verlengUitlening(100L);

        // Assert
        assertTrue(testUitlening.isVerlengd());
        assertEquals(origineleDatum.plusDays(14), testUitlening.getInleverDatum());
        verify(uitleningRepository, times(1)).save(testUitlening);
    }

    @Test
    void verlengUitlening_AlIngeleverd_GooitException() {
        // Arrange
        testUitlening.setIngeleverd(true);
        testUitlening.setStatus("GOEDGEKEURD");
        when(uitleningRepository.findById(100L)).thenReturn(Optional.of(testUitlening));

        // Act & Then
        ap.be.backend.exception.LeenVoorwaardeException exception = assertThrows(
                ap.be.backend.exception.LeenVoorwaardeException.class, () -> {
                    gebruikerService.verlengUitlening(100L);
                });
        assertEquals("Dit boek is al ingeleverd.", exception.getMessage());
    }

    @Test
    void verlengUitlening_AlVerlengd_GooitException() {
        // Arrange
        testUitlening.setVerlengd(true);
        testUitlening.setStatus("GOEDGEKEURD");
        when(uitleningRepository.findById(100L)).thenReturn(Optional.of(testUitlening));

        // Act & Then
        ap.be.backend.exception.LeenVoorwaardeException exception = assertThrows(
                ap.be.backend.exception.LeenVoorwaardeException.class, () -> {
                    gebruikerService.verlengUitlening(100L);
                });
        assertEquals("Je hebt dit boek al een keer verlengd.", exception.getMessage());
    }

    @Test
    void verlengUitlening_NogInAanvraag_GooitException() {
        // Arrange
        testUitlening.setStatus("AANGEVRAAGD");
        when(uitleningRepository.findById(100L)).thenReturn(Optional.of(testUitlening));

        // Act & Then
        ap.be.backend.exception.LeenVoorwaardeException exception = assertThrows(
                ap.be.backend.exception.LeenVoorwaardeException.class, () -> {
                    gebruikerService.verlengUitlening(100L);
                });
        assertEquals("Je kunt een boek dat nog in aanvraag is niet verlengen.", exception.getMessage());
    }

    @Test
    void givenOverdueBooks_whenCheckLeenStatus_thenReturnFalseAndErrorMessage() {
        // ARRANGE (Setup)
        Long userId = 1L;
        Long bookId = 2L;

        Boek mockBoek = new Boek();
        mockBoek.setAantalExemplaren(5); // Zorg dat het boek wel op voorraad is, anders faalt de test te vroeg

        // Vertel de neppe database dat het boek bestaat
        given(boekRepository.findById(bookId)).willReturn(Optional.of(mockBoek));

        // Vertel de neppe database: "Ja, deze specifieke gebruiker heeft een boek dat
        // te laat is!"
        given(uitleningRepository.existsByGebruiker_GebruikerIDAndIsIngeleverdFalseAndInleverDatumBefore(
                eq(userId), any(LocalDate.class))).willReturn(true);

        // ACT (Voer de methode uit)
        LeenStatusResponse response = gebruikerService.checkLeenStatus(userId, bookId);

        // ASSERT (Controleer het resultaat)
        assertFalse(response.isMagLenen(), "De gebruiker mag niet lenen omdat hij een te laat boek heeft.");
        assertEquals("Je hebt nog boeken die te laat zijn. Lever deze eerst in!", response.getReden());

        // Verifieer dat de juiste repository methode ook echt is aangeroepen
        then(uitleningRepository).should().existsByGebruiker_GebruikerIDAndIsIngeleverdFalseAndInleverDatumBefore(
                eq(userId), any(LocalDate.class));
    }

    @Test
    void givenNoOverdueBooks_whenCheckLeenStatus_thenReturnTrue() {
        // ARRANGE (Setup)
        Long userId = 1L;
        Long bookId = 2L;

        Boek mockBoek = new Boek();
        mockBoek.setAantalExemplaren(5);

        given(boekRepository.findById(bookId)).willReturn(Optional.of(mockBoek));

        // Nu zeggen we: "Nee, deze gebruiker heeft KEEN te late boeken"
        given(uitleningRepository.existsByGebruiker_GebruikerIDAndIsIngeleverdFalseAndInleverDatumBefore(
                eq(userId), any(LocalDate.class))).willReturn(false);

        // We moeten ook zorgen dat hij het boek nog niet heeft en onder de limiet van 3
        // zit
        given(uitleningRepository.existsByGebruiker_GebruikerIDAndBoek_IdAndIsIngeleverdFalse(userId, bookId))
                .willReturn(false);
        given(uitleningRepository.findByGebruiker_GebruikerIDAndIsIngeleverdFalse(userId)).willReturn(List.of()); // Lege
                                                                                                                  // lijst
                                                                                                                  // (0
                                                                                                                  // actieve
                                                                                                                  // leningen)

        // ACT
        LeenStatusResponse response = gebruikerService.checkLeenStatus(userId, bookId);

        // ASSERT
        assertTrue(response.isMagLenen(), "De gebruiker moet mogen lenen.");
        assertEquals("Beschikbaar om te reserveren", response.getReden());
    }

    @Test
    void givenLimietIsDrieEnGebruikerHeeftDrieLeningen_whenCheckLeenStatus_thenReturnsFalse() {
        // Arrange
        Long gebruikerId = 1L;
        Long boekId = 100L;

        Boek boek = new Boek();
        boek.setId(boekId);
        boek.setAantalExemplaren(5); // Boek is op voorraad

        when(boekRepository.findById(boekId)).thenReturn(Optional.of(boek));
        // Simuleer dat de gebruiker DIT specifieke boek nog niet heeft, en geen boeken
        // te laat heeft
        when(uitleningRepository.existsByGebruiker_GebruikerIDAndBoek_IdAndIsIngeleverdFalse(gebruikerId, boekId))
                .thenReturn(false);
        when(uitleningRepository.existsByGebruiker_GebruikerIDAndIsIngeleverdFalseAndInleverDatumBefore(eq(gebruikerId),
                any())).thenReturn(false);

        // Hier stellen we onze nieuwe feature in: de limiet is 3
        when(instellingService.getLeenLimiet()).thenReturn(3);

        // Simuleer dat de gebruiker al 3 andere boeken heeft geleend
        List<Uitlening> actieveLeningen = Arrays.asList(new Uitlening(), new Uitlening(), new Uitlening());
        when(uitleningRepository.findByGebruiker_GebruikerIDAndIsIngeleverdFalse(gebruikerId))
                .thenReturn(actieveLeningen);

        // Act
        LeenStatusResponse response = gebruikerService.checkLeenStatus(gebruikerId, boekId);

        // Assert
        assertFalse(response.isMagLenen(), "De gebruiker mag niet lenen omdat de limiet is bereikt.");
        assertTrue(response.getReden().contains("limiet van 3 boeken bereikt"));
    }

    @Test
    void givenLimietIsNulEnGebruikerHeeftTienLeningen_whenCheckLeenStatus_thenReturnsTrueEnSlaatQueryOver() {
        // Arrange
        Long gebruikerId = 1L;
        Long boekId = 100L;

        Boek boek = new Boek();
        boek.setId(boekId);
        boek.setAantalExemplaren(5); // Boek is op voorraad

        when(boekRepository.findById(boekId)).thenReturn(Optional.of(boek));
        when(uitleningRepository.existsByGebruiker_GebruikerIDAndBoek_IdAndIsIngeleverdFalse(gebruikerId, boekId))
                .thenReturn(false);
        when(uitleningRepository.existsByGebruiker_GebruikerIDAndIsIngeleverdFalseAndInleverDatumBefore(eq(gebruikerId),
                any())).thenReturn(false);

        // De admin heeft onbeperkt lenen (0) ingesteld
        when(instellingService.getLeenLimiet()).thenReturn(0);

        // Act
        LeenStatusResponse response = gebruikerService.checkLeenStatus(gebruikerId, boekId);

        // Assert
        assertTrue(response.isMagLenen(), "De gebruiker mag lenen want de limiet is onbeperkt (0).");

        // CLEAN CODE CHECK: Omdat de limiet 0 is, hoeft de applicatie niet de database
        // in
        // om alle actieve leningen te tellen. We verifiëren hier of we die onnodige
        // belasting daadwerkelijk overslaan!
        verify(uitleningRepository, never()).findByGebruiker_GebruikerIDAndIsIngeleverdFalse(gebruikerId);
    }

    @Test
    void givenGeldigeUitlening_whenVerlengUitlening_thenStatusWordtVerlengingAangevraagd() {
        // Arrange
        Long uitleningId = 1L;
        Uitlening uitlening = new Uitlening();
        uitlening.setId(uitleningId);
        uitlening.setIngeleverd(false);
        uitlening.setVerlengd(false);
        uitlening.setStatus("GOEDGEKEURD"); // Een normaal uitgeleend boek

        when(uitleningRepository.findById(uitleningId)).thenReturn(Optional.of(uitlening));

        // Act
        gebruikerService.verlengUitlening(uitleningId);

        // Assert
        // Controleer of de status is gewijzigd naar aanvraag, zonder dat de datum al
        // wordt aangepast
        assertEquals("VERLENGING_AANGEVRAAGD", uitlening.getStatus(),
                "De status moet veranderen naar VERLENGING_AANGEVRAAGD");
        verify(uitleningRepository).save(uitlening);
    }

    @Test
    void givenAlVerlengdeUitlening_whenVerlengUitlening_thenThrowsLeenVoorwaardeException() {
        // Arrange
        Long uitleningId = 1L;
        Uitlening uitlening = new Uitlening();
        uitlening.setId(uitleningId);
        uitlening.setVerlengd(true); // Boek is al eens verlengd

        when(uitleningRepository.findById(uitleningId)).thenReturn(Optional.of(uitlening));

        // Act & Then
        LeenVoorwaardeException exception = assertThrows(LeenVoorwaardeException.class, () -> {
            gebruikerService.verlengUitlening(uitleningId);
        });

        assertEquals("Je hebt dit boek al een keer verlengd.", exception.getMessage());
        verify(uitleningRepository, never()).save(any(Uitlening.class));
    }

    @Test
    void givenNieuweAanvraag_whenVerlengUitlening_thenThrowsLeenVoorwaardeException() {
        // Arrange
        Long uitleningId = 1L;
        Uitlening uitlening = new Uitlening();
        uitlening.setId(uitleningId);
        uitlening.setStatus("AANGEVRAAGD"); // Boek is nog niet eens uitgeleend

        when(uitleningRepository.findById(uitleningId)).thenReturn(Optional.of(uitlening));

        // Act & Then
        LeenVoorwaardeException exception = assertThrows(LeenVoorwaardeException.class, () -> {
            gebruikerService.verlengUitlening(uitleningId);
        });

        assertEquals("Je kunt een boek dat nog in aanvraag is niet verlengen.", exception.getMessage());
        verify(uitleningRepository, never()).save(any(Uitlening.class));
    }

    @Test
    void givenValidData_whenBevestigBulkUitlening_thenSavesUitleningenAndUpdateStock() {
        // Arrange
        Long leraarId = 1L;
        List<Long> boekIds = Arrays.asList(10L, 11L);

        Gebruiker leraar = new Gebruiker();
        leraar.setId(leraarId);

        Boek boek1 = new Boek("De Hobbit", "Tolkien");
        boek1.setId(10L);
        boek1.setAantalExemplaren(3);

        Boek boek2 = new Boek("1984", "Orwell");
        boek2.setId(11L);
        boek2.setAantalExemplaren(1);

        when(gebruikerRepository.findById(leraarId)).thenReturn(Optional.of(leraar));
        when(boekRepository.findById(10L)).thenReturn(Optional.of(boek1));
        when(boekRepository.findById(11L)).thenReturn(Optional.of(boek2));

        // Act
        gebruikerService.bevestigBulkUitlening(leraarId, boekIds);

        // Assert
        assertEquals(2, boek1.getAantalExemplaren(), "Voorraad boek 1 moet met 1 verminderd zijn");
        assertEquals(0, boek2.getAantalExemplaren(), "Voorraad boek 2 moet met 1 verminderd zijn");

        verify(boekRepository, times(2)).save(any(Boek.class));
        verify(uitleningRepository, times(2)).save(any(Uitlening.class));
    }

    @Test
    void givenEmptyList_whenBevestigBulkUitlening_thenThrowsInvalidImportDataException() {
        // Act & Assert
        assertThrows(InvalidImportDataException.class, () -> {
            gebruikerService.bevestigBulkUitlening(1L, Collections.emptyList());
        });

        verify(uitleningRepository, never()).save(any());
    }

    @Test
    void givenNonExistingTeacher_whenBevestigBulkUitlening_thenThrowsResourceNotFoundException() {
        // Arrange
        when(gebruikerRepository.findById(99L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> {
            gebruikerService.bevestigBulkUitlening(99L, Arrays.asList(1L));
        });
    }

    @Test
    void givenOutOfStockBook_whenBevestigBulkUitlening_thenThrowsOnvoldoendeVoorraadException() {
        // Arrange
        Long leraarId = 1L;
        Gebruiker leraar = new Gebruiker();
        leraar.setId(leraarId);

        Boek boekUitVerkocht = new Boek("Clean Code", "Robert C. Martin");
        boekUitVerkocht.setId(5L);
        boekUitVerkocht.setAantalExemplaren(0); // OUT OF STOCK

        when(gebruikerRepository.findById(leraarId)).thenReturn(Optional.of(leraar));
        when(boekRepository.findById(5L)).thenReturn(Optional.of(boekUitVerkocht));

        // Act & Assert
        assertThrows(OnvoldoendeVoorraadException.class, () -> {
            gebruikerService.bevestigBulkUitlening(leraarId, Arrays.asList(5L));
        });
    }
}