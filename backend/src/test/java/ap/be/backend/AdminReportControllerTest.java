package ap.be.backend;

import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;

import ap.be.backend.dto.GerapporteerdeBeoordelingAdminDTO;
import ap.be.backend.service.GerapporteerdeBeoordelingService;

class AdminReportControllerTest extends AbstractIntegrationTest {

    @MockBean
    private GerapporteerdeBeoordelingService gerapporteerdeBeoordelingService;

    @Test
    void givenGerapporteerdeBeoordelingen_whenGetAlleRapportages_thenOkMetData() throws Exception {
        // Arrange
        when(gerapporteerdeBeoordelingService.getAlleGerapporteerdeBeoordelingen()).thenReturn(List.of(
                new GerapporteerdeBeoordelingAdminDTO(1L, 10L, "Boektitel", "Reviewtekst", 2L, 3L,
                        "spoiler", null)));

        // Act + Assert
        mockMvc.perform(get("/api/reports"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].boekTitel").value("Boektitel"));

        verify(gerapporteerdeBeoordelingService, times(1)).getAlleGerapporteerdeBeoordelingen();
    }

    @Test
    void givenBestaandeRapportage_whenNegeerRapportage_thenNoContent() throws Exception {
        // Arrange
        Long rapportageId = 7L;
        doNothing().when(gerapporteerdeBeoordelingService).verwijderRapportage(rapportageId);

        // Act + Assert
        mockMvc.perform(delete("/api/reports/{id}", rapportageId))
                .andExpect(status().isNoContent());

        verify(gerapporteerdeBeoordelingService, times(1)).verwijderRapportage(rapportageId);
    }
}
