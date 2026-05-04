package ap.be.backend;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.util.Optional;
import java.util.HashSet;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import ap.be.backend.dto.KlassLeeslijstResponseDTO;
import ap.be.backend.entity.Boek;
import ap.be.backend.entity.KlassLeeslijst;
import ap.be.backend.entity.Gebruiker;
import ap.be.backend.exception.GebruikerNotFoundException;
import ap.be.backend.exception.KlassLeeslijstNotFoundException;
import ap.be.backend.repository.BoekRepository;
import ap.be.backend.repository.KlassLeeslijstRepository;
import ap.be.backend.repository.GebruikerRepository;
import ap.be.backend.service.KlassLeeslijstService;

@ExtendWith(MockitoExtension.class)
public class KlassLeeslijstServiceTest {

    @Mock
    private KlassLeeslijstRepository leeslijstRepository;
    @Mock
    private GebruikerRepository gebruikerRepository;
    @Mock
    private BoekRepository boekRepository;

    @InjectMocks
    private KlassLeeslijstService leeslijstService;

    private Gebruiker testLeerkracht;
    private KlassLeeslijst testLijst;
    private Boek testBoek;

    @BeforeEach
    void setUp() {
        testLeerkracht = new Gebruiker();
        testLeerkracht.setGebruikerID(1L);

        testLijst = new KlassLeeslijst();
        testLijst.setId(10L);
        testLijst.setNaam("Testlijst");
        testLijst.setLeerkracht(testLeerkracht);
        testLijst.setBoeken(new HashSet<>());

        testBoek = new Boek();
        testBoek.setId(5L);
        testBoek.setTitel("Test Boek");
    }

    @Test
    void givenGeldigeData_whenCreateLeeslijst_thenSavesAndReturnsDTO() {
        when(gebruikerRepository.findById(1L)).thenReturn(Optional.of(testLeerkracht));
        when(leeslijstRepository.save(any(KlassLeeslijst.class))).thenReturn(testLijst);

        KlassLeeslijstResponseDTO result = leeslijstService.createLeeslijst("Testlijst", 1L);

        assertNotNull(result);
        assertEquals("Testlijst", result.naam());
        verify(leeslijstRepository, times(1)).save(any(KlassLeeslijst.class));
    }

    @Test
    void givenLeerkrachtNietGevonden_whenCreateLeeslijst_thenThrowsGebruikerNotFoundException() {
        when(gebruikerRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(GebruikerNotFoundException.class, () -> {
            leeslijstService.createLeeslijst("Naam", 1L);
        });
    }

    @Test
    void givenGeldigeIds_whenAddBoekToLeeslijst_thenUpdatesLijst() {
        when(leeslijstRepository.findById(10L)).thenReturn(Optional.of(testLijst));
        when(boekRepository.findById(5L)).thenReturn(Optional.of(testBoek));
        when(leeslijstRepository.save(any(KlassLeeslijst.class))).thenReturn(testLijst);

        KlassLeeslijstResponseDTO result = leeslijstService.addBoekToLeeslijst(10L, 5L);

        verify(leeslijstRepository, times(1)).save(testLijst);
    }

    @Test
    void givenLijstNietGevonden_whenAddBoekToLeeslijst_thenThrowsKlassLeeslijstNotFoundException() {
        when(leeslijstRepository.findById(10L)).thenReturn(Optional.empty());

        assertThrows(KlassLeeslijstNotFoundException.class, () -> {
            leeslijstService.addBoekToLeeslijst(10L, 5L);
        });
    }

    @Test
    void givenGeldigeIds_whenRemoveBoekFromLeeslijst_thenRemovesBoek() {
        testLijst.getBoeken().add(testBoek);
        when(leeslijstRepository.findById(10L)).thenReturn(Optional.of(testLijst));
        when(boekRepository.findById(5L)).thenReturn(Optional.of(testBoek));
        when(leeslijstRepository.save(any(KlassLeeslijst.class))).thenReturn(testLijst);

        KlassLeeslijstResponseDTO result = leeslijstService.removeBoekFromLeeslijst(10L, 5L);

        verify(leeslijstRepository, times(1)).save(testLijst);
        assertTrue(testLijst.getBoeken().isEmpty()); // Verwijdert de koppeling tussen de leeslijst en het boek[cite:
                                                     // 10]
    }

    @Test
    void givenGeldigId_whenUpdateLeeslijstNaam_thenUpdatesNaam() {
        when(leeslijstRepository.findById(10L)).thenReturn(Optional.of(testLijst));
        when(leeslijstRepository.save(any(KlassLeeslijst.class))).thenReturn(testLijst);

        KlassLeeslijstResponseDTO result = leeslijstService.updateLeeslijstNaam(10L, "Nieuwe Naam");

        verify(leeslijstRepository, times(1)).save(testLijst);
        assertEquals("Nieuwe Naam", testLijst.getNaam());
    }

    @Test
    void givenGeldigId_whenDeleteLeeslijst_thenDeletesLijst() {
        when(leeslijstRepository.findById(10L)).thenReturn(Optional.of(testLijst));

        leeslijstService.deleteLeeslijst(10L);

        verify(leeslijstRepository, times(1)).delete(testLijst); // Controleert of leeslijstRepository.delete wordt
                                                                 // aangeroepen[cite: 10]
    }
}