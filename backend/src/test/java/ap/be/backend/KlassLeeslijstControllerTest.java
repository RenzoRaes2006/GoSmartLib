package ap.be.backend;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;

import ap.be.backend.dto.KlassLeeslijstRequestDTO;
import ap.be.backend.dto.KlassLeeslijstResponseDTO;
import ap.be.backend.service.KlassLeeslijstService;

class KlassLeeslijstControllerTest extends AbstractIntegrationTest {

    @MockBean
    private KlassLeeslijstService leeslijstService;

    @Test
    void createLeeslijst_ReturnsCreatedDTO() throws Exception {
        // Arrange
        KlassLeeslijstRequestDTO request = new KlassLeeslijstRequestDTO("Mijn Lijst");
        KlassLeeslijstResponseDTO expected = new KlassLeeslijstResponseDTO(1L, "Mijn Lijst", List.of());
        when(leeslijstService.createLeeslijst("Mijn Lijst", 1L)).thenReturn(expected);

        // Act + Assert
        mockMvc.perform(post("/api/leeslijsten/leerkracht/{id}", 1L)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.naam").value("Mijn Lijst"));
    }

    @Test
    void getLeeslijstenByLeerkracht_ReturnsList() throws Exception {
        // Arrange
        Long leerkrachtId = 1L;
        when(leeslijstService.getLeeslijstenByLeerkracht(leerkrachtId)).thenReturn(List.of(
                new KlassLeeslijstResponseDTO(1L, "Lijst 1", List.of())));

        // Act + Assert
        mockMvc.perform(get("/api/leeslijsten/leerkracht/{id}", leerkrachtId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    void addBoekToLeeslijst_ReturnsUpdatedDTO() throws Exception {
        // Arrange
        Long lijstId = 10L;
        Long boekId = 5L;
        when(leeslijstService.addBoekToLeeslijst(lijstId, boekId))
                .thenReturn(new KlassLeeslijstResponseDTO(lijstId, "Lijst", List.of()));

        // Act + Assert
        mockMvc.perform(post("/api/leeslijsten/{l}/boeken/{b}", lijstId, boekId))
                .andExpect(status().isOk());

        verify(leeslijstService, times(1)).addBoekToLeeslijst(lijstId, boekId);
    }

    @Test
    void getLeeslijstById_ReturnsDTO() throws Exception {
        // Arrange
        Long lijstId = 10L;
        when(leeslijstService.getLeeslijstById(lijstId))
                .thenReturn(new KlassLeeslijstResponseDTO(lijstId, "Lijst", List.of()));

        // Act + Assert
        mockMvc.perform(get("/api/leeslijsten/{id}", lijstId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(10));
    }

    @Test
    void removeBoekFromLeeslijst_ReturnsUpdatedDTO() throws Exception {
        // Arrange
        Long lijstId = 10L;
        Long boekId = 5L;
        when(leeslijstService.removeBoekFromLeeslijst(lijstId, boekId))
                .thenReturn(new KlassLeeslijstResponseDTO(lijstId, "Lijst", List.of()));

        // Act + Assert
        mockMvc.perform(delete("/api/leeslijsten/{l}/boeken/{b}", lijstId, boekId))
                .andExpect(status().isOk());

        verify(leeslijstService, times(1)).removeBoekFromLeeslijst(lijstId, boekId);
    }

    @Test
    void updateLeeslijstNaam_ReturnsUpdatedDTO() throws Exception {
        // Arrange
        Long lijstId = 10L;
        String nieuweNaam = "Nieuwe Naam";
        when(leeslijstService.updateLeeslijstNaam(eq(lijstId), eq(nieuweNaam)))
                .thenReturn(new KlassLeeslijstResponseDTO(lijstId, nieuweNaam, List.of()));

        // Act + Assert
        mockMvc.perform(put("/api/leeslijsten/{id}/naam", lijstId)
                .contentType(MediaType.TEXT_PLAIN)
                .content(nieuweNaam))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.naam").value(nieuweNaam));
    }

    @Test
    void deleteLeeslijst_ReturnsNoContent() throws Exception {
        // Arrange
        Long lijstId = 10L;
        doNothing().when(leeslijstService).deleteLeeslijst(lijstId);

        // Act + Assert
        mockMvc.perform(delete("/api/leeslijsten/{id}", lijstId))
                .andExpect(status().isNoContent());

        verify(leeslijstService, times(1)).deleteLeeslijst(lijstId);
    }

    @Test
    void getAllKlassenLeeslijsten_ReturnsList() throws Exception {
        // Arrange
        when(leeslijstService.getAllKlassenLeeslijsten()).thenReturn(List.of(
                new KlassLeeslijstResponseDTO(1L, "Lijst 1", List.of())));

        // Act + Assert
        mockMvc.perform(get("/api/leeslijsten/alle"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }
}
