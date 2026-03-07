package ap.be.backend; // Let op: check even of jouw test in deze package staat of in ap.be.backend.controller

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

import java.util.Optional;
import java.util.Set;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import ap.be.backend.controller.GebruikerController;
import ap.be.backend.repository.GebruikerRepository;
import ap.be.backend.entity.Gebruiker;
import ap.be.backend.entity.Boek;

@WebMvcTest(GebruikerController.class)
public class GebruikerControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private GebruikerRepository gebruikerRepository;

    @Test
    public void givenGebruikerMetLeeslijst_whenGetLeeslijst_thenReturnStatusOkEnBoeken() throws Exception {
        // mock data
        Gebruiker mockUser = new Gebruiker();
        mockUser.setGebruikerID(1L);

        Boek mockBoek = new Boek();
        mockBoek.setTitel("Test Boek voor Leeslijst");

        when(gebruikerRepository.findById(1L)).thenReturn(Optional.of(mockUser));

        mockMvc.perform(get("/api/gebruiker/1/leeslijst"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].titel").value("Test Boek voor Leeslijst"));
    }
}