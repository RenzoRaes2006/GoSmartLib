package ap.be.backend;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import ap.be.backend.entity.Beoordeling;
import ap.be.backend.entity.Gebruiker;
import ap.be.backend.entity.GerapporteerdeBeoordeling;
import ap.be.backend.exception.BeoordelingNotFoundException;
import ap.be.backend.exception.DuplicateReportException;
import ap.be.backend.exception.GebruikerNotFoundException;
import ap.be.backend.repository.BeoordelingRepository;
import ap.be.backend.repository.GebruikerRepository;
import ap.be.backend.repository.GerapporteerdeBeoordelingRepository;
import ap.be.backend.service.GerapporteerdeBeoordelingService;

@ExtendWith(MockitoExtension.class)
public class GerapporteerdeBeoordelingServiceTest {

    @Mock
    private GerapporteerdeBeoordelingRepository gerapporteerdeBeoordelingRepository;

    @Mock
    private BeoordelingRepository beoordelingRepository;

    @Mock
    private GebruikerRepository gebruikerRepository;

    @InjectMocks
    private GerapporteerdeBeoordelingService gerapporteerdeBeoordelingService;

    // Testdata variabelen
    private final Long BEOORDELING_ID = 10L;
    private final Long GEBRUIKER_ID = 1L;
    private final String REDEN = "Ongepast taalgebruik";
    
    private Beoordeling testBeoordeling;
    private Gebruiker testGebruiker;

    @BeforeEach
    void setUp() {
        testBeoordeling = new Beoordeling();
        testBeoordeling.setId(BEOORDELING_ID);

        testGebruiker = new Gebruiker();
        // Zorg dat je hier de juiste setter gebruikt voor jouw Gebruiker entiteit
        // Bijv: testGebruiker.setId(GEBRUIKER_ID); 
    }

    // =========================================================================
    // TESTS VOOR: rapporteerBeoordeling(Long, Long, String)
    // =========================================================================

    @Test
    void givenRapportBestaatAl_whenRapporteerBeoordeling_thenThrowsDuplicateReportException() {
        // GIVEN
        when(gerapporteerdeBeoordelingRepository.existsByBeoordelingIdAndGebruiker_GebruikerID(BEOORDELING_ID, GEBRUIKER_ID))
                .thenReturn(true);

        // WHEN & THEN
        DuplicateReportException exception = assertThrows(DuplicateReportException.class, () -> {
            gerapporteerdeBeoordelingService.rapporteerBeoordeling(BEOORDELING_ID, GEBRUIKER_ID, REDEN);
        });

        assertEquals("Je hebt deze beoordeling al gerapporteerd.", exception.getMessage());
        
        // Verifieer dat de rest van de code NOOIT wordt uitgevoerd
        verify(beoordelingRepository, never()).findById(any());
        verify(gebruikerRepository, never()).findById(any());
        verify(gerapporteerdeBeoordelingRepository, never()).save(any());
    }

    @Test
    void givenBeoordelingBestaatNiet_whenRapporteerBeoordeling_thenThrowsBeoordelingNotFoundException() {
        // GIVEN
        when(gerapporteerdeBeoordelingRepository.existsByBeoordelingIdAndGebruiker_GebruikerID(BEOORDELING_ID, GEBRUIKER_ID))
                .thenReturn(false);
        when(beoordelingRepository.findById(BEOORDELING_ID)).thenReturn(Optional.empty());

        // WHEN & THEN
        BeoordelingNotFoundException exception = assertThrows(BeoordelingNotFoundException.class, () -> {
            gerapporteerdeBeoordelingService.rapporteerBeoordeling(BEOORDELING_ID, GEBRUIKER_ID, REDEN);
        });

        assertEquals("Beoordeling niet gevonden met id: " + BEOORDELING_ID, exception.getMessage());
        
        // Verifieer dat we stoppen voordat de gebruiker wordt gezocht of iets wordt opgeslagen
        verify(gebruikerRepository, never()).findById(any());
        verify(gerapporteerdeBeoordelingRepository, never()).save(any());
    }

