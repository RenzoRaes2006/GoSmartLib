package ap.be.backend;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import ap.be.backend.dto.BeoordelingDTO;
import ap.be.backend.entity.Beoordeling;
import ap.be.backend.entity.Boek;
import ap.be.backend.entity.User;
import ap.be.backend.exception.BoekNotFoundException;
import ap.be.backend.exception.GebruikerNotFoundException;
import ap.be.backend.repository.BeoordelingRepository;
import ap.be.backend.repository.BoekRepository;
import ap.be.backend.repository.UserRepository;
import ap.be.backend.repository.GerapporteerdeBeoordelingRepository;
import ap.be.backend.service.BeoordelingService;
import ap.be.backend.service.FilterService;

@ExtendWith(MockitoExtension.class)
public class BeoordelingServiceTest {

    @Mock
    private BeoordelingRepository beoordelingRepository;

    @Mock
    private BoekRepository boekRepository;

    @Mock
    private GerapporteerdeBeoordelingRepository gerapporteerdeBeoordelingRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private FilterService filterService;

    @InjectMocks
    private BeoordelingService beoordelingService;

    private Boek testBoek;
    private Beoordeling testBeoordeling;
    private User testGebruiker;
    private BeoordelingDTO testDto;
    private final Long BOEK_ID = 1L;
    private static final String SMARTSCHOOL_ID = "ss-test-1";

    @BeforeEach
    void setUp() {
        testBoek = new Boek();
        testBoek.setId(BOEK_ID);

        testGebruiker = new User();
        testGebruiker.setId(1L);
        testGebruiker.setSmartschoolId(SMARTSCHOOL_ID);

        testBeoordeling = new Beoordeling();
        testBeoordeling.setId(10L);
        testBeoordeling.setUser(testGebruiker);
        testBeoordeling.setBoek(testBoek);
        testBeoordeling.setWaardering(5);
        testBeoordeling.setInhoud("Geweldig boek!");
        testBeoordeling.setAnoniem(false);
        testBeoordeling.setSpoiler(false);

        testDto = new BeoordelingDTO(10L, "Test Gebruiker", BOEK_ID, 5, "Geweldig boek!", false, false, false);

        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(SMARTSCHOOL_ID, null, List.of()));
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void givenBoekBestaatNiet_whenGetBeoordelingenOpBoekId_thenThrowsBoekNotFoundException() {
        when(boekRepository.existsById(BOEK_ID)).thenReturn(false);

        BoekNotFoundException exception = assertThrows(BoekNotFoundException.class, () -> {
            beoordelingService.getBeoordelingenOpBoekId(BOEK_ID);
        });

        assertEquals("Boek met id " + BOEK_ID + " niet gevonden", exception.getMessage());
        verify(beoordelingRepository, never()).findByBoekId(BOEK_ID);
    }

    @Test
    void givenBoekBestaatZonderBeoordelingen_whenGetBeoordelingenOpBoekId_thenReturnsEmptyList() {
        when(boekRepository.existsById(BOEK_ID)).thenReturn(true);
        when(beoordelingRepository.findByBoekId(BOEK_ID)).thenReturn(Collections.emptyList());
        when(userRepository.findBySmartschoolId(SMARTSCHOOL_ID)).thenReturn(Optional.of(testGebruiker));

        List<BeoordelingDTO> result = beoordelingService.getBeoordelingenOpBoekId(BOEK_ID);

        assertTrue(result.isEmpty());
        verify(beoordelingRepository, times(1)).findByBoekId(BOEK_ID);
    }

    @Test
    void givenBoekBestaatMetBeoordelingen_whenGetBeoordelingenOpBoekId_thenReturnsBeoordelingDTOList() {
        when(boekRepository.existsById(BOEK_ID)).thenReturn(true);
        when(beoordelingRepository.findByBoekId(BOEK_ID)).thenReturn(List.of(testBeoordeling));
        when(userRepository.findBySmartschoolId(SMARTSCHOOL_ID)).thenReturn(Optional.of(testGebruiker));
        when(gerapporteerdeBeoordelingRepository.existsByBeoordelingIdAndUserMelder_Id(10L, 1L))
                .thenReturn(true);

        List<BeoordelingDTO> result = beoordelingService.getBeoordelingenOpBoekId(BOEK_ID);

        assertEquals(1, result.size());
        assertEquals("Geweldig boek!", result.get(0).inhoud());
        assertTrue(result.get(0).gerapporteerd());
        verify(beoordelingRepository, times(1)).findByBoekId(BOEK_ID);
    }

    @Test
    void givenBoekBestaatNiet_whenBeoordelingOpslaan_thenThrowsBoekNotFoundException() {
        when(filterService.bevatScheldwoorden(testDto.inhoud())).thenReturn(false);
        when(boekRepository.findById(testDto.boekId())).thenReturn(Optional.empty());

        BoekNotFoundException exception = assertThrows(BoekNotFoundException.class, () -> {
            beoordelingService.beoordelingOpslaan(testDto);
        });

        assertEquals("Kan beoordeling niet opslaan: Boek met id " + testDto.boekId() + " is niet gevonden.", exception.getMessage());
        verify(beoordelingRepository, never()).save(any(Beoordeling.class));
    }

    @Test
    void givenBoekBestaatMaarGebruikerNiet_whenBeoordelingOpslaan_thenThrowsGebruikerNotFoundException() {
        when(filterService.bevatScheldwoorden(testDto.inhoud())).thenReturn(false);
        when(boekRepository.findById(testDto.boekId())).thenReturn(Optional.of(testBoek));
        when(userRepository.findBySmartschoolId(SMARTSCHOOL_ID)).thenReturn(Optional.empty());

        GebruikerNotFoundException exception = assertThrows(GebruikerNotFoundException.class, () -> {
            beoordelingService.beoordelingOpslaan(testDto);
        });

        assertEquals("Geen user gevonden voor smartschoolId: " + SMARTSCHOOL_ID, exception.getMessage());
        verify(beoordelingRepository, never()).save(any(Beoordeling.class));
    }

    @Test
    void givenGeldigeDataEnBestaandBoekEnGebruiker_whenBeoordelingOpslaan_thenSavesBeoordeling() {
        when(filterService.bevatScheldwoorden(testDto.inhoud())).thenReturn(false);
        when(boekRepository.findById(testDto.boekId())).thenReturn(Optional.of(testBoek));
        when(userRepository.findBySmartschoolId(SMARTSCHOOL_ID)).thenReturn(Optional.of(testGebruiker));
        when(beoordelingRepository.findByBoekId(testBoek.getId())).thenReturn(List.of(testBeoordeling));

        beoordelingService.beoordelingOpslaan(testDto);

        verify(beoordelingRepository, times(1)).save(any(Beoordeling.class));
    }

    @Test
    void givenBestaandeBeoordeling_whenVerwijderBeoordeling_thenDeletesById() {
        Long beoordelingId = 99L;
        when(beoordelingRepository.existsById(beoordelingId)).thenReturn(true);

        beoordelingService.verwijderBeoordeling(beoordelingId);

        verify(beoordelingRepository, times(1)).deleteById(beoordelingId);
    }

    @Test
    void givenNietBestaandeBeoordeling_whenVerwijderBeoordeling_thenThrowsIllegalArgumentException() {
        Long beoordelingId = 100L;
        when(beoordelingRepository.existsById(beoordelingId)).thenReturn(false);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> beoordelingService.verwijderBeoordeling(beoordelingId));

        assertEquals("Beoordeling niet gevonden met id: " + beoordelingId, exception.getMessage());
        verify(beoordelingRepository, never()).deleteById(beoordelingId);
    }
}
