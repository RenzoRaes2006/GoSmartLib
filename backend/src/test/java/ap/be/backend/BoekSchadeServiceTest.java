package ap.be.backend;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import ap.be.backend.entity.Boek;
import ap.be.backend.entity.BoekSchade;
import ap.be.backend.entity.Exemplaar;
import ap.be.backend.entity.ExemplaarStatus;
import ap.be.backend.entity.Uitlening;
import ap.be.backend.repository.BoekSchadeRepository;
import ap.be.backend.service.BoekSchadeService;
import ap.be.backend.service.ExemplaarService;
import ap.be.backend.service.OneRosterService;

@ExtendWith(MockitoExtension.class)
public class BoekSchadeServiceTest {

    @Mock
    private BoekSchadeRepository boekSchadeRepository;

    @Mock
    private OneRosterService oneRosterService;

    @Mock
    private ExemplaarService exemplaarService;

    @InjectMocks
    private BoekSchadeService boekSchadeService;

    @Test
    void givenSchadeMetBeschadigdExemplaar_whenVerwijder_thenHersteltExemplaarEnVerwijdertRapport() {
        Boek boek = new Boek();
        boek.setTitel("Clean Code");
        Exemplaar exemplaar = new Exemplaar(boek, 1, ExemplaarStatus.BESCHADIGD);
        Uitlening uitlening = new Uitlening();
        uitlening.setExemplaar(exemplaar);
        BoekSchade schade = new BoekSchade();
        schade.setId(7L);
        schade.setUitlening(uitlening);

        when(boekSchadeRepository.findById(7L)).thenReturn(Optional.of(schade));

        boolean result = boekSchadeService.verwijder(7L);

        assertTrue(result);
        verify(boekSchadeRepository, times(1)).delete(schade);
        verify(exemplaarService, times(1)).herstelBeschadigd(exemplaar);
    }

    @Test
    void givenOnbestaandSchade_whenVerwijder_thenFalseEnGeenHerstel() {
        when(boekSchadeRepository.findById(99L)).thenReturn(Optional.empty());

        boolean result = boekSchadeService.verwijder(99L);

        assertFalse(result);
        verify(boekSchadeRepository, never()).delete(org.mockito.ArgumentMatchers.any());
        verify(exemplaarService, never()).herstelBeschadigd(org.mockito.ArgumentMatchers.any());
    }
}
