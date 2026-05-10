package ap.be.backend;

import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;

import ap.be.backend.exception.LeenVoorwaardeException;
import ap.be.backend.service.GebruikerService;

public class GebruikerControllerTest extends AbstractIntegrationTest {

    @MockBean
    private GebruikerService gebruikerService;

    @Test
    public void givenGeldigeUitlening_whenVerlengLening_thenStatus200() throws Exception {
        // Arrange
        doNothing().when(gebruikerService).verlengUitlening(100L);

        // Act + Assert
        mockMvc.perform(patch("/api/gebruiker/leningen/100/verlengen"))
                .andExpect(status().isOk())
                .andExpect(content().string("Uitleentermijn succesvol verlengd met 14 dagen."));
    }

    @Test
    public void givenAlVerlengdeUitlening_whenVerlengLening_thenStatus400() throws Exception {
        // Arrange
        doThrow(new LeenVoorwaardeException("Je hebt dit boek al een keer verlengd."))
                .when(gebruikerService).verlengUitlening(100L);

        // Act + Assert
        mockMvc.perform(patch("/api/gebruiker/leningen/100/verlengen"))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Je hebt dit boek al een keer verlengd."));
    }
}
