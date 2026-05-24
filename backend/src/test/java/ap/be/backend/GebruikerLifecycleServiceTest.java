package ap.be.backend;

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
import ap.be.backend.service.OneRosterClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GebruikerLifecycleServiceTest {

    @Mock private UserRepository userRepository;
    @Mock private SchoolRepository schoolRepository;
    @Mock private UitleningRepository uitleningRepository;
    @Mock private OneRosterClient oneRosterClient;
    @Mock private KlasWijzigingRepository klasWijzigingRepository;
    @Mock private KlassLeeslijstRepository leeslijstRepository;

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
    void wijzigKlas_updatetEnSchrijftHistoriek() {
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
    void wijzigKlas_ongewijzigd_doetNiets() {
        service.wijzigKlas(42L, "1A");

        verify(userRepository, never()).save(any(User.class));
        verify(klasWijzigingRepository, never()).save(any(KlasWijziging.class));
    }

    @Test
    void wijzigKlas_onbestaandeKlas_gooitException() {
        assertThrows(IllegalArgumentException.class, () -> service.wijzigKlas(42L, "ZZZ"));
        verify(klasWijzigingRepository, never()).save(any(KlasWijziging.class));
    }

    @Test
    void wijzigKlas_leegInput_gooitException() {
        assertThrows(IllegalArgumentException.class, () -> service.wijzigKlas(42L, "  "));
    }

    @Test
    void wijzigKlas_leerkracht_gooitException() {
        User lk = new User("lk-1", null, Rol.LEERKRACHT);
        lk.setSchool(school);
        when(userRepository.findById(99L)).thenReturn(Optional.of(lk));

        assertThrows(IllegalArgumentException.class, () -> service.wijzigKlas(99L, "1B"));
    }

    @Test
    void wijzigKlas_onbekendeLeerling_gooitNotFound() {
        when(userRepository.findById(123L)).thenReturn(Optional.empty());
        assertThrows(GebruikerNotFoundException.class, () -> service.wijzigKlas(123L, "1B"));
    }

    @Test
    void markeerVertrokken_leerkracht_archiveertActieveLeeslijsten() {
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
    void markeerVertrokken_leerling_doetNietsMetLeeslijsten() {
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
    void markeerVertrokken_alVertrokken_doetNiets() {
        User lk = new User("lk-1", null, Rol.LEERKRACHT);
        lk.setId(77L);
        lk.setActief(false);
        when(userRepository.findById(77L)).thenReturn(Optional.of(lk));

        service.markeerVertrokken(77L);

        verify(userRepository, never()).save(any(User.class));
        verify(leeslijstRepository, never()).saveAll(any());
    }

}
