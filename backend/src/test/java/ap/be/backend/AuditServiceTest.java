package ap.be.backend;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import ap.be.backend.entity.AuditActie;
import ap.be.backend.entity.AuditLog;
import ap.be.backend.entity.Rol;
import ap.be.backend.entity.User;
import ap.be.backend.repository.AuditLogRepository;
import ap.be.backend.repository.UserRepository;
import ap.be.backend.service.AuditService;

@ExtendWith(MockitoExtension.class)
class AuditServiceTest {

    @Mock private AuditLogRepository repository;
    @Mock private UserRepository userRepository;

    @InjectMocks private AuditService service;

    private User admin;

    @BeforeEach
    void setUp() {
        admin = new User("admin-1", null, Rol.LEERKRACHT);
        admin.setId(77L);
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void givenIngelogdeUser_whenLog_thenActorVeldenGezet() {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("admin-1", null, java.util.List.of()));
        when(userRepository.findBySmartschoolId("admin-1")).thenReturn(Optional.of(admin));

        service.log(AuditActie.GEBRUIKER_TOEGEVOEGD, "Gebruiker", 5L,
                AuditService.details("smartschoolId", "ll-9", "klas", "1A"));

        ArgumentCaptor<AuditLog> captor = ArgumentCaptor.forClass(AuditLog.class);
        verify(repository).save(captor.capture());
        AuditLog rij = captor.getValue();
        assertEquals(AuditActie.GEBRUIKER_TOEGEVOEGD, rij.getActie());
        assertEquals("Gebruiker", rij.getEntiteitType());
        assertEquals(5L, rij.getEntiteitId());
        assertEquals(77L, rij.getActorGebruikerId());
        assertEquals("admin-1", rij.getActorSmartschoolId());
        assertNotNull(rij.getDetailsJson());
        assertTrue(rij.getDetailsJson().contains("\"smartschoolId\":\"ll-9\""));
        assertTrue(rij.getDetailsJson().contains("\"klas\":\"1A\""));
    }

    @Test
    void givenGeenSecurityContext_whenLog_thenActorBlijftNull() {
        service.log(AuditActie.KLAS_GEWIJZIGD, "Gebruiker", 1L, Map.of("van", "1A", "naar", "1B"));

        ArgumentCaptor<AuditLog> captor = ArgumentCaptor.forClass(AuditLog.class);
        verify(repository).save(captor.capture());
        AuditLog rij = captor.getValue();
        assertNull(rij.getActorGebruikerId());
        assertNull(rij.getActorSmartschoolId());
    }

    @Test
    void givenIngelogdeUser_whenLogSysteem_thenActorIsNull() {
        // Bewijst dat sync-acties geen actor krijgen ook al is er een ingelogde user.
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("admin-1", null, java.util.List.of()));

        service.logSysteem(AuditActie.GEBRUIKER_VERTROKKEN_SYNC, "Gebruiker", 9L,
                AuditService.details("smartschoolId", "ll-x"));

        ArgumentCaptor<AuditLog> captor = ArgumentCaptor.forClass(AuditLog.class);
        verify(repository).save(captor.capture());
        AuditLog rij = captor.getValue();
        assertNull(rij.getActorGebruikerId());
        assertNull(rij.getActorSmartschoolId());
        assertEquals(AuditActie.GEBRUIKER_VERTROKKEN_SYNC, rij.getActie());
        // Bewijst dat we userRepository niet raadplegen voor systeem-acties.
        verify(userRepository, never()).findBySmartschoolId(any());
    }

    @Test
    void givenGeenDetails_whenLog_thenDetailsJsonIsNull() {
        service.log(AuditActie.GEBRUIKER_GEREACTIVEERD, "Gebruiker", 1L, null);

        ArgumentCaptor<AuditLog> captor = ArgumentCaptor.forClass(AuditLog.class);
        verify(repository).save(captor.capture());
        assertNull(captor.getValue().getDetailsJson());
    }

    @Test
    void givenRepositoryException_whenLog_thenCallerWordtNietGeblokkeerd() {
        // Audit-bug mag nooit een lifecycle-actie laten falen.
        when(repository.save(any())).thenThrow(new RuntimeException("DB stuk"));

        service.log(AuditActie.ROLLOVER_UITGEVOERD, "School", 1L, null);
        // Geen exception = pass.
    }

    @Test
    void givenOnevenAantalArgs_whenDetails_thenException() {
        org.junit.jupiter.api.Assertions.assertThrows(
                IllegalArgumentException.class,
                () -> AuditService.details("een"));
    }
}
