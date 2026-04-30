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

import ap.be.backend.controller.AdminReportController;
import ap.be.backend.dto.GerapporteerdeBeoordelingAdminDTO;
import ap.be.backend.service.GerapporteerdeBeoordelingService;

@ExtendWith(MockitoExtension.class)
class AdminReportControllerTest {

    @Mock
    private GerapporteerdeBeoordelingService gerapporteerdeBeoordelingService;

    @InjectMocks
    private AdminReportController adminReportController;

    @Test
    void getAlleRapportagesGeeftOkMetDataTerug() {
        List<GerapporteerdeBeoordelingAdminDTO> expected = List.of(
                new GerapporteerdeBeoordelingAdminDTO(1L, 10L, "Boektitel", "Reviewtekst", 2L, 3L,
                        "spoiler", null));
        when(gerapporteerdeBeoordelingService.getAlleGerapporteerdeBeoordelingen()).thenReturn(expected);

        var response = adminReportController.getAlleRapportages();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(1, response.getBody().size());
        verify(gerapporteerdeBeoordelingService, times(1)).getAlleGerapporteerdeBeoordelingen();
    }

    @Test
    void negeerRapportageGeeftNoContentTerug() {
        Long rapportageId = 7L;
        doNothing().when(gerapporteerdeBeoordelingService).verwijderRapportage(rapportageId);

        var response = adminReportController.negeerRapportage(rapportageId);

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        verify(gerapporteerdeBeoordelingService, times(1)).verwijderRapportage(rapportageId);
    }
}
