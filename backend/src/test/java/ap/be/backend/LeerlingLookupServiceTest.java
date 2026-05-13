package ap.be.backend;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.when;

import ap.be.backend.entity.Rol;
import ap.be.backend.entity.User;
import ap.be.backend.exception.GebruikerNotFoundException;
import ap.be.backend.repository.BoekRepository;
import ap.be.backend.repository.UserRepository;
import ap.be.backend.repository.LeeslijstMapRepository;
import ap.be.backend.repository.UitleningRepository;
import ap.be.backend.service.GebruikerService;
import ap.be.backend.service.InstellingService;

@ExtendWith(MockitoExtension.class)
public class LeerlingLookupServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private LeeslijstMapRepository leeslijstMapRepository;

    @Mock
    private BoekRepository boekRepository;

    @Mock
    private UitleningRepository uitleningRepository;

    @Mock
    private InstellingService instellingService;

    @InjectMocks
    private GebruikerService gebruikerService;

    private User leerling;

    @BeforeEach
    void setUp() {
        leerling = new User();
        leerling.setId(1L);
        leerling.setSmartschoolId("12345");
        leerling.setRol(Rol.LEERLING);
    }

    @Test
    void givenBestaandeLeerling_whenGetLeerlingBySmartschoolId_thenReturnLeerling() {
        when(userRepository.findBySmartschoolId("12345")).thenReturn(Optional.of(leerling));

        User result = gebruikerService.getLeerlingBySmartschoolId("12345");

        assertEquals("12345", result.getSmartschoolId());
        assertEquals(Rol.LEERLING, result.getRol());
    }

    @Test
    void givenOnbekendeSmartschoolId_whenGetLeerlingBySmartschoolId_thenThrowNotFound() {
        when(userRepository.findBySmartschoolId("99999")).thenReturn(Optional.empty());

        assertThrows(GebruikerNotFoundException.class,
                () -> gebruikerService.getLeerlingBySmartschoolId("99999"));
    }

    @Test
    void givenGebruikerIsBeheerder_whenGetLeerlingBySmartschoolId_thenThrowNotFound() {
        User beheerder = new User();
        beheerder.setId(2L);
        beheerder.setSmartschoolId("54321");
        beheerder.setRol(Rol.BEHEERDER);
        when(userRepository.findBySmartschoolId("54321")).thenReturn(Optional.of(beheerder));

        assertThrows(GebruikerNotFoundException.class,
                () -> gebruikerService.getLeerlingBySmartschoolId("54321"));
    }

    @Test
    void givenGebruikerIsLeerkracht_whenGetLeerlingBySmartschoolId_thenThrowNotFound() {
        User leerkracht = new User();
        leerkracht.setId(3L);
        leerkracht.setSmartschoolId("77777");
        leerkracht.setRol(Rol.LEERKRACHT);
        when(userRepository.findBySmartschoolId("77777")).thenReturn(Optional.of(leerkracht));

        assertThrows(GebruikerNotFoundException.class,
                () -> gebruikerService.getLeerlingBySmartschoolId("77777"));
    }
}
