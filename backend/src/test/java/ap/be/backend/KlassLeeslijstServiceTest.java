package ap.be.backend;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.util.Optional;
import java.util.HashSet;
import java.util.List;
import java.util.Collections;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import ap.be.backend.config.SecurityUtils;
import ap.be.backend.dto.KlassLeeslijstRequestDTO;
import ap.be.backend.dto.KlassLeeslijstResponseDTO;
import ap.be.backend.entity.Boek;
import ap.be.backend.entity.KlassLeeslijst;
import ap.be.backend.entity.User;
import ap.be.backend.exception.GebruikerNotFoundException;
import ap.be.backend.exception.KlassLeeslijstNotFoundException;
import ap.be.backend.repository.BoekRepository;
import ap.be.backend.repository.KlassLeeslijstRepository;
import ap.be.backend.repository.UserRepository;
import ap.be.backend.service.KlassLeeslijstService;

@ExtendWith(MockitoExtension.class)
public class KlassLeeslijstServiceTest {

    @Mock
    private KlassLeeslijstRepository leeslijstRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private BoekRepository boekRepository;

    @InjectMocks
    private KlassLeeslijstService leeslijstService;

    private User testLeerkracht;
    private User testLeerling;
    private KlassLeeslijst testLijst;
    private Boek testBoek;

    @BeforeEach
    void setUp() {
        testLeerkracht = new User();
        testLeerkracht.setId(1L);

        testLeerling = new User();
        testLeerling.setId(42L);
        testLeerling.setSmartschoolId("leerling42");
        testLeerling.setKlas("5INF1");

        testLijst = new KlassLeeslijst();
        testLijst.setId(10L);
        testLijst.setNaam("Testlijst");
        testLijst.setLeerkracht(testLeerkracht);
        testLijst.setBoeken(new HashSet<>());

        testBoek = new Boek();
        testBoek.setId(5L);
        testBoek.setTitel("Test Boek");
    }

    /**
     * Test of er een exceptie wordt gegooid wanneer noch klas noch leerling
     * worden meegegeven — een leeslijst moet altijd gekoppeld zijn.
     */
    @Test
    void givenGeenKoppeling_whenCreateLeeslijst_thenThrowsIllegalArgumentException() {
        KlassLeeslijstRequestDTO request = new KlassLeeslijstRequestDTO("Testlijst", null, null, null, null);

        assertThrows(IllegalArgumentException.class,
                () -> leeslijstService.createLeeslijst(request, 1L));
    }

    /**
     * Test of een leeslijst aan een klas gekoppeld wordt en zichtbaarheid wordt
     * gerespecteerd.
     */
    @Test
    void givenKlasKoppeling_whenCreateLeeslijst_thenZetKlasEnZichtbaar() {
        KlassLeeslijstRequestDTO request = new KlassLeeslijstRequestDTO(
                "Wiskunde lijst", "5INF1", null, "Voor het rekenen-hoofdstuk", true);
        when(userRepository.findDistinctKlassen()).thenReturn(List.of("5INF1"));
        when(userRepository.findById(1L)).thenReturn(Optional.of(testLeerkracht));
        when(leeslijstRepository.save(any(KlassLeeslijst.class))).thenAnswer(inv -> inv.getArgument(0));

        KlassLeeslijstResponseDTO result = leeslijstService.createLeeslijst(request, 1L);

        assertEquals("5INF1", result.klas());
        assertNull(result.leerlingId());
        assertEquals("Voor het rekenen-hoofdstuk", result.beschrijving());
        assertTrue(result.zichtbaar());
    }

    /**
     * Test of een leeslijst aan een leerling gekoppeld wordt.
     */
    @Test
    void givenLeerlingKoppeling_whenCreateLeeslijst_thenZetLeerling() {
        KlassLeeslijstRequestDTO request = new KlassLeeslijstRequestDTO(
                "Persoonlijk lijstje", null, 42L, null, false);
        when(userRepository.findById(1L)).thenReturn(Optional.of(testLeerkracht));
        when(userRepository.findById(42L)).thenReturn(Optional.of(testLeerling));
        when(leeslijstRepository.save(any(KlassLeeslijst.class))).thenAnswer(inv -> inv.getArgument(0));

        KlassLeeslijstResponseDTO result = leeslijstService.createLeeslijst(request, 1L);

        assertNull(result.klas());
        assertEquals(42L, result.leerlingId());
        assertEquals("leerling42", result.leerlingNaam());
    }

