package ap.be.backend;

import ap.be.backend.dto.BeoordelingDTO;
import ap.be.backend.entity.Beoordeling;
import ap.be.backend.entity.Boek;
import ap.be.backend.repository.BeoordelingRepository;
import ap.be.backend.service.BeoordelingService;
import ap.be.backend.repository.BoekRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class BeoordelingServiceTest {

    private BeoordelingRepository beoordelingRepository;
    private BoekRepository boekRepository;
    private BeoordelingService beoordelingService;

    @BeforeEach
    public void setup() {
        beoordelingRepository = mock(BeoordelingRepository.class);
        boekRepository = mock(BoekRepository.class);
        beoordelingService = new BeoordelingService(beoordelingRepository, boekRepository);
    }

    // getBeoordelingenOpBoekId tests

    @Test
    public void given_bestaandeBeoordelingen_when_getBeoordelingenOpBoekId_then_returnMappedDTOs() {
        Long boekId = 1L;

        // 1. Maak een Boek en stel de ID in
        Boek boek = new Boek();
        // Stel in hoe jij je ID set, bijv: boek.setId(boekId);

        // 2. Koppel het boek aan de beoordeling
        Beoordeling beoordeling = new Beoordeling();
        beoordeling.setBoek(boek);

        when(beoordelingRepository.findByBoekId(boekId)).thenReturn(List.of(beoordeling));

        List<BeoordelingDTO> result = beoordelingService.getBeoordelingenOpBoekId(boekId);

        assertEquals(1, result.size());
        verify(beoordelingRepository, times(1)).findByBoekId(boekId);
    }

    @Test
    public void given_geenBeoordelingen_when_getBeoordelingenOpBoekId_then_returnEmptyList() {
        Long boekId = 2L;
        when(beoordelingRepository.findByBoekId(boekId)).thenReturn(List.of());

        List<BeoordelingDTO> result = beoordelingService.getBeoordelingenOpBoekId(boekId);

        assertTrue(result.isEmpty());
        verify(beoordelingRepository, times(1)).findByBoekId(boekId);
    }

    // beoordelingOpslaan tests

    @Test
    public void given_bestaandBoek_when_beoordelingOpslaan_then_verifySaveIsCalled() {
        Long boekId = 1L;
        // Mock de DTO om het boekId terug te geven
        BeoordelingDTO dto = mock(BeoordelingDTO.class);
        when(dto.boekId()).thenReturn(boekId);

        Boek boek = new Boek();
        when(boekRepository.findById(boekId)).thenReturn(Optional.of(boek));

        beoordelingService.beoordelingOpslaan(dto);

        verify(boekRepository, times(1)).findById(boekId);
        verify(beoordelingRepository, times(1)).save(any()); // any() omdat de mapper een nieuwe instantie maakt
    }

    @Test
    public void given_nietBestaandBoek_when_beoordelingOpslaan_then_throwRuntimeException() {
        Long boekId = 99L;
        BeoordelingDTO dto = mock(BeoordelingDTO.class);
        when(dto.boekId()).thenReturn(boekId);

        when(boekRepository.findById(boekId)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            beoordelingService.beoordelingOpslaan(dto);
        });

        assertEquals("Boek niet gevonden", exception.getMessage());
        verify(boekRepository, times(1)).findById(boekId);
        verify(beoordelingRepository, never()).save(any()); // Save mag nooit worden aangeroepen
    }
}