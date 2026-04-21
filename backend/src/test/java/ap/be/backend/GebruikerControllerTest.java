package ap.be.backend;

import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import ap.be.backend.controller.GebruikerController;
import ap.be.backend.repository.LeeslijstMapRepository;
import ap.be.backend.service.GebruikerService;
import ap.be.backend.exception.GlobalExceptionHandler;

@ExtendWith(MockitoExtension.class)
public class GebruikerControllerTest {

    private MockMvc mockMvc;

    // Mockimplementaties van de afhankelijkheden van de controller
    @Mock
    private GebruikerService gebruikerService;

    @Mock
    private LeeslijstMapRepository leeslijstMapRepository;

    // Injecteert de gemockte afhankelijkheden in de controller
    @InjectMocks
    private GebruikerController gebruikerController;

    @BeforeEach
    void setUp() {
        // Using standalone setup to test the controller in isolation without loading
        // the full Spring context.
        mockMvc = MockMvcBuilders.standaloneSetup(gebruikerController)
                .setControllerAdvice(new GlobalExceptionHandler()) // Register the global exception handler for
                                                                   // consistent error responses during the test.
                .build();
    }

    // --- PATCH: UITLENING VERLENGEN (FEATURE 55) ---

    @Test
    public void givenGeldigeUitlening_whenVerlengLening_thenStatus200() throws Exception {
        // We simuleren dat de service de actie succesvol uitvoert (doNothing)
        doNothing().when(gebruikerService).verlengUitlening(100L);

        // When & Then: We verwachten een 200 OK
        mockMvc.perform(patch("/api/gebruiker/leningen/100/verlengen"))
                .andExpect(status().isOk())
                .andExpect(content().string("Uitleentermijn succesvol verlengd met 14 dagen."));
    }

    @Test
    public void givenAlVerlengdeUitlening_whenVerlengLening_thenStatus400() throws Exception {
        // We simuleren dat de gebruiker de limiet probeert te breken
        doThrow(new IllegalArgumentException("Je hebt dit boek al een keer verlengd."))
                .when(gebruikerService).verlengUitlening(100L);

        // When & Then: Je GlobalExceptionHandler moet hier een 400 Bad Request van
        // maken
        mockMvc.perform(patch("/api/gebruiker/leningen/100/verlengen"))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Je hebt dit boek al een keer verlengd."));
    }
}