package ap.be.backend;

import ap.be.backend.entity.Rol;
import ap.be.backend.entity.School;
import ap.be.backend.entity.User;
import ap.be.backend.repository.SchoolRepository;
import ap.be.backend.repository.UserRepository;
import ap.be.backend.service.AuditService;
import ap.be.backend.service.GebruikerLifecycleService;
import ap.be.backend.service.OneRosterClient;
import ap.be.backend.service.OneRosterImportService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class OneRosterImportServiceTest {

    @Mock
    private SchoolRepository schoolRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private OneRosterClient oneRosterClient;

    @Mock
    private GebruikerLifecycleService lifecycleService;

    @Mock
    private AuditService auditService;

    @InjectMocks
    private OneRosterImportService service;

    private School school;
    private Map<String, Object> liveData;

    @BeforeEach
    void setUp() {
        school = new School();
        school.setId(1L);
        school.setNaam("Test");
        school.setSmartschoolDomein("test.smartschool.be");
        school.setOauthClientId("client");
        school.setOauthClientSecret("secret");

        liveData = new HashMap<>();
        liveData.put("klassen", List.of(
                Map.of("id", "k1", "naam", "1A"),
                Map.of("id", "k2", "naam", "1B")
        ));
        liveData.put("leerlingen", List.of(
                Map.of("id", "ll-100", "klas", "1A"),
                Map.of("id", "ll-101", "klas", "1B")
        ));
        liveData.put("leerkrachten", List.of(
                Map.of("id", "lk-200", "klas", "")
        ));

        lenient().when(schoolRepository.findById(1L)).thenReturn(Optional.of(school));
        lenient().when(oneRosterClient.haalLiveSchoolDataOp("test.smartschool.be", "client", "secret"))
                .thenReturn(liveData);
    }

    @Test
    void givenGeenBestaandeGebruikers_whenImportSchool_thenAlleGebruikersNieuw() {
        when(userRepository.findBySmartschoolId(anyString())).thenReturn(Optional.empty());

        OneRosterImportService.ImportResultaat r = service.importSchool(1L);

        assertEquals(2, r.leerlingenNieuw());
        assertEquals(0, r.leerlingenGeupdatet());
        assertEquals(0, r.leerlingenOngewijzigd());
        assertEquals(1, r.leerkrachtenNieuw());
        assertEquals(0, r.leerkrachtenGeupdatet());
        assertEquals(0, r.leerkrachtenOngewijzigd());
        assertEquals(2, r.aantalKlassen());
        verify(userRepository, times(3)).save(any(User.class));
    }

    @Test
    void givenIdentiekeLiveData_whenImportSchool_thenNietsGewijzigd() {
        User ll1 = bestaande("ll-100", "1A", Rol.LEERLING);
        User ll2 = bestaande("ll-101", "1B", Rol.LEERLING);
        User lk = bestaande("lk-200", "", Rol.LEERKRACHT);
        when(userRepository.findBySmartschoolId("ll-100")).thenReturn(Optional.of(ll1));
        when(userRepository.findBySmartschoolId("ll-101")).thenReturn(Optional.of(ll2));
        when(userRepository.findBySmartschoolId("lk-200")).thenReturn(Optional.of(lk));

        OneRosterImportService.ImportResultaat r = service.importSchool(1L);

        assertEquals(0, r.leerlingenNieuw());
        assertEquals(0, r.leerlingenGeupdatet());
        assertEquals(2, r.leerlingenOngewijzigd());
        assertEquals(0, r.leerkrachtenNieuw());
        assertEquals(0, r.leerkrachtenGeupdatet());
        assertEquals(1, r.leerkrachtenOngewijzigd());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void givenGewijzigdeKlas_whenImportSchool_thenUserGeupdatetMetHistoriek() {
        User ll1 = bestaande("ll-100", "1A", Rol.LEERLING);
        User ll2 = bestaande("ll-101", "OUDE-KLAS", Rol.LEERLING);
        User lk = bestaande("lk-200", "", Rol.LEERKRACHT);
        when(userRepository.findBySmartschoolId("ll-100")).thenReturn(Optional.of(ll1));
        when(userRepository.findBySmartschoolId("ll-101")).thenReturn(Optional.of(ll2));
        when(userRepository.findBySmartschoolId("lk-200")).thenReturn(Optional.of(lk));

        OneRosterImportService.ImportResultaat r = service.importSchool(1L);

        assertEquals(0, r.leerlingenNieuw());
        assertEquals(1, r.leerlingenGeupdatet());
        assertEquals(1, r.leerlingenOngewijzigd());
        assertEquals(1, r.leerkrachtenOngewijzigd());
        assertEquals("1B", ll2.getKlas());
        verify(userRepository, times(1)).save(ll2);
        // Klaswijziging via OneRoster-sync schrijft een historiek-rij met bron ONEROSTER_SYNC.
        verify(lifecycleService, times(1))
                .schrijfHistoriek(eq(ll2), eq("OUDE-KLAS"), eq("1B"), isNull(), eq("ONEROSTER_SYNC"));
    }

    @Test
    void givenOngewijzigdeKlas_whenImportSchool_thenGeenHistoriek() {
        User ll1 = bestaande("ll-100", "1A", Rol.LEERLING);
        User ll2 = bestaande("ll-101", "1B", Rol.LEERLING);
        User lk = bestaande("lk-200", "", Rol.LEERKRACHT);
        when(userRepository.findBySmartschoolId("ll-100")).thenReturn(Optional.of(ll1));
        when(userRepository.findBySmartschoolId("ll-101")).thenReturn(Optional.of(ll2));
        when(userRepository.findBySmartschoolId("lk-200")).thenReturn(Optional.of(lk));

        service.importSchool(1L);

        verify(lifecycleService, never())
                .schrijfHistoriek(any(), any(), any(), any(), any());
    }

    @Test
    void givenGeenOneRosterCredentials_whenImportSchool_thenIllegalStateException() {
        school.setSmartschoolDomein(null);

        assertThrows(IllegalStateException.class, () -> service.importSchool(1L));
    }

    @Test
    void givenOnbekendeSchool_whenImportSchool_thenIllegalArgumentException() {
        when(schoolRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> service.importSchool(99L));
    }

    private User bestaande(String smartschoolId, String klas, Rol rol) {
        User u = new User(smartschoolId, klas, rol);
        u.setSchool(school);
        return u;
    }
}
