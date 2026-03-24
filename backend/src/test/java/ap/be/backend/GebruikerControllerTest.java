package ap.be.backend; 

import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import ap.be.backend.controller.GebruikerController;
import ap.be.backend.repository.GebruikerRepository;
import ap.be.backend.repository.LeeslijstMapRepository;
import ap.be.backend.service.GebruikerService;
import ap.be.backend.entity.Gebruiker;
import ap.be.backend.entity.LeeslijstMap;
import ap.be.backend.entity.Uitlening;
import ap.be.backend.entity.Boek;

@WebMvcTest(GebruikerController.class)
public class GebruikerControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private GebruikerRepository gebruikerRepository;

    @MockBean
    private GebruikerService gebruikerService;

    @MockBean
    private LeeslijstMapRepository leeslijstMapRepository;

   @Test
    public void givenGebruikerMetLeeslijst_whenGetLeeslijst_thenReturnStatusOkEnBoeken() throws Exception {
        // initialize mock user
        Gebruiker mockUser = new Gebruiker();
        mockUser.setGebruikerID(1L);

        // initialize mock book
        Boek mockBoek = new Boek();
        mockBoek.setTitel("Test Boek voor Leeslijst");

        // create a map and link the book to it
        LeeslijstMap mockMap = new LeeslijstMap();
        mockMap.setNaam("Favorieten");
        mockMap.getBoeken().add(mockBoek); 

        // mock the repository to return the map containing the book
        when(leeslijstMapRepository.findByGebruiker_GebruikerID(1L)).thenReturn(java.util.Arrays.asList(mockMap));

        // perform request (make sure this URL matches your actual controller!)
        mockMvc.perform(get("/api/gebruiker/1/mappen"))
                .andDo(print())
                .andExpect(status().isOk())
                // check if the first map's first book has the correct title
                .andExpect(jsonPath("$[0].boeken[0].titel").value("Test Boek voor Leeslijst")); 
    }

    @Test
    public void givenUserWithActiveLoans_whenGetActieveLeningen_thenReturnOkAndJsonArray() throws Exception {
        // given
        Long userId = 1L;
        Uitlening dummyLoan = new Uitlening();
        dummyLoan.setId(100L);
        dummyLoan.setStatus("GOEDGEKEURD");
        List<Uitlening> expectedLoans = Arrays.asList(dummyLoan);
        
        given(gebruikerService.getActieveLeningenVanGebruiker(userId)).willReturn(expectedLoans);

        // when & then
        mockMvc.perform(get("/api/gebruiker/1/leningen"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(100))
                .andExpect(jsonPath("$[0].status").value("GOEDGEKEURD"));

        then(gebruikerService).should(times(1)).getActieveLeningenVanGebruiker(userId);
    }

    @Test
    public void givenUserWithNoLoans_whenGetActieveLeningen_thenReturnOkAndEmptyArray() throws Exception {
        // given
        Long userId = 1L;
        given(gebruikerService.getActieveLeningenVanGebruiker(userId)).willReturn(Collections.emptyList());

        // when & then
        mockMvc.perform(get("/api/gebruiker/1/leningen"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isEmpty());

        then(gebruikerService).should(times(1)).getActieveLeningenVanGebruiker(userId);
    }

    @Test
    public void givenUserWithFolders_whenGetAlleMappenVanGebruiker_thenReturnOkAndJsonArray() throws Exception {
        // given
        Long userId = 1L;
        LeeslijstMap dummyFolder = new LeeslijstMap();
        dummyFolder.setId(5L);
        dummyFolder.setNaam("Favorieten");
        List<LeeslijstMap> expectedFolders = Arrays.asList(dummyFolder);

        given(leeslijstMapRepository.findByGebruiker_GebruikerID(userId)).willReturn(expectedFolders);

        // when & then
        mockMvc.perform(get("/api/gebruiker/1/mappen"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(5))
                .andExpect(jsonPath("$[0].naam").value("Favorieten"));

        then(leeslijstMapRepository).should(times(1)).findByGebruiker_GebruikerID(userId);
    }
}