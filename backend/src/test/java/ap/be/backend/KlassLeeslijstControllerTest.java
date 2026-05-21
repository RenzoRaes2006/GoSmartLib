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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;

import ap.be.backend.dto.KlassLeeslijstRequestDTO;
import ap.be.backend.dto.KlassLeeslijstResponseDTO;
import ap.be.backend.service.KlassLeeslijstService;

class KlassLeeslijstControllerTest extends AbstractIntegrationTest {

    @MockBean
    private KlassLeeslijstService leeslijstService;

    private KlassLeeslijstResponseDTO basicResponse(Long id, String naam) {
        return new KlassLeeslijstResponseDTO(id, naam, null, null, null, null, false, false, List.of());
    }

    @Test
    void createLeeslijst_ReturnsCreatedDTO() throws Exception {
        KlassLeeslijstRequestDTO request = new KlassLeeslijstRequestDTO("Mijn Lijst", null, null, null, null);
        KlassLeeslijstResponseDTO expected = basicResponse(1L, "Mijn Lijst");
        when(leeslijstService.createLeeslijst(any(KlassLeeslijstRequestDTO.class), eq(1L))).thenReturn(expected);

        mockMvc.perform(post("/api/leeslijsten/leerkracht/{id}", 1L)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.naam").value("Mijn Lijst"));
    }

    @Test
    void createLeeslijst_metKlasEnBeschrijving_ReturnsCreatedDTO() throws Exception {
        KlassLeeslijstRequestDTO request = new KlassLeeslijstRequestDTO(
                "Lessen Wiskunde", "5INF1", null, "Boeken voor het rekenenkundig hoofdstuk", true);
        KlassLeeslijstResponseDTO expected = new KlassLeeslijstResponseDTO(
                2L, "Lessen Wiskunde", "5INF1", null, null,
                "Boeken voor het rekenenkundig hoofdstuk", true, false, List.of());
        when(leeslijstService.createLeeslijst(any(KlassLeeslijstRequestDTO.class), eq(7L))).thenReturn(expected);

        mockMvc.perform(post("/api/leeslijsten/leerkracht/{id}", 7L)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.klas").value("5INF1"))
                .andExpect(jsonPath("$.zichtbaar").value(true));
    }

    @Test
    void getLeeslijstenByLeerkracht_ReturnsList() throws Exception {
        Long leerkrachtId = 1L;
        when(leeslijstService.getLeeslijstenByLeerkracht(leerkrachtId)).thenReturn(List.of(
                basicResponse(1L, "Lijst 1")));

        mockMvc.perform(get("/api/leeslijsten/leerkracht/{id}", leerkrachtId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    @WithMockUser(roles = "STUDENT")
    void getLeeslijstenVoorHuidigeLeerling_ReturnsList() throws Exception {
        when(leeslijstService.getLeeslijstenVoorHuidigeLeerling()).thenReturn(List.of(
                new KlassLeeslijstResponseDTO(1L, "Klas-lijst", "5INF1", null, null, null, true, false, List.of()),
                new KlassLeeslijstResponseDTO(2L, "Persoonlijke lijst", null, List.of(42L), List.of("leerling42"), null, true, false, List.of())));

        mockMvc.perform(get("/api/leeslijsten/mijn-leerling-lijsten"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].klas").value("5INF1"))
                .andExpect(jsonPath("$[1].leerlingIds[0]").value(42));
    }

    @Test
    void addBoekToLeeslijst_ReturnsUpdatedDTO() throws Exception {
        Long lijstId = 10L;
        Long boekId = 5L;
        when(leeslijstService.addBoekToLeeslijst(lijstId, boekId))
                .thenReturn(basicResponse(lijstId, "Lijst"));

        mockMvc.perform(post("/api/leeslijsten/{l}/boeken/{b}", lijstId, boekId))
                .andExpect(status().isOk());

        verify(leeslijstService, times(1)).addBoekToLeeslijst(lijstId, boekId);
    }

    @Test
    void getLeeslijstById_ReturnsDTO() throws Exception {
        Long lijstId = 10L;
        when(leeslijstService.getLeeslijstById(lijstId))
                .thenReturn(basicResponse(lijstId, "Lijst"));

        mockMvc.perform(get("/api/leeslijsten/{id}", lijstId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(10));
    }

    @Test
    void removeBoekFromLeeslijst_ReturnsUpdatedDTO() throws Exception {
        Long lijstId = 10L;
        Long boekId = 5L;
        when(leeslijstService.removeBoekFromLeeslijst(lijstId, boekId))
                .thenReturn(basicResponse(lijstId, "Lijst"));

        mockMvc.perform(delete("/api/leeslijsten/{l}/boeken/{b}", lijstId, boekId))
                .andExpect(status().isOk());

        verify(leeslijstService, times(1)).removeBoekFromLeeslijst(lijstId, boekId);
    }

    @Test
    void updateLeeslijstNaam_ReturnsUpdatedDTO() throws Exception {
        Long lijstId = 10L;
        String nieuweNaam = "Nieuwe Naam";
        when(leeslijstService.updateLeeslijstNaam(eq(lijstId), eq(nieuweNaam)))
                .thenReturn(basicResponse(lijstId, nieuweNaam));

        mockMvc.perform(put("/api/leeslijsten/{id}/naam", lijstId)
                .contentType(MediaType.TEXT_PLAIN)
                .content(nieuweNaam))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.naam").value(nieuweNaam));
    }

    @Test
    void updateBeschrijving_ReturnsUpdatedDTO() throws Exception {
        Long lijstId = 10L;
        String nieuweTekst = "Korte uitleg over de opdracht.";
        when(leeslijstService.updateBeschrijving(eq(lijstId), eq(nieuweTekst)))
                .thenReturn(new KlassLeeslijstResponseDTO(
                        lijstId, "Lijst", null, null, null, nieuweTekst, false, false, List.of()));

        mockMvc.perform(put("/api/leeslijsten/{id}/beschrijving", lijstId)
                .contentType(MediaType.TEXT_PLAIN)
                .content(nieuweTekst))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.beschrijving").value(nieuweTekst));
    }

    @Test
    void updateZichtbaar_ReturnsUpdatedDTO() throws Exception {
        Long lijstId = 10L;
        when(leeslijstService.updateZichtbaar(eq(lijstId), eq(true)))
                .thenReturn(new KlassLeeslijstResponseDTO(
                        lijstId, "Lijst", null, null, null, null, true, false, List.of()));

        mockMvc.perform(put("/api/leeslijsten/{id}/zichtbaar", lijstId)
                .contentType(MediaType.APPLICATION_JSON)
                .content("true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.zichtbaar").value(true));
    }

    @Test
    void deleteLeeslijst_ReturnsNoContent() throws Exception {
        Long lijstId = 10L;
        doNothing().when(leeslijstService).deleteLeeslijst(lijstId);

        mockMvc.perform(delete("/api/leeslijsten/{id}", lijstId))
                .andExpect(status().isNoContent());

        verify(leeslijstService, times(1)).deleteLeeslijst(lijstId);
    }

    @Test
    void getAllKlassenLeeslijsten_ReturnsList() throws Exception {
        when(leeslijstService.getAllKlassenLeeslijsten()).thenReturn(List.of(
                basicResponse(1L, "Lijst 1")));

        mockMvc.perform(get("/api/leeslijsten/alle"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }
}
