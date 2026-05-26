package ap.be.backend;

import ap.be.backend.dto.RolloverPreviewDTO;
import ap.be.backend.dto.RolloverResultaatDTO;
import ap.be.backend.dto.SchooljaarDTO;
import ap.be.backend.entity.*;
import ap.be.backend.exception.OpenLeningenBlokkadeException;
import ap.be.backend.exception.SchoolNotFoundException;
import ap.be.backend.exception.SchooljaarNotFoundException;
import ap.be.backend.repository.*;
import ap.be.backend.service.OneRosterImportService;
import ap.be.backend.service.SchooljaarService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SchooljaarServiceTest {

    @Mock private SchooljaarRepository schooljaarRepository;
    @Mock private SchoolRepository schoolRepository;
    @Mock private UserRepository userRepository;
    @Mock private BeoordelingRepository beoordelingRepository;
    @Mock private KlassLeeslijstRepository leeslijstRepository;
    @Mock private UitleningRepository uitleningRepository;
    @Mock private OneRosterImportService oneRosterImportService;
    @Mock private ap.be.backend.service.AuditService auditService;

    @InjectMocks
    private SchooljaarService schooljaarService;

    private School testSchool;
    private User vertrokkenLeerling;
    private Uitlening openUitlening;
    private Boek testBoek;
    private KlassLeeslijst testLeeslijst;
    private Beoordeling testBeoordeling;
    private OneRosterImportService.ImportResultaat importResultaat;

    private static final Long SCHOOL_ID = 1L;
    private static final LocalDate START = LocalDate.of(2025, 9, 1);
    private static final LocalDate EIND = LocalDate.of(2026, 6, 30);

    @BeforeEach
    void setUp() {
        testSchool = new School();
        testSchool.setId(SCHOOL_ID);

        vertrokkenLeerling = new User();
        vertrokkenLeerling.setId(10L);
        vertrokkenLeerling.setSmartschoolId("leerling-vertrokken");
        vertrokkenLeerling.setActief(true);

        testBoek = new Boek();
        testBoek.setId(5L);
        testBoek.setTitel("De Aanslag");
        testBoek.setIsbn("9789023443");

        openUitlening = new Uitlening();
        openUitlening.setId(20L);
        openUitlening.setUser(vertrokkenLeerling);
        openUitlening.setBoek(testBoek);
        openUitlening.setIngeleverd(false);

        testLeeslijst = new KlassLeeslijst();
        testLeeslijst.setId(30L);
        testLeeslijst.setGearchiveerd(false);

        testBeoordeling = new Beoordeling();
        testBeoordeling.setId(40L);
        testBeoordeling.setAnoniem(false);

        importResultaat = new OneRosterImportService.ImportResultaat(
                1, 0, 0, 0, 0, 0, 1, Set.of("leerling-blijft"));
    }

    // ─── getActiefSchooljaar ────────────────────────────────────────────────

    @Test
    void givenActiefSchooljaarAanwezig_whenGetActiefSchooljaar_thenReturnDTO() {
        Schooljaar schooljaar = new Schooljaar("2024-2025", START, EIND, true);
        schooljaar.setId(1L);
        when(schooljaarRepository.findByActiefTrue()).thenReturn(Optional.of(schooljaar));

        SchooljaarDTO result = schooljaarService.getActiefSchooljaar();

        assertEquals("2024-2025", result.naam());
        assertTrue(result.actief());
    }

    @Test
    void givenGeenActiefSchooljaar_whenGetActiefSchooljaar_thenThrowsSchooljaarNotFoundException() {
        when(schooljaarRepository.findByActiefTrue()).thenReturn(Optional.empty());

        assertThrows(SchooljaarNotFoundException.class,
                () -> schooljaarService.getActiefSchooljaar());
    }

    // ─── getAllSchooljaren ──────────────────────────────────────────────────

    @Test
    void givenMeerdereSchooljaren_whenGetAllSchooljaren_thenReturnGesorteerdeDTO() {
        Schooljaar oud = new Schooljaar("2023-2024", LocalDate.of(2023, 9, 1), LocalDate.of(2024, 6, 30), false);
        oud.setId(1L);
        Schooljaar nieuw = new Schooljaar("2024-2025", START, EIND, true);
        nieuw.setId(2L);
        when(schooljaarRepository.findAll()).thenReturn(List.of(oud, nieuw));

        List<SchooljaarDTO> result = schooljaarService.getAllSchooljaren();

        assertEquals(2, result.size());
        // nieuwste eerst
        assertEquals("2024-2025", result.get(0).naam());
        assertEquals("2023-2024", result.get(1).naam());
    }

    @Test
    void givenGeenSchooljaren_whenGetAllSchooljaren_thenReturnLeegeLijst() {
        when(schooljaarRepository.findAll()).thenReturn(List.of());

        List<SchooljaarDTO> result = schooljaarService.getAllSchooljaren();

        assertTrue(result.isEmpty());
    }

    // ─── previewRollover ────────────────────────────────────────────────────

    @Test
    void givenSchoolNietGevonden_whenPreviewRollover_thenThrowsSchoolNotFoundException() {
        when(schoolRepository.findById(SCHOOL_ID)).thenReturn(Optional.empty());

        assertThrows(SchoolNotFoundException.class,
                () -> schooljaarService.previewRollover(SCHOOL_ID));
    }

    @Test
    void givenOneRosterLeeg_whenPreviewRollover_thenThrowsIllegalStateException() {
        when(schoolRepository.findById(SCHOOL_ID)).thenReturn(Optional.of(testSchool));
        when(oneRosterImportService.fetchGebruikers(SCHOOL_ID))
                .thenReturn(Map.of("leerlingen", List.of()));

        assertThrows(IllegalStateException.class,
                () -> schooljaarService.previewRollover(SCHOOL_ID));
    }

    @Test
    void givenGeenVertrokkenLeerlingen_whenPreviewRollover_thenKanDoorgaanIsTrue() {
        when(schoolRepository.findById(SCHOOL_ID)).thenReturn(Optional.of(testSchool));
        when(oneRosterImportService.fetchGebruikers(SCHOOL_ID))
                .thenReturn(Map.of("leerlingen", List.of(Map.of("id", "leerling-blijft"))));
        when(userRepository.findVertrokkenLeerlingen(SCHOOL_ID, Set.of("leerling-blijft")))
                .thenReturn(List.of());
        when(userRepository.findBySchool_IdAndRol(SCHOOL_ID, Rol.LEERKRACHT))
                .thenReturn(List.of());

        RolloverPreviewDTO result = schooljaarService.previewRollover(SCHOOL_ID);

        assertEquals(0, result.aantalVertrokkenLeerlingen());
        assertTrue(result.openLeningenVertrokkenen().isEmpty());
        assertTrue(result.kanDoorgaan());
    }

    @Test
    void givenVertrokkenLeerlingMetOpenLening_whenPreviewRollover_thenKanDoorgaanIsFalse() {
        when(schoolRepository.findById(SCHOOL_ID)).thenReturn(Optional.of(testSchool));
        when(oneRosterImportService.fetchGebruikers(SCHOOL_ID))
                .thenReturn(Map.of("leerlingen", List.of(Map.of("id", "leerling-blijft"))));
        when(userRepository.findVertrokkenLeerlingen(SCHOOL_ID, Set.of("leerling-blijft")))
                .thenReturn(List.of(vertrokkenLeerling));
        when(uitleningRepository.findByUser_IdInAndIsIngeleverdFalse(List.of(10L)))
                .thenReturn(List.of(openUitlening));
        when(userRepository.findBySchool_IdAndRol(SCHOOL_ID, Rol.LEERKRACHT))
                .thenReturn(List.of());

        RolloverPreviewDTO result = schooljaarService.previewRollover(SCHOOL_ID);

        assertEquals(1, result.aantalVertrokkenLeerlingen());
        assertEquals(1, result.openLeningenVertrokkenen().size());
        assertEquals("De Aanslag", result.openLeningenVertrokkenen().get(0).boekTitel());
        assertFalse(result.kanDoorgaan());
    }

    @Test
    void givenLeerkrachtenMetLeeslijsten_whenPreviewRollover_thenTeltKlasLeeslijsten() {
        User leerkracht = new User();
        leerkracht.setId(50L);

        when(schoolRepository.findById(SCHOOL_ID)).thenReturn(Optional.of(testSchool));
        when(oneRosterImportService.fetchGebruikers(SCHOOL_ID))
                .thenReturn(Map.of("leerlingen", List.of(Map.of("id", "leerling-blijft"))));
        when(userRepository.findVertrokkenLeerlingen(SCHOOL_ID, Set.of("leerling-blijft")))
                .thenReturn(List.of());
        when(userRepository.findBySchool_IdAndRol(SCHOOL_ID, Rol.LEERKRACHT))
                .thenReturn(List.of(leerkracht));
        when(leeslijstRepository.findByLeerkracht_IdInAndKlasIsNotNullAndGearchiveerdFalse(List.of(50L)))
                .thenReturn(List.of(testLeeslijst));

        RolloverPreviewDTO result = schooljaarService.previewRollover(SCHOOL_ID);

        assertEquals(1, result.aantalTeArchiverenKlasLeeslijsten());
        assertTrue(result.kanDoorgaan());
    }

    // ─── voerRolloverUit ────────────────────────────────────────────────────

    @Test
    void givenSchoolNietGevonden_whenVoerRolloverUit_thenThrowsSchoolNotFoundException() {
        when(schoolRepository.findById(SCHOOL_ID)).thenReturn(Optional.empty());

        assertThrows(SchoolNotFoundException.class,
                () -> schooljaarService.voerRolloverUit(SCHOOL_ID, false, "2025-2026", START, EIND));
    }

    @Test
    void givenOneRosterLeeg_whenVoerRolloverUit_thenThrowsIllegalStateException() {
        when(schoolRepository.findById(SCHOOL_ID)).thenReturn(Optional.of(testSchool));
        OneRosterImportService.ImportResultaat leegResultaat =
                new OneRosterImportService.ImportResultaat(0, 0, 0, 0, 0, 0, 0, Set.of());
        when(oneRosterImportService.importSchool(SCHOOL_ID)).thenReturn(leegResultaat);

        assertThrows(IllegalStateException.class,
                () -> schooljaarService.voerRolloverUit(SCHOOL_ID, false, "2025-2026", START, EIND));
    }

    @Test
    void givenOpenLeningenZonderForceer_whenVoerRolloverUit_thenThrowsOpenLeningenBlokkadeException() {
        when(schoolRepository.findById(SCHOOL_ID)).thenReturn(Optional.of(testSchool));
        when(oneRosterImportService.importSchool(SCHOOL_ID)).thenReturn(importResultaat);
        when(userRepository.findVertrokkenLeerlingen(SCHOOL_ID, Set.of("leerling-blijft")))
                .thenReturn(List.of(vertrokkenLeerling));
        when(uitleningRepository.findByUser_IdInAndIsIngeleverdFalse(List.of(10L)))
                .thenReturn(List.of(openUitlening));

        OpenLeningenBlokkadeException ex = assertThrows(OpenLeningenBlokkadeException.class,
                () -> schooljaarService.voerRolloverUit(SCHOOL_ID, false, "2025-2026", START, EIND));

        assertTrue(ex.getMessage().contains("1"));
        verify(uitleningRepository, never()).saveAll(anyList());
    }

    @Test
    void givenOpenLeningenMetForceer_whenVoerRolloverUit_thenMarkeertAlsVerloren() {
        when(schoolRepository.findById(SCHOOL_ID)).thenReturn(Optional.of(testSchool));
        when(oneRosterImportService.importSchool(SCHOOL_ID)).thenReturn(importResultaat);
        when(userRepository.findVertrokkenLeerlingen(SCHOOL_ID, Set.of("leerling-blijft")))
                .thenReturn(List.of(vertrokkenLeerling));
        when(uitleningRepository.findByUser_IdInAndIsIngeleverdFalse(List.of(10L)))
                .thenReturn(List.of(openUitlening));
        when(beoordelingRepository.findByUser_IdInAndIsAnoniemFalse(List.of(10L)))
                .thenReturn(List.of());
        when(userRepository.findBySchool_IdAndRol(SCHOOL_ID, Rol.LEERKRACHT)).thenReturn(List.of());
        when(schooljaarRepository.findByActiefTrue()).thenReturn(Optional.empty());
        when(schooljaarRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        RolloverResultaatDTO result = schooljaarService.voerRolloverUit(
                SCHOOL_ID, true, "2025-2026", START, EIND);

        assertEquals("VERLOREN", openUitlening.getStatus());
        assertTrue(openUitlening.isIngeleverd());
        verify(uitleningRepository).saveAll(List.of(openUitlening));
        assertEquals(1, result.leningenAlsVerlorenGemarkeerd());
    }

    @Test
    void givenVertrokkenLeerling_whenVoerRolloverUit_thenDeactiveertEnAnonimiseert() {
        Beoordeling beoordeling = new Beoordeling();
        beoordeling.setId(40L);
        beoordeling.setAnoniem(false);

        when(schoolRepository.findById(SCHOOL_ID)).thenReturn(Optional.of(testSchool));
        when(oneRosterImportService.importSchool(SCHOOL_ID)).thenReturn(importResultaat);
        when(userRepository.findVertrokkenLeerlingen(SCHOOL_ID, Set.of("leerling-blijft")))
                .thenReturn(List.of(vertrokkenLeerling));
        when(uitleningRepository.findByUser_IdInAndIsIngeleverdFalse(List.of(10L)))
                .thenReturn(List.of());
        when(beoordelingRepository.findByUser_IdInAndIsAnoniemFalse(List.of(10L)))
                .thenReturn(List.of(beoordeling));
        when(userRepository.findBySchool_IdAndRol(SCHOOL_ID, Rol.LEERKRACHT)).thenReturn(List.of());
        when(schooljaarRepository.findByActiefTrue()).thenReturn(Optional.empty());
        when(schooljaarRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        RolloverResultaatDTO result = schooljaarService.voerRolloverUit(
                SCHOOL_ID, false, "2025-2026", START, EIND);

        assertFalse(vertrokkenLeerling.isActief());
        verify(userRepository).saveAll(List.of(vertrokkenLeerling));
        assertTrue(beoordeling.isAnoniem());
        verify(beoordelingRepository).saveAll(List.of(beoordeling));
        assertEquals(1, result.leerlingenGedeactiveerd());
        assertEquals(1, result.beoordelingenGeanonimiseerd());
    }

    @Test
    void givenLeerkrachtenMetKlasLeeslijsten_whenVoerRolloverUit_thenArchiveertLeeslijsten() {
        User leerkracht = new User();
        leerkracht.setId(50L);

        when(schoolRepository.findById(SCHOOL_ID)).thenReturn(Optional.of(testSchool));
        when(oneRosterImportService.importSchool(SCHOOL_ID)).thenReturn(importResultaat);
        when(userRepository.findVertrokkenLeerlingen(SCHOOL_ID, Set.of("leerling-blijft")))
                .thenReturn(List.of());
        when(userRepository.findBySchool_IdAndRol(SCHOOL_ID, Rol.LEERKRACHT))
                .thenReturn(List.of(leerkracht));
        when(leeslijstRepository.findByLeerkracht_IdInAndKlasIsNotNullAndGearchiveerdFalse(List.of(50L)))
                .thenReturn(List.of(testLeeslijst));
        when(schooljaarRepository.findByActiefTrue()).thenReturn(Optional.empty());
        when(schooljaarRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        RolloverResultaatDTO result = schooljaarService.voerRolloverUit(
                SCHOOL_ID, false, "2025-2026", START, EIND);

        assertTrue(testLeeslijst.isGearchiveerd());
        verify(leeslijstRepository).saveAll(List.of(testLeeslijst));
        assertEquals(1, result.leeslijstenGearchiveerd());
    }

    @Test
    void givenHuidigActiefSchooljaar_whenVoerRolloverUit_thenDeactiveertHuidigEnMaaktNieuw() {
        Schooljaar huidig = new Schooljaar("2024-2025", LocalDate.of(2024, 9, 1), LocalDate.of(2025, 6, 30), true);
        huidig.setId(1L);

        when(schoolRepository.findById(SCHOOL_ID)).thenReturn(Optional.of(testSchool));
        when(oneRosterImportService.importSchool(SCHOOL_ID)).thenReturn(importResultaat);
        when(userRepository.findVertrokkenLeerlingen(SCHOOL_ID, Set.of("leerling-blijft")))
                .thenReturn(List.of());
        when(userRepository.findBySchool_IdAndRol(SCHOOL_ID, Rol.LEERKRACHT)).thenReturn(List.of());
        when(schooljaarRepository.findByActiefTrue()).thenReturn(Optional.of(huidig));
        when(schooljaarRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        RolloverResultaatDTO result = schooljaarService.voerRolloverUit(
                SCHOOL_ID, false, "2025-2026", START, EIND);

        assertFalse(huidig.isActief());
        assertEquals("2025-2026", result.nieuwSchooljaar());
        // 1x voor het deactiveren van het huidig schooljaar, 1x voor het nieuwe
        verify(schooljaarRepository, times(2)).save(any());
    }

    @Test
    void givenGeenVertrokkenLeerlingen_whenVoerRolloverUit_thenResultaatIsNul() {
        when(schoolRepository.findById(SCHOOL_ID)).thenReturn(Optional.of(testSchool));
        when(oneRosterImportService.importSchool(SCHOOL_ID)).thenReturn(importResultaat);
        when(userRepository.findVertrokkenLeerlingen(SCHOOL_ID, Set.of("leerling-blijft")))
                .thenReturn(List.of());
        when(userRepository.findBySchool_IdAndRol(SCHOOL_ID, Rol.LEERKRACHT)).thenReturn(List.of());
        when(schooljaarRepository.findByActiefTrue()).thenReturn(Optional.empty());
        when(schooljaarRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        RolloverResultaatDTO result = schooljaarService.voerRolloverUit(
                SCHOOL_ID, false, "2025-2026", START, EIND);

        assertEquals(0, result.leerlingenGedeactiveerd());
        assertEquals(0, result.beoordelingenGeanonimiseerd());
        assertEquals(0, result.leeslijstenGearchiveerd());
        assertEquals(0, result.leningenAlsVerlorenGemarkeerd());
        assertEquals("2025-2026", result.nieuwSchooljaar());
    }
}
