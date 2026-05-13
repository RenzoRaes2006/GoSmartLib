package ap.be.backend;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import ap.be.backend.dto.LeenStatusResponse;
import ap.be.backend.entity.Boek;
import ap.be.backend.entity.User;
import ap.be.backend.entity.Uitlening;
import ap.be.backend.repository.BoekRepository;
import ap.be.backend.repository.UserRepository;
import ap.be.backend.repository.LeeslijstMapRepository;
import ap.be.backend.repository.UitleningRepository;
import ap.be.backend.service.GebruikerService;
import ap.be.backend.service.InstellingService;

@ExtendWith(MockitoExtension.class)
public class UitleningTest {
    @Mock
    private UserRepository userRepository;
    @Mock
    private BoekRepository boekRepository;
    @Mock
    private UitleningRepository uitleningRepository;
    @Mock
    private LeeslijstMapRepository leeslijstMapRepository;
    @Mock
    private InstellingService instellingService;

    @InjectMocks
    private GebruikerService gebruikerService;

    private User testGebruiker;
    private Boek testBoek;

    @BeforeEach
    void setUp() {
        testGebruiker = new User();
        testGebruiker.setId(1L);

        testBoek = new Boek();
        testBoek.setId(100L);
        testBoek.setTitel("Spring Boot voor Beginners");
        testBoek.setAantalExemplaren(5);

        lenient().when(instellingService.getLeenLimiet()).thenReturn(3);
    }

    @Test
    void checkLeenStatus_MagLenen_AlsAllesKlopt() {
        // Arrange
        when(boekRepository.findById(100L)).thenReturn(Optional.of(testBoek));
        when(uitleningRepository.existsByUser_IdAndBoek_IdAndIsIngeleverdFalse(1L, 100L))
                .thenReturn(false);
        when(uitleningRepository.findByUser_IdAndIsIngeleverdFalse(1L)).thenReturn(new ArrayList<>());

        // Act
        LeenStatusResponse response = gebruikerService.checkLeenStatus(1L, 100L);

        // Assert
        assertTrue(response.isMagLenen());
        assertEquals("Beschikbaar om te reserveren", response.getReden());
    }

    @Test
    void checkLeenStatus_MagNietLenen_AlsVoorraadNulIs() {
        // Arrange
        testBoek.setAantalExemplaren(0);
        when(boekRepository.findById(100L)).thenReturn(Optional.of(testBoek));

        // Act
        LeenStatusResponse response = gebruikerService.checkLeenStatus(1L, 100L);

        // Assert
        assertFalse(response.isMagLenen());
        assertEquals("Boek is momenteel niet beschikbaar.", response.getReden());
    }

    @Test
    void checkLeenStatus_MagNietLenen_AlsMax3BoekenBereiktIs() {
        // Arrange
        when(boekRepository.findById(100L)).thenReturn(Optional.of(testBoek));

        List<Uitlening> actieveLeningen = List.of(new Uitlening(), new Uitlening(), new Uitlening());
        when(uitleningRepository.findByUser_IdAndIsIngeleverdFalse(1L)).thenReturn(actieveLeningen);

        // Act
        LeenStatusResponse response = gebruikerService.checkLeenStatus(1L, 100L);

        // Assert
        assertFalse(response.isMagLenen());
        assertEquals("Je hebt je limiet van 3 boeken bereikt (inclusief aanvragen).", response.getReden());
    }

    @Test
    void checkLeenStatus_MagNietLenen_AlsBoekAlGeleendOfAangevraagdIs() {
        // Arrange
        when(boekRepository.findById(100L)).thenReturn(Optional.of(testBoek));
        when(uitleningRepository.existsByUser_IdAndBoek_IdAndIsIngeleverdFalse(1L, 100L))
                .thenReturn(true);

        // Act
        LeenStatusResponse response = gebruikerService.checkLeenStatus(1L, 100L);

        // Assert
        assertFalse(response.isMagLenen());

        assertEquals("Je hebt dit boek al geleend of in aanvraag.", response.getReden());
    }

    @Test
    void checkLeenStatus_MagNietLenen_AlsGebruikerTeLateBoekenHeeft() {
        // Arrange
        when(boekRepository.findById(100L)).thenReturn(Optional.of(testBoek));
        when(uitleningRepository.existsByUser_IdAndBoek_IdAndIsIngeleverdFalse(1L, 100L))
                .thenReturn(false);
        when(uitleningRepository.existsByUser_IdAndIsIngeleverdFalseAndInleverDatumBefore(any(Long.class),
                any(LocalDate.class))).thenReturn(true);

        // Act
        LeenStatusResponse response = gebruikerService.checkLeenStatus(1L, 100L);

        // Assert
        assertFalse(response.isMagLenen());
        assertEquals("Je hebt nog boeken die te laat zijn. Lever deze eerst in!", response.getReden());
    }

    @Test
    void bevestigUitlening_Succes_VermindertVoorraadEnSlaatOp() {
        // Arrange
        when(boekRepository.findById(100L)).thenReturn(Optional.of(testBoek));
        when(userRepository.findById(1L)).thenReturn(Optional.of(testGebruiker));

        int startVoorraad = testBoek.getAantalExemplaren();

        // Act
        gebruikerService.bevestigUitlening(1L, 100L);

        // Assert
        assertEquals(startVoorraad - 1, testBoek.getAantalExemplaren(), "De voorraad moet met 1 verminderd zijn");
        verify(boekRepository, times(1)).save(testBoek);
        verify(uitleningRepository, times(1)).save(any(Uitlening.class));
    }
}