    @Test
    void givenGebruikerBestaatNiet_whenRapporteerBeoordeling_thenThrowsGebruikerNotFoundException() {
        // GIVEN
        when(gerapporteerdeBeoordelingRepository.existsByBeoordelingIdAndGebruiker_GebruikerID(BEOORDELING_ID, GEBRUIKER_ID))
                .thenReturn(false);
        when(beoordelingRepository.findById(BEOORDELING_ID)).thenReturn(Optional.of(testBeoordeling));
        when(gebruikerRepository.findById(GEBRUIKER_ID)).thenReturn(Optional.empty());

        // WHEN & THEN
        GebruikerNotFoundException exception = assertThrows(GebruikerNotFoundException.class, () -> {
            gerapporteerdeBeoordelingService.rapporteerBeoordeling(BEOORDELING_ID, GEBRUIKER_ID, REDEN);
        });

        assertEquals("Gebruiker niet gevonden met id: " + GEBRUIKER_ID, exception.getMessage());
        
        // Verifieer dat we stoppen voor het opslaan
        verify(gerapporteerdeBeoordelingRepository, never()).save(any());
    }

    @Test
    void givenGeldigeData_whenRapporteerBeoordeling_thenSavesGerapporteerdeBeoordeling() {
        // GIVEN
        when(gerapporteerdeBeoordelingRepository.existsByBeoordelingIdAndGebruiker_GebruikerID(BEOORDELING_ID, GEBRUIKER_ID))
                .thenReturn(false);
        when(beoordelingRepository.findById(BEOORDELING_ID)).thenReturn(Optional.of(testBeoordeling));
        when(gebruikerRepository.findById(GEBRUIKER_ID)).thenReturn(Optional.of(testGebruiker));

        // WHEN
        gerapporteerdeBeoordelingService.rapporteerBeoordeling(BEOORDELING_ID, GEBRUIKER_ID, REDEN);

        // THEN
        // Verifieer dat alle stappen zijn doorlopen en de save methode exact 1 keer is aangeroepen
        verify(gerapporteerdeBeoordelingRepository, times(1)).save(any(GerapporteerdeBeoordeling.class));
    }

    @Test
    void givenRedenIsNull_whenRapporteerBeoordeling_thenThrowsIllegalArgumentException() {
        // WHEN & THEN
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            gerapporteerdeBeoordelingService.rapporteerBeoordeling(BEOORDELING_ID, GEBRUIKER_ID, null);
        });

        assertEquals("Reden mag niet leeg zijn.", exception.getMessage());
        
        // Verifieer dat de rest van de code NOOIT wordt uitgevoerd
        verify(gerapporteerdeBeoordelingRepository, never()).existsByBeoordelingIdAndGebruiker_GebruikerID(any(), any());
        verify(gerapporteerdeBeoordelingRepository, never()).save(any());
    }

    @Test
    void givenRedenIsEmpty_whenRapporteerBeoordeling_thenThrowsIllegalArgumentException() {
        // WHEN & THEN
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            gerapporteerdeBeoordelingService.rapporteerBeoordeling(BEOORDELING_ID, GEBRUIKER_ID, "");
        });

        assertEquals("Reden mag niet leeg zijn.", exception.getMessage());
        
        verify(gerapporteerdeBeoordelingRepository, never()).existsByBeoordelingIdAndGebruiker_GebruikerID(any(), any());
        verify(gerapporteerdeBeoordelingRepository, never()).save(any());
    }

    @Test
    void givenRedenIsOnlySpaces_whenRapporteerBeoordeling_thenThrowsIllegalArgumentException() {
        // WHEN & THEN
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            gerapporteerdeBeoordelingService.rapporteerBeoordeling(BEOORDELING_ID, GEBRUIKER_ID, "   ");
        });

        assertEquals("Reden mag niet leeg zijn.", exception.getMessage());
        
        verify(gerapporteerdeBeoordelingRepository, never()).existsByBeoordelingIdAndGebruiker_GebruikerID(any(), any());
        verify(gerapporteerdeBeoordelingRepository, never()).save(any());
    }
}