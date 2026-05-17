package ap.be.backend;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.times;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import ap.be.backend.service.InstellingService;

@SpringBootTest
@AutoConfigureMockMvc
public class InstellingControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private InstellingService instellingService;

    @Test
    @WithMockUser(roles = "BEHEERDER")
    void givenValidRequest_whenGetInlevertermijn_thenReturns200AndValue() throws Exception {
        when(instellingService.getInlevertermijnDagen()).thenReturn(21);

        mockMvc.perform(get("/api/instellingen/inlevertermijn"))
                .andExpect(status().isOk())
                .andExpect(content().string("21"));
    }

    @Test
    @WithMockUser(roles = "BEHEERDER")
    void givenValidDays_whenUpdateInlevertermijn_thenReturns200AndSuccessMessage() throws Exception {
        int newDays = 21;

        mockMvc.perform(put("/api/instellingen/inlevertermijn")
                .contentType(MediaType.APPLICATION_JSON)
                .content(String.valueOf(newDays)))
                .andExpect(status().isOk())
                .andExpect(content().string("Standaard inlevertermijn succesvol geüpdatet naar 21 dagen."));

        verify(instellingService, times(1)).updateInlevertermijnDagen(newDays);
    }

    @Test
    @WithMockUser(roles = "BEHEERDER")
    void givenInvalidDaysTooLow_whenUpdateInlevertermijn_thenReturns400() throws Exception {
        int invalidDays = 0;

        mockMvc.perform(put("/api/instellingen/inlevertermijn")
                .contentType(MediaType.APPLICATION_JSON)
                .content(String.valueOf(invalidDays)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Inlevertermijn moet tussen 1 en 365 dagen liggen."));

        verify(instellingService, times(0)).updateInlevertermijnDagen(invalidDays);
    }

    @Test
    @WithMockUser(roles = "BEHEERDER")
    void givenInvalidDaysTooHigh_whenUpdateInlevertermijn_thenReturns400() throws Exception {
        int invalidDays = 366;

        mockMvc.perform(put("/api/instellingen/inlevertermijn")
                .contentType(MediaType.APPLICATION_JSON)
                .content(String.valueOf(invalidDays)))
                .andExpect(status().isBadRequest());

        verify(instellingService, times(0)).updateInlevertermijnDagen(invalidDays);
    }
}