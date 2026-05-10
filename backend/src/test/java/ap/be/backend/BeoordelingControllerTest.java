package ap.be.backend;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;

import ap.be.backend.dto.BeoordelingDTO;
import ap.be.backend.service.BeoordelingService;
import ap.be.backend.service.GerapporteerdeBeoordelingService;

class BeoordelingControllerTest extends AbstractIntegrationTest {

    @MockBean
    private BeoordelingService beoordelingService;

    @MockBean
    private GerapporteerdeBeoordelingService gerapporteerdeBeoordelingService;

    @Test
    void getBeoordelingenOpBoekIdGeeftLijstTerug() throws Exception {
        // Arrange
        Long boekId = 1L;
        when(beoordelingService.getBeoordelingenOpBoekId(boekId)).thenReturn(List.of(
                new BeoordelingDTO(10L, "Test Gebruiker", boekId, 4, "Sterke review", false, false, false)));

        // Act + Assert
        mockMvc.perform(get("/api/books/{id}/reviews", boekId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].inhoud").value("Sterke review"));

        verify(beoordelingService, times(1)).getBeoordelingenOpBoekId(boekId);
    }

    @Test
    void beoordelingOpslaanReturnedZelfdeDto() throws Exception {
        // Arrange
        BeoordelingDTO dto = new BeoordelingDTO(11L, "Leerling", 2L, 5, "Top boek", true, false, false);
        doNothing().when(beoordelingService).beoordelingOpslaan(any(BeoordelingDTO.class));

        // Act + Assert
        mockMvc.perform(post("/api/reviews")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.inhoud").value("Top boek"));

        verify(beoordelingService, times(1)).beoordelingOpslaan(any(BeoordelingDTO.class));
    }

    @Test
    void rapporteerBeoordelingGebruiktTijdelijkeGebruikerId1() throws Exception {
        // Arrange
        Long beoordelingId = 7L;
        String reden = "spoiler";

        // Act
        mockMvc.perform(post("/api/reviews/{id}/report", beoordelingId)
                .contentType(MediaType.TEXT_PLAIN)
                .content(reden))
                .andExpect(status().isOk());

        // Assert
        verify(gerapporteerdeBeoordelingService, times(1))
                .rapporteerBeoordeling(eq(beoordelingId), eq(1L), eq(reden));
    }

    @Test
    void verwijderBeoordelingGeeftNoContent() throws Exception {
        // Arrange
        Long beoordelingId = 33L;
        doNothing().when(beoordelingService).verwijderBeoordeling(beoordelingId);

        // Act + Assert
        mockMvc.perform(delete("/api/reviews/{id}", beoordelingId))
                .andExpect(status().isNoContent());

        verify(beoordelingService, times(1)).verwijderBeoordeling(beoordelingId);
    }
}
