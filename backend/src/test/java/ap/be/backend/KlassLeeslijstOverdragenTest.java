package ap.be.backend;

import ap.be.backend.entity.KlassLeeslijst;
import ap.be.backend.entity.Rol;
import ap.be.backend.entity.School;
import ap.be.backend.entity.User;
import ap.be.backend.exception.GebruikerNotFoundException;
import ap.be.backend.exception.KlassLeeslijstNotFoundException;
import ap.be.backend.repository.BoekRepository;
import ap.be.backend.repository.KlassLeeslijstRepository;
import ap.be.backend.repository.UserRepository;
import ap.be.backend.service.KlassLeeslijstService;
import ap.be.backend.service.OneRosterService;
import ap.be.backend.mappers.KlassLeeslijstMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

/**
 * Unit-tests voor {@link KlassLeeslijstService#overdragen}. Valideert dat alle
 * randgevallen (zelfde leerkracht, andere school, niet-actief, verkeerde rol)
 * een nette {@code IllegalArgumentException} opleveren — de
 * {@code GlobalExceptionHandler} vertaalt die naar HTTP 400.
 */
@ExtendWith(MockitoExtension.class)
class KlassLeeslijstOverdragenTest {

    @Mock private KlassLeeslijstRepository leeslijstRepository;
    @Mock private UserRepository userRepository;
    @Mock private BoekRepository boekRepository;
    @Mock private OneRosterService oneRosterService;

    @InjectMocks private KlassLeeslijstService service;

    private School school1;
    private School school2;
    private User huidigeLeerkracht;
    private User andereLeerkracht;
    private KlassLeeslijst lijst;

    @BeforeEach
    void setUp() {
        // De mapper gebruikt een statische OneRoster-afhankelijkheid voor namen;
        // we initialiseren 'm hier zodat toDTO niet NPE't tijdens de save-path.
        new KlassLeeslijstMapper(oneRosterService);

        school1 = new School();
        school1.setId(1L);
        school2 = new School();
        school2.setId(2L);

        huidigeLeerkracht = new User("lk-huidig", null, Rol.LEERKRACHT);
        huidigeLeerkracht.setId(10L);
        huidigeLeerkracht.setSchool(school1);
        huidigeLeerkracht.setActief(true);

        andereLeerkracht = new User("lk-nieuw", null, Rol.LEERKRACHT);
        andereLeerkracht.setId(20L);
        andereLeerkracht.setSchool(school1);
        andereLeerkracht.setActief(true);

        lijst = new KlassLeeslijst();
        lijst.setId(100L);
        lijst.setNaam("Boekenlijst 1");
        lijst.setLeerkracht(huidigeLeerkracht);

        lenient().when(leeslijstRepository.findById(100L)).thenReturn(Optional.of(lijst));
        lenient().when(leeslijstRepository.save(lijst)).thenReturn(lijst);
    }

    @Test
    void givenGeldigeOverdracht_whenOverdragen_thenLeerkrachtGewisseld() {
        when(userRepository.findById(20L)).thenReturn(Optional.of(andereLeerkracht));

        service.overdragen(100L, 20L);

        assertEquals(andereLeerkracht, lijst.getLeerkracht());
    }

    @Test
    void givenGeenDoelId_whenOverdragen_thenIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class,
                () -> service.overdragen(100L, null));
    }

    @Test
    void givenOnbekendeLijst_whenOverdragen_thenKlassLeeslijstNotFoundException() {
        when(leeslijstRepository.findById(999L)).thenReturn(Optional.empty());
        assertThrows(KlassLeeslijstNotFoundException.class,
                () -> service.overdragen(999L, 20L));
    }

    @Test
    void givenOnbekendeDoelLeerkracht_whenOverdragen_thenGebruikerNotFoundException() {
        when(userRepository.findById(999L)).thenReturn(Optional.empty());
        assertThrows(GebruikerNotFoundException.class,
                () -> service.overdragen(100L, 999L));
    }

    @Test
    void givenZelfdeLeerkracht_whenOverdragen_thenIllegalArgumentException() {
        when(userRepository.findById(10L)).thenReturn(Optional.of(huidigeLeerkracht));
        assertThrows(IllegalArgumentException.class,
                () -> service.overdragen(100L, 10L));
    }

    @Test
    void givenDoelIsLeerling_whenOverdragen_thenIllegalArgumentException() {
        User leerling = new User("ll-1", "1A", Rol.LEERLING);
        leerling.setId(30L);
        leerling.setSchool(school1);
        leerling.setActief(true);
        when(userRepository.findById(30L)).thenReturn(Optional.of(leerling));

        assertThrows(IllegalArgumentException.class,
                () -> service.overdragen(100L, 30L));
    }

    @Test
    void givenInactieveDoelLeerkracht_whenOverdragen_thenIllegalArgumentException() {
        andereLeerkracht.setActief(false);
        when(userRepository.findById(20L)).thenReturn(Optional.of(andereLeerkracht));

        assertThrows(IllegalArgumentException.class,
                () -> service.overdragen(100L, 20L));
    }

    @Test
    void givenDoelLeerkrachtAndereSchool_whenOverdragen_thenIllegalArgumentException() {
        andereLeerkracht.setSchool(school2);
        when(userRepository.findById(20L)).thenReturn(Optional.of(andereLeerkracht));

        assertThrows(IllegalArgumentException.class,
                () -> service.overdragen(100L, 20L));
    }
}