    /**
     * Test of er een exceptie wordt gegooid wanneer zowel klas als leerling
     * worden meegegeven (dubbele koppeling is niet toegestaan).
     */
    @Test
    void givenBeideKoppelingen_whenCreateLeeslijst_thenThrowsIllegalArgumentException() {
        KlassLeeslijstRequestDTO request = new KlassLeeslijstRequestDTO(
                "Foute lijst", "5INF1", 42L, null, false);

        assertThrows(IllegalArgumentException.class,
                () -> leeslijstService.createLeeslijst(request, 1L));
    }

    /**
     * Test of er een exceptie wordt gegooid wanneer er geprobeerd wordt
     * een leeslijst aan te maken voor een niet-bestaande leerkracht.
     */
    @Test
    void givenLeerkrachtNietGevonden_whenCreateLeeslijst_thenThrowsGebruikerNotFoundException() {
        KlassLeeslijstRequestDTO request = new KlassLeeslijstRequestDTO("Naam", "5INF1", null, null, null);
        when(userRepository.findDistinctKlassen()).thenReturn(List.of("5INF1"));
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(GebruikerNotFoundException.class,
                () -> leeslijstService.createLeeslijst(request, 1L));
    }

    /**
     * Test of er een exceptie wordt gegooid wanneer de opgegeven klas niet
     * bestaat in de database (server-side validatie).
     */
    @Test
    void givenOnbekendeKlas_whenCreateLeeslijst_thenThrowsIllegalArgumentException() {
        KlassLeeslijstRequestDTO request = new KlassLeeslijstRequestDTO("Naam", "ONBEKEND", null, null, null);
        when(userRepository.findDistinctKlassen()).thenReturn(List.of("5INF1", "5INF2"));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> leeslijstService.createLeeslijst(request, 1L));
        assertTrue(ex.getMessage().contains("ONBEKEND"));
    }

    /**
     * Test of de koppeling van een bestaande lijst gewisseld kan worden van
     * klas naar leerling.
     */
    @Test
    void givenBestaandeLijst_whenUpdateKoppelingNaarLeerling_thenZetLeerlingEnWistKlas() {
        testLijst.setKlas("5INF1");
        when(leeslijstRepository.findById(10L)).thenReturn(Optional.of(testLijst));
        when(userRepository.findById(42L)).thenReturn(Optional.of(testLeerling));
        when(leeslijstRepository.save(any(KlassLeeslijst.class))).thenAnswer(inv -> inv.getArgument(0));

        KlassLeeslijstResponseDTO result = leeslijstService.updateKoppeling(10L, null, 42L);

        assertNull(result.klas());
        assertEquals(42L, result.leerlingId());
    }

    /**
     * Test of de koppeling van een bestaande lijst gewisseld kan worden van
     * leerling naar klas, en dat de klas-validatie ook hier geldt.
     */
    @Test
    void givenOnbekendeKlas_whenUpdateKoppeling_thenThrowsIllegalArgumentException() {
        when(userRepository.findDistinctKlassen()).thenReturn(List.of("5INF1"));

        assertThrows(IllegalArgumentException.class,
                () -> leeslijstService.updateKoppeling(10L, "ONBEKEND", null));
    }

    /**
     * Test of een bestaand boek succesvol aan een bestaande leeslijst
     * wordt toegevoegd en de lijst wordt geüpdatet.
     */
    @Test
    void givenGeldigeIds_whenAddBoekToLeeslijst_thenUpdatesLijst() {
        when(leeslijstRepository.findById(10L)).thenReturn(Optional.of(testLijst));
        when(boekRepository.findById(5L)).thenReturn(Optional.of(testBoek));
        when(leeslijstRepository.save(any(KlassLeeslijst.class))).thenReturn(testLijst);

        leeslijstService.addBoekToLeeslijst(10L, 5L);

        verify(leeslijstRepository, times(1)).save(testLijst);
    }

    /**
     * Test of er een exceptie wordt gegooid wanneer een boek wordt
     * toegevoegd aan een leeslijst die niet bestaat in de database.
     */
    @Test
    void givenLijstNietGevonden_whenAddBoekToLeeslijst_thenThrowsKlassLeeslijstNotFoundException() {
        when(leeslijstRepository.findById(10L)).thenReturn(Optional.empty());

        assertThrows(KlassLeeslijstNotFoundException.class,
                () -> leeslijstService.addBoekToLeeslijst(10L, 5L));
    }

    /**
     * Test of een boek succesvol wordt verwijderd uit een bestaande leeslijst.
     */
    @Test
    void givenGeldigeIds_whenRemoveBoekFromLeeslijst_thenRemovesBoek() {
        testLijst.getBoeken().add(testBoek);
        when(leeslijstRepository.findById(10L)).thenReturn(Optional.of(testLijst));
        when(boekRepository.findById(5L)).thenReturn(Optional.of(testBoek));
        when(leeslijstRepository.save(any(KlassLeeslijst.class))).thenReturn(testLijst);

        leeslijstService.removeBoekFromLeeslijst(10L, 5L);

        verify(leeslijstRepository, times(1)).save(testLijst);
        assertTrue(testLijst.getBoeken().isEmpty());
    }

    /**
     * Test of de naam van een bestaande leeslijst correct kan worden gewijzigd.
     */
    @Test
    void givenGeldigId_whenUpdateLeeslijstNaam_thenUpdatesNaam() {
        when(leeslijstRepository.findById(10L)).thenReturn(Optional.of(testLijst));
        when(leeslijstRepository.save(any(KlassLeeslijst.class))).thenReturn(testLijst);

        leeslijstService.updateLeeslijstNaam(10L, "Nieuwe Naam");

        verify(leeslijstRepository, times(1)).save(testLijst);
        assertEquals("Nieuwe Naam", testLijst.getNaam());
    }

    /**
     * Test of de beschrijving van een bestaande leeslijst correct kan worden
     * gewijzigd.
     */
    @Test
    void givenGeldigId_whenUpdateBeschrijving_thenUpdatesBeschrijving() {
        when(leeslijstRepository.findById(10L)).thenReturn(Optional.of(testLijst));
        when(leeslijstRepository.save(any(KlassLeeslijst.class))).thenReturn(testLijst);

        leeslijstService.updateBeschrijving(10L, "Nieuwe uitleg");

        assertEquals("Nieuwe uitleg", testLijst.getBeschrijving());
        verify(leeslijstRepository, times(1)).save(testLijst);
    }

    /**
     * Test of de zichtbaarheid van een bestaande leeslijst correct kan worden
     * omgewisseld.
     */
    @Test
    void givenGeldigId_whenUpdateZichtbaar_thenUpdatesZichtbaar() {
        when(leeslijstRepository.findById(10L)).thenReturn(Optional.of(testLijst));
        when(leeslijstRepository.save(any(KlassLeeslijst.class))).thenReturn(testLijst);

        leeslijstService.updateZichtbaar(10L, true);

        assertTrue(testLijst.isZichtbaar());
        verify(leeslijstRepository, times(1)).save(testLijst);
    }

    /**
     * Test of een bestaande leeslijst correct wordt verwijderd uit de database.
     */
    @Test
    void givenGeldigId_whenDeleteLeeslijst_thenDeletesLijst() {
        when(leeslijstRepository.findById(10L)).thenReturn(Optional.of(testLijst));

        leeslijstService.deleteLeeslijst(10L);

        verify(leeslijstRepository, times(1)).delete(testLijst);
    }

    /**
     * Test of alle beschikbare klassen-leeslijsten worden opgehaald.
     */
    @Test
    void givenLeeslijsten_whenGetAllKlassenLeeslijsten_thenReturnsList() {
        when(leeslijstRepository.findAll()).thenReturn(Collections.singletonList(testLijst));

        List<KlassLeeslijstResponseDTO> result = leeslijstService.getAllKlassenLeeslijsten();

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Testlijst", result.get(0).naam());
    }

    /**
     * CR-acceptatie: een leerling van klas 5INF1 ziet zowel zichtbare
     * klas-leeslijsten van 5INF1 als persoonlijk toegewezen lijsten.
     */
    @Test
    void givenLeerlingInKlas_whenGetLeeslijstenVoorHuidigeLeerling_thenCombineertKlasEnPersoonlijk() {
        KlassLeeslijst klasLijst = new KlassLeeslijst();
        klasLijst.setId(20L);
        klasLijst.setNaam("Klas-lijst");
        klasLijst.setLeerkracht(testLeerkracht);
        klasLijst.setKlas("5INF1");
        klasLijst.setZichtbaar(true);
        klasLijst.setBoeken(new HashSet<>());

        KlassLeeslijst persoonlijkeLijst = new KlassLeeslijst();
        persoonlijkeLijst.setId(21L);
        persoonlijkeLijst.setNaam("Persoonlijke lijst");
        persoonlijkeLijst.setLeerkracht(testLeerkracht);
        persoonlijkeLijst.setLeerling(testLeerling);
        persoonlijkeLijst.setZichtbaar(true);
        persoonlijkeLijst.setBoeken(new HashSet<>());

        when(leeslijstRepository.findByKlasAndZichtbaarTrueAndGearchiveerdFalseOrderByAangemaaktOpDesc("5INF1"))
                .thenReturn(List.of(klasLijst));
        when(leeslijstRepository.findByLeerlingIdAndZichtbaarTrueAndGearchiveerdFalseOrderByAangemaaktOpDesc(42L))
                .thenReturn(List.of(persoonlijkeLijst));

        try (MockedStatic<SecurityUtils> securityMock = Mockito.mockStatic(SecurityUtils.class)) {
            securityMock.when(() -> SecurityUtils.getCurrentUser(userRepository))
                    .thenReturn(testLeerling);

            List<KlassLeeslijstResponseDTO> result = leeslijstService.getLeeslijstenVoorHuidigeLeerling();

            assertEquals(2, result.size());
            assertTrue(result.stream().anyMatch(l -> "5INF1".equals(l.klas())));
            assertTrue(result.stream().anyMatch(l -> l.leerlingId() != null && l.leerlingId() == 42L));
        }
    }

    /**
     * Test of een gearchiveerde leeslijst wordt teruggezet naar actief (gearchiveerd=false)
     * en gedwongen wordt naar concept-status (zichtbaar=false) zodat de leerkracht hem
     * eerst bewust moet publiceren.
     */
    @Test
    void givenGearchiveerdeLijst_whenDearchiveer_thenZetGearchiveerdFalseEnZichtbaarFalse() {
        testLijst.setGearchiveerd(true);
        testLijst.setZichtbaar(true);
        when(leeslijstRepository.findById(10L)).thenReturn(Optional.of(testLijst));
        when(leeslijstRepository.save(any(KlassLeeslijst.class))).thenAnswer(inv -> inv.getArgument(0));

        KlassLeeslijstResponseDTO result = leeslijstService.dearchiveer(10L);

        assertFalse(testLijst.isGearchiveerd());
        assertFalse(testLijst.isZichtbaar());
        assertFalse(result.gearchiveerd());
        assertFalse(result.zichtbaar());
        verify(leeslijstRepository, times(1)).save(testLijst);
    }

    /**
     * Test of dearchiveer een exceptie gooit wanneer de leeslijst niet bestaat.
     */
    @Test
    void givenLijstNietGevonden_whenDearchiveer_thenThrowsKlassLeeslijstNotFoundException() {
        when(leeslijstRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(KlassLeeslijstNotFoundException.class,
                () -> leeslijstService.dearchiveer(99L));
    }
}
