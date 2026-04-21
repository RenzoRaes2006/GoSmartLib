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

import ap.be.backend.entity.Gebruiker;
import ap.be.backend.entity.Rol;
import ap.be.backend.exception.GebruikerNotFoundException;
import ap.be.backend.repository.BoekRepository;
import ap.be.backend.repository.GebruikerRepository;
import ap.be.backend.repository.LeeslijstMapRepository;
import ap.be.backend.repository.UitleningRepository;
import ap.be.backend.service.GebruikerService;

@ExtendWith(MockitoExtension.class)
public class LeerlingLookupServiceTest {

    @Mock
    private GebruikerRepository gebruikerRepository;

    @Mock
    private LeeslijstMapRepository leeslijstMapRepository;

    @Mock
    private BoekRepository boekRepository;

    @Mock
    private UitleningRepository uitleningRepository;

    @InjectMocks
    private GebruikerService gebruikerService;

    private Gebruiker leerling;

    @BeforeEach
    void setUp() {
        leerling = new Gebruiker();
        leerling.setGebruikerID(1L);
        leerling.setSchoolId(12345L);
        leerling.setVoorNaam("Test");
        leerling.setAchternaam("Leerling");
        leerling.setRol(Rol.LEERLING);
    }

    @Test
    void givenBestaandeLeerling_whenGetLeerlingBySchoolId_thenReturnLeerling() {
        when(gebruikerRepository.findBySchoolId(12345L)).thenReturn(Optional.of(leerling));

        Gebruiker result = gebruikerService.getLeerlingBySchoolId(12345L);

        assertEquals(12345L, result.getSchoolId());
        assertEquals(Rol.LEERLING, result.getRol());
    }

    @Test
    void givenOnbekendeSchoolId_whenGetLeerlingBySchoolId_thenThrowNotFound() {
        when(gebruikerRepository.findBySchoolId(99999L)).thenReturn(Optional.empty());

        GebruikerNotFoundException ex = assertThrows(GebruikerNotFoundException.class,
                () -> gebruikerService.getLeerlingBySchoolId(99999L));

        assertEquals("Leerling met school ID 99999 niet gevonden", ex.getMessage());
    }

    @Test
    void givenGebruikerIsBeheerder_whenGetLeerlingBySchoolId_thenThrowNotFound() {
        Gebruiker beheerder = new Gebruiker();
        beheerder.setGebruikerID(2L);
        beheerder.setSchoolId(54321L);
        beheerder.setRol(Rol.BEHEERDER);
        when(gebruikerRepository.findBySchoolId(54321L)).thenReturn(Optional.of(beheerder));

        GebruikerNotFoundException ex = assertThrows(GebruikerNotFoundException.class,
                () -> gebruikerService.getLeerlingBySchoolId(54321L));

        assertEquals("Leerling met school ID 54321 niet gevonden", ex.getMessage());
    }

    @Test
    void givenGebruikerIsLeerkracht_whenGetLeerlingBySchoolId_thenThrowNotFound() {
        Gebruiker leerkracht = new Gebruiker();
        leerkracht.setGebruikerID(3L);
        leerkracht.setSchoolId(77777L);
        leerkracht.setRol(Rol.LEERKRACHT);
        when(gebruikerRepository.findBySchoolId(77777L)).thenReturn(Optional.of(leerkracht));

        assertThrows(GebruikerNotFoundException.class,
                () -> gebruikerService.getLeerlingBySchoolId(77777L));
    }
}
