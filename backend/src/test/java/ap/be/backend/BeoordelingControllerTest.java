package ap.be.backend;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;

import ap.be.backend.controller.BeoordelingController;
import ap.be.backend.dto.BeoordelingDTO;
import ap.be.backend.exception.BoekNotFoundException;
import ap.be.backend.service.BeoordelingService;
import ap.be.backend.service.GerapporteerdeBeoordelingService;

@WebMvcTest(BeoordelingController.class)
public class BeoordelingControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private BeoordelingService beoordelingService;

    @MockBean
    private GerapporteerdeBeoordelingService gerapporteerdeBeoordelingService;

    private BeoordelingDTO testDto;
    private BeoordelingDTO fouteDto;
    private final Long BOEK_ID = 1L;
    private final Long BEOORDELING_ID = 10L;

    @BeforeEach
    void setUp() {
        // boolean 'false' toegevoegd aan het einde van de DTOs voor de gerapporteerd status
        testDto = new BeoordelingDTO(BEOORDELING_ID, 1L, BOEK_ID, 5, "Prachtig boek!", false);
        fouteDto = new BeoordelingDTO(null, 1L, BOEK_ID, 9, "Te hoge score", false); 
    }

    @Test
    void givenBoekBestaat_whenGetBeoordelingenOpBoekId_thenReturns200AndList() throws Exception {
        when(beoordelingService.getBeoordelingenOpBoekId(BOEK_ID)).thenReturn(List.of(testDto));

        mockMvc.perform(get("/api/books/" + BOEK_ID + "/reviews"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$[0].inhoud").value("Prachtig boek!"))
                .andExpect(jsonPath("$[0].waardering").value(5))
                .andExpect(jsonPath("$[0].gerapporteerd").value(false)); // Nieuwe check toegevoegd
    }

    @Test
    void givenBoekBestaatNiet_whenGetBeoordelingenOpBoekId_thenReturns404() throws Exception {
        when(beoordelingService.getBeoordelingenOpBoekId(BOEK_ID))
                .thenThrow(new BoekNotFoundException("Boek niet gevonden"));

        mockMvc.perform(get("/api/books/" + BOEK_ID + "/reviews"))
                .andExpect(status().isNotFound());
    }

    @Test
    void givenGeldigeDto_whenBeoordelingOpslaan_thenReturns200AndDto() throws Exception {
        mockMvc.perform(post("/api/reviews")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.inhoud").value("Prachtig boek!"));
    }

    @Test
    void givenOngeldigeDto_whenBeoordelingOpslaan_thenReturns400BadRequest() throws Exception {
        mockMvc.perform(post("/api/reviews")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(fouteDto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void givenGeldigeReden_whenRapporteerBeoordeling_thenReturns200Ok() throws Exception {
        String reden = "Ongepaste taal";

        mockMvc.perform(post("/api/reviews/" + BEOORDELING_ID + "/report")
                .contentType(MediaType.TEXT_PLAIN)
                .content(reden))
                .andExpect(status().isOk());
    }
}