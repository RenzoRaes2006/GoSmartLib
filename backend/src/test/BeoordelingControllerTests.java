package ap.be.backend.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;

import ap.be.backend.dto.BeoordelingDTO;
import ap.be.backend.service.BeoordelingService;

@WebMvcTest(BeoordelingController.class)
class BeoordelingControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private BeoordelingService beoordelingService;

    @Test
    void given_bestaandBoekId_when_getBeoordelingen_then_returnBeoordelingenLijstMetStatus200() throws Exception {
        // Arrange
        Long boekId = 1L;
        BeoordelingDTO beoordeling = new BeoordelingDTO();
        List<BeoordelingDTO> verwachteLijst = Arrays.asList(beoordeling);

        when(beoordelingService.getBeoordelingenOpBoekId(boekId)).thenReturn(verwachteLijst);

        // Act & Assert
        mockMvc.perform(get("/api/books/{id}/reviews", boekId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()").value(1));

        // Controleer of de service daadwerkelijk is aangeroepen
        verify(beoordelingService).getBeoordelingenOpBoekId(boekId);
    }

    @Test
    void given_geldigeBeoordeling_when_beoordelingOpslaan_then_returnOpgeslagenBeoordelingMetStatus200() throws Exception {
        // Arrange
        BeoordelingDTO nieuweBeoordeling = new BeoordelingDTO();
        // Optioneel: vul velden in van je DTO voor specifiekere checks (bijv. nieuweBeoordeling.setScore(5);)

        // Act & Assert
        mockMvc.perform(post("/api/reviews")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(nieuweBeoordeling)))
                .andExpect(status().isOk());

        // Controleer of de opslaan-methode van de service is aangeroepen met een BeoordelingDTO
        verify(beoordelingService).beoordelingOpslaan(any(BeoordelingDTO.class));
    }
}