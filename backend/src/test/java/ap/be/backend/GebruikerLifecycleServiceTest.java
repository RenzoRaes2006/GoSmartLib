package ap.be.backend;

import ap.be.backend.dto.LeerkrachtVertrekPreviewDTO;
import ap.be.backend.entity.AuditActie;
import ap.be.backend.entity.KlasWijziging;
import ap.be.backend.entity.KlassLeeslijst;
import ap.be.backend.entity.Rol;
import ap.be.backend.entity.School;
import ap.be.backend.entity.User;
import ap.be.backend.exception.GebruikerNotFoundException;
import ap.be.backend.repository.KlasWijzigingRepository;
import ap.be.backend.repository.KlassLeeslijstRepository;
import ap.be.backend.repository.SchoolRepository;
import ap.be.backend.repository.UitleningRepository;
import ap.be.backend.repository.UserRepository;
import ap.be.backend.service.GebruikerLifecycleService;
import ap.be.backend.service.KlassLeeslijstService;
import ap.be.backend.service.OneRosterClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GebruikerLifecycleServiceTest {

    @Mock private UserRepository userRepository;
    @Mock private SchoolRepository schoolRepository;
    @Mock private UitleningRepository uitleningRepository;
    @Mock private OneRosterClient oneRosterClient;
    @Mock private KlasWijzigingRepository klasWijzigingRepository;
    @Mock private ap.be.backend.service.AuditService auditService;
    @Mock private KlassLeeslijstRepository leeslijstRepository;
    @Mock private KlassLeeslijstService leeslijstService;

    @InjectMocks private GebruikerLifecycleService service;

    private School school;
    private User leerling;

    @BeforeEach
    void setUp() {
        school = new School();
        school.setId(1L);
        leerling = new User("ll-1", "1A", Rol.LEERLING);
        leerling.setSchool(school);
        // Geen Smartschool-credentials → service valt terug op DB-distinct-klassen
        lenient().when(userRepository.findById(42L)).thenReturn(Optional.of(leerling));
        lenient().when(userRepository.findDistinctKlassenBySchoolId(1L)).thenReturn(List.of("1A", "1B", "2A"));
    }

    @Test
    void givenNieuweKlas_whenWijzigKlas_thenUpdatetEnSchrijftHistoriek() {
        service.wijzigKlas(42L, "1B");

        assertEquals("1B", leerling.getKlas());
        verify(userRepository).save(leerling);

        ArgumentCaptor<KlasWijziging> captor = ArgumentCaptor.forClass(KlasWijziging.class);
        verify(klasWijzigingRepository).save(captor.capture());
        KlasWijziging rij = captor.getValue();
        assertSame(leerling, rij.getLeerling());
        assertEquals("1A", rij.getVanKlas());
        assertEquals("1B", rij.getNaarKlas());
        assertEquals("HANDMATIG", rij.getBron());
        assertNull(rij.getDoorWie());
    }

    @Test
    void givenZelfdeKlas_whenWijzigKlas_thenDoetNiets() {
        service.wijzigKlas(42L, "1A");

        verify(userRepository, never()).save(any(User.class));
        verify(klasWijzigingRepository, never()).save(any(KlasWijziging.class));
    }

    @Test
    void givenOnbestaandeKlas_whenWijzigKlas_thenIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> service.wijzigKlas(42L, "ZZZ"));
        verify(klasWijzigingRepository, never()).save(any(KlasWijziging.class));
    }

    @Test
    void givenLeegKlasInput_whenWijzigKlas_thenIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> service.wijzigKlas(42L, "  "));
    }

    @Test
    void givenLeerkracht_whenWijzigKlas_thenIllegalArgumentException() {
        User lk = new User("lk-1", null, Rol.LEERKRACHT);
        lk.setSchool(school);
        when(userRepository.findById(99L)).thenReturn(Optional.of(lk));

        assertThrows(IllegalArgumentException.class, () -> service.wijzigKlas(99L, "1B"));
    }

    @Test
    void givenOnbekendeLeerling_whenWijzigKlas_thenGebruikerNotFoundException() {
        when(userRepository.findById(123L)).thenReturn(Optional.empty());
        assertThrows(GebruikerNotFoundException.class, () -> service.wijzigKlas(123L, "1B"));
    }

    @Test
    void givenNieuweKlas_whenWijzigKlas_thenAuditRijGeschreven() {
        service.wijzigKlas(42L, "1B");
        verify(auditService).log(
                org.mockito.ArgumentMatchers.eq(AuditActie.KLAS_GEWIJZIGD),
                org.mockito.ArgumentMatchers.eq("Gebruiker"),
                org.mockito.ArgumentMatchers.any(),
                org.mockito.ArgumentMatchers.anyMap());
    }

    @Test
    void givenZelfdeKlas_whenWijzigKlas_thenGeenAudit() {
        service.wijzigKlas(42L, "1A");
        verify(auditService, never()).log(
                org.mockito.ArgumentMatchers.any(AuditActie.class),
                org.mockito.ArgumentMatchers.anyString(),
                org.mockito.ArgumentMatchers.any(),
                org.mockito.ArgumentMatchers.anyMap());
    }

    @Test
    void givenActieveGebruiker_whenMarkeerVertrokken_thenAuditRijGeschreven() {
        when(uitleningRepository.findByUser_IdAndIsIngeleverdFalse(42L)).thenReturn(List.of());
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        service.markeerVertrokken(42L);

        verify(auditService).log(
                org.mockito.ArgumentMatchers.eq(AuditActie.GEBRUIKER_VERTROKKEN),
                org.mockito.ArgumentMatchers.eq("Gebruiker"),
                org.mockito.ArgumentMatchers.any(),
                org.mockito.ArgumentMatchers.anyMap());
    }

    @Test
    void givenInactieveGebruiker_whenReactiveer_thenAuditRijGeschreven() {
        leerling.setActief(false);
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        service.reactiveer(42L);

        verify(auditService).log(
                org.mockito.ArgumentMatchers.eq(AuditActie.GEBRUIKER_GEREACTIVEERD),
                org.mockito.ArgumentMatchers.eq("Gebruiker"),
                org.mockito.ArgumentMatchers.any(),
                org.mockito.ArgumentMatchers.anyMap());
    }

    @Test
    void givenNieuweGebruiker_whenVoegHandmatigToe_thenAuditRijGeschreven() {
        when(schoolRepository.findById(1L)).thenReturn(Optional.of(school));
        when(userRepository.findBySmartschoolId("nieuw-1")).thenReturn(Optional.empty());
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        service.voegHandmatigToe("nieuw-1", 1L, "1A", Rol.LEERLING);

        verify(auditService).log(
                org.mockito.ArgumentMatchers.eq(AuditActie.GEBRUIKER_TOEGEVOEGD),
                org.mockito.ArgumentMatchers.eq("Gebruiker"),
                org.mockito.ArgumentMatchers.any(),
                org.mockito.ArgumentMatchers.anyMap());
    }

    @Test
    void givenLeerkrachtMetLeeslijsten_whenMarkeerVertrokken_thenLeeslijstenGearchiveerd() {
        User lk = new User("lk-1", null, Rol.LEERKRACHT);
        lk.setId(77L);
        lk.setActief(true);
        when(userRepository.findById(77L)).thenReturn(Optional.of(lk));
        when(uitleningRepository.findByUser_IdAndIsIngeleverdFalse(77L)).thenReturn(List.of());

        KlassLeeslijst l1 = new KlassLeeslijst();
        l1.setGearchiveerd(false);
        KlassLeeslijst l2 = new KlassLeeslijst();
        l2.setGearchiveerd(false);
        when(leeslijstRepository.findByLeerkrachtIdAndGearchiveerdFalseOrderByAangemaaktOpDesc(77L))
                .thenReturn(List.of(l1, l2));

        service.markeerVertrokken(77L);

        verify(userRepository).save(lk);
        org.junit.jupiter.api.Assertions.assertFalse(lk.isActief());
        org.junit.jupiter.api.Assertions.assertTrue(l1.isGearchiveerd());
        org.junit.jupiter.api.Assertions.assertTrue(l2.isGearchiveerd());
        verify(leeslijstRepository).saveAll(List.of(l1, l2));
    }

    @Test
    void givenLeerling_whenMarkeerVertrokken_thenGeenLeeslijstActie() {
        leerling.setId(42L);
        leerling.setActief(true);
        when(uitleningRepository.findByUser_IdAndIsIngeleverdFalse(42L)).thenReturn(List.of());

        service.markeerVertrokken(42L);

        org.junit.jupiter.api.Assertions.assertFalse(leerling.isActief());
        verify(leeslijstRepository, never())
                .findByLeerkrachtIdAndGearchiveerdFalseOrderByAangemaaktOpDesc(any());
        verify(leeslijstRepository, never()).saveAll(any());
    }

    @Test
    void givenAlVertrokken_whenMarkeerVertrokken_thenDoetNiets() {
        User lk = new User("lk-1", null, Rol.LEERKRACHT);
        lk.setId(77L);
        lk.setActief(false);
        when(userRepository.findById(77L)).thenReturn(Optional.of(lk));

        service.markeerVertrokken(77L);

        verify(userRepository, never()).save(any(User.class));
        verify(leeslijstRepository, never()).saveAll(any());
    }

    @Test
    void givenOverdrachten_whenMarkeerVertrokken_thenGeselecteerdOvergedragenRestGearchiveerd() {
        User lk = new User("lk-1", null, Rol.LEERKRACHT);
        lk.setId(77L);
        lk.setActief(true);
        when(userRepository.findById(77L)).thenReturn(Optional.of(lk));
        when(uitleningRepository.findByUser_IdAndIsIngeleverdFalse(77L)).thenReturn(List.of());

        KlassLeeslijst l1 = new KlassLeeslijst();
        l1.setId(101L);
        l1.setGearchiveerd(false);
        KlassLeeslijst l2 = new KlassLeeslijst();
        l2.setId(102L);
        l2.setGearchiveerd(false);
        KlassLeeslijst l3 = new KlassLeeslijst();
        l3.setId(103L);
        l3.setGearchiveerd(false);
        when(leeslijstRepository.findByLeerkrachtIdAndGearchiveerdFalseOrderByAangemaaktOpDesc(77L))
                .thenReturn(List.of(l1, l2, l3));

        // l1 + l3 overdragen aan lk 88, l2 → archiveren
        service.markeerVertrokken(77L, Map.of(101L, 88L, 103L, 88L));

        verify(leeslijstService).overdragen(eq(101L), eq(88L));
        verify(leeslijstService).overdragen(eq(103L), eq(88L));
        verify(leeslijstService, never()).overdragen(eq(102L), any());

        // Enkel niet-overgedragen lijsten worden gearchiveerd
        assertTrue(l2.isGearchiveerd());
        org.junit.jupiter.api.Assertions.assertFalse(l1.isGearchiveerd());
        org.junit.jupiter.api.Assertions.assertFalse(l3.isGearchiveerd());
        verify(leeslijstRepository).saveAll(List.of(l2));

        // Leerkracht zelf wordt op vertrokken gezet
        org.junit.jupiter.api.Assertions.assertFalse(lk.isActief());
        verify(userRepository).save(lk);
    }

    @Test
    void givenVreemdeLijstInOverdracht_whenMarkeerVertrokken_thenIllegalArgumentException() {
        User lk = new User("lk-1", null, Rol.LEERKRACHT);
        lk.setId(77L);
        lk.setActief(true);
        when(userRepository.findById(77L)).thenReturn(Optional.of(lk));
        when(uitleningRepository.findByUser_IdAndIsIngeleverdFalse(77L)).thenReturn(List.of());

        KlassLeeslijst eigen = new KlassLeeslijst();
        eigen.setId(101L);
        when(leeslijstRepository.findByLeerkrachtIdAndGearchiveerdFalseOrderByAangemaaktOpDesc(77L))
                .thenReturn(List.of(eigen));

        // Lijst-id 999 hoort niet bij deze leerkracht → moet falen
        assertThrows(IllegalArgumentException.class,
                () -> service.markeerVertrokken(77L, Map.of(999L, 88L)));

        // Bij fout: leerkracht blijft actief — transactie rolt terug.
        // (Dat doet Spring zelf; we verifiëren hier dat we niet hebben gesaved vóór de fout.)
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void givenLeerlingMetOverdrachten_whenMarkeerVertrokken_thenIllegalArgumentException() {
        leerling.setId(42L);
        leerling.setActief(true);
        when(uitleningRepository.findByUser_IdAndIsIngeleverdFalse(42L)).thenReturn(List.of());

        assertThrows(IllegalArgumentException.class,
                () -> service.markeerVertrokken(42L, Map.of(1L, 2L)));

        verifyNoInteractions(leeslijstService);
    }

    @Test
    void givenLeerkracht_whenGetLeerkrachtVertrekPreview_thenLeeslijstenEnActieveCollegas() {
        // Mapper toDTO heeft een statische OneRoster-afhankelijkheid; om hem
        // zonder Spring-context te kunnen aanroepen geven we een lege lijst van
        // leeslijsten terug. De preview-call zelf doet zo geen mapping.
        User lk = new User("lk-1", null, Rol.LEERKRACHT);
        lk.setId(77L);
        lk.setSchool(school);
        lk.setActief(true);
        when(userRepository.findById(77L)).thenReturn(Optional.of(lk));
        when(leeslijstRepository.findByLeerkrachtIdAndGearchiveerdFalseOrderByAangemaaktOpDesc(77L))
                .thenReturn(List.of());

        User collega = new User("lk-2", null, Rol.LEERKRACHT);
        collega.setId(88L);
        collega.setSchool(school);
        collega.setActief(true);
        User vertrokkenCollega = new User("lk-3", null, Rol.LEERKRACHT);
        vertrokkenCollega.setId(89L);
        vertrokkenCollega.setSchool(school);
        vertrokkenCollega.setActief(false);
        // De vertrekkende leerkracht zelf zit ook in de school-lijst en moet eruit gefilterd worden
        when(userRepository.findBySchool_IdAndRol(school.getId(), Rol.LEERKRACHT))
                .thenReturn(List.of(lk, collega, vertrokkenCollega));
        when(uitleningRepository.findByUser_IdAndIsIngeleverdFalse(77L)).thenReturn(List.of());

        LeerkrachtVertrekPreviewDTO preview = service.getLeerkrachtVertrekPreview(77L);

        assertEquals(0, preview.aantalOpenLeningen());
        // Alleen actieve collega's behalve zichzelf
        assertEquals(1, preview.kandidaten().size());
        assertEquals(88L, preview.kandidaten().get(0).id());
    }

    @Test
    void givenLeerling_whenGetLeerkrachtVertrekPreview_thenIllegalArgumentException() {
        leerling.setId(42L);
        assertThrows(IllegalArgumentException.class,
                () -> service.getLeerkrachtVertrekPreview(42L));
    }

}
