package ap.be.backend;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;

import ap.be.backend.controller.ScanController;
import ap.be.backend.dto.ScanBatchItemResultDTO;
import ap.be.backend.dto.ScanBatchRequestDTO;
import ap.be.backend.dto.ScanBatchResponseDTO;
import ap.be.backend.service.ScanService;

@WebMvcTest(ScanController.class)
public class ScanControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ScanService scanService;

    private ScanBatchRequestDTO buildRequest(String actie, String... isbns) {
        ScanBatchRequestDTO req = new ScanBatchRequestDTO();
        req.setGebruikerId(1L);
        req.setActie(actie);
        req.setIsbns(List.of(isbns));
        return req;
    }

    @Test
    public void givenGeldigeUitlening_whenPostBatch_thenReturnOkAndResponse() throws Exception {
        ScanBatchRequestDTO request = buildRequest("UITLENING", "9780132350884");
        ScanBatchResponseDTO response = new ScanBatchResponseDTO("UITLENING",
                List.of(new ScanBatchItemResultDTO("9780132350884", "Clean Code", "OK", null)));

        given(scanService.verwerkBatch(any(ScanBatchRequestDTO.class))).willReturn(response);

        mockMvc.perform(post("/api/scan/batch")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.actie").value("UITLENING"))
                .andExpect(jsonPath("$.resultaten[0].isbn").value("9780132350884"))
                .andExpect(jsonPath("$.resultaten[0].titel").value("Clean Code"))
                .andExpect(jsonPath("$.resultaten[0].status").value("OK"));

        verify(scanService, times(1)).verwerkBatch(any(ScanBatchRequestDTO.class));
    }

    @Test
    public void givenRetourMetFouten_whenPostBatch_thenReturnOkWithItemFouten() throws Exception {
        ScanBatchRequestDTO request = buildRequest("RETOUR", "0000000000000");
        ScanBatchResponseDTO response = new ScanBatchResponseDTO("RETOUR",
                List.of(new ScanBatchItemResultDTO("0000000000000", null, "FOUT", "Boek niet gevonden.")));

        given(scanService.verwerkBatch(any(ScanBatchRequestDTO.class))).willReturn(response);

        mockMvc.perform(post("/api/scan/batch")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.actie").value("RETOUR"))
                .andExpect(jsonPath("$.resultaten[0].status").value("FOUT"))
                .andExpect(jsonPath("$.resultaten[0].foutReden").value("Boek niet gevonden."));
    }

    @Test
    public void givenLegeIsbnLijst_whenPostBatch_thenReturnOkAndLegeResultaten() throws Exception {
        ScanBatchRequestDTO request = buildRequest("UITLENING");
        ScanBatchResponseDTO response = new ScanBatchResponseDTO("UITLENING", List.of());

        given(scanService.verwerkBatch(any(ScanBatchRequestDTO.class))).willReturn(response);

        mockMvc.perform(post("/api/scan/batch")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.actie").value("UITLENING"))
                .andExpect(jsonPath("$.resultaten").isArray())
                .andExpect(jsonPath("$.resultaten").isEmpty());
    }
}
