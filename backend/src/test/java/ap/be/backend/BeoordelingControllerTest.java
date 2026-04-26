package ap.be.backend;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import ap.be.backend.controller.BeoordelingController;
import ap.be.backend.dto.BeoordelingDTO;
import ap.be.backend.service.BeoordelingService;
import ap.be.backend.service.GerapporteerdeBeoordelingService;

@ExtendWith(MockitoExtension.class)
class BeoordelingControllerTest {

    @Mock
    private BeoordelingService beoordelingService;

    @Mock
    private GerapporteerdeBeoordelingService gerapporteerdeBeoordelingService;

    @InjectMocks
    private BeoordelingController beoordelingController;

    @Test
    void getBeoordelingenOpBoekIdGeeftLijstTerug() {
        Long boekId = 1L;
        List<BeoordelingDTO> expected = List.of(
            new BeoordelingDTO(10L, "Test Gebruiker", boekId, 4, "Sterke review", false, false, false));
        when(beoordelingService.getBeoordelingenOpBoekId(boekId)).thenReturn(expected);

        List<BeoordelingDTO> result = beoordelingController.getBeoordelingenOpBoekId(boekId);

        assertEquals(1, result.size());
        assertEquals("Sterke review", result.get(0).inhoud());
        verify(beoordelingService, times(1)).getBeoordelingenOpBoekId(boekId);
    }

    @Test
    void beoordelingOpslaanReturnedZelfdeDto() {
        BeoordelingDTO dto = new BeoordelingDTO(11L, "Leerling", 2L, 5, "Top boek", true, false, false);
        doNothing().when(beoordelingService).beoordelingOpslaan(dto);

        BeoordelingDTO response = beoordelingController.beoordelingOpslaan(dto);

        assertEquals(dto, response);
        verify(beoordelingService, times(1)).beoordelingOpslaan(dto);
    }

    @Test
    void rapporteerBeoordelingGebruiktTijdelijkeGebruikerId1() {
        Long beoordelingId = 7L;
        String reden = "spoiler";

        beoordelingController.rapporteerBeoordeling(beoordelingId, reden);

        verify(gerapporteerdeBeoordelingService, times(1))
                .rapporteerBeoordeling(beoordelingId, 1L, reden);
    }

    @Test
    void verwijderBeoordelingGeeftNoContent() {
        Long beoordelingId = 33L;
        doNothing().when(beoordelingService).verwijderBeoordeling(beoordelingId);

        var response = beoordelingController.verwijderBeoordeling(beoordelingId);

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        verify(beoordelingService, times(1)).verwijderBeoordeling(beoordelingId);
    }
}