package ap.be.backend;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;

import ap.be.backend.dto.AdminStatistiekDTO;
import ap.be.backend.dto.LeerkrachtStatistiekDTO;
import ap.be.backend.dto.LeerlingStatistiekDTO;
import ap.be.backend.service.StatistiekService;

class StatistiekControllerTest extends AbstractIntegrationTest {

    @MockBean
    private StatistiekService statistiekService;

    @Test
    void getAdminStatistieken_GeeftDtoVanService() throws Exception {
        // Arrange
        AdminStatistiekDTO expected = new AdminStatistiekDTO(25.0, 4, "Fantasy", 12);
        when(statistiekService.getAdminStatistieken()).thenReturn(expected);

        // Act + Assert
        mockMvc.perform(get("/api/statistieken/admin"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.populairsteGenre").value("Fantasy"));

        verify(statistiekService, times(1)).getAdminStatistieken();
    }

    @Test
    void getLeerkrachtStatistieken_MetKlasIdGeeftDtoVanService() throws Exception {
        // Arrange
        String klasId = "5A";
        LeerkrachtStatistiekDTO expected = new LeerkrachtStatistiekDTO(22, 8, 2, 5, List.of("Boek A"));
        when(statistiekService.getLeerkrachtStatistieken(klasId)).thenReturn(expected);

        // Act + Assert
        mockMvc.perform(get("/api/statistieken/leerkracht").param("klasId", klasId))
                .andExpect(status().isOk());

        verify(statistiekService, times(1)).getLeerkrachtStatistieken(klasId);
    }

    @Test
    void getLeerkrachtStatistieken_ZonderKlasIdGeeftDtoVanService() throws Exception {
        // Arrange
        LeerkrachtStatistiekDTO expected = new LeerkrachtStatistiekDTO(150, 40, 6, 20, List.of());
        when(statistiekService.getLeerkrachtStatistieken(null)).thenReturn(expected);

        // Act + Assert
        mockMvc.perform(get("/api/statistieken/leerkracht"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.aantalLeerlingen").value(150));

        verify(statistiekService, times(1)).getLeerkrachtStatistieken(null);
    }

    @Test
    void getLeerlingStatistieken_GeeftDtoVanService() throws Exception {
        // Arrange
        String schoolId = "12345";
        LeerlingStatistiekDTO expected = new LeerlingStatistiekDTO(11, 10, 2, 1);
        when(statistiekService.getLeerlingStatistieken(schoolId)).thenReturn(expected);

        // Act + Assert
        mockMvc.perform(get("/api/statistieken/leerling/{id}", schoolId))
                .andExpect(status().isOk());

        verify(statistiekService, times(1)).getLeerlingStatistieken(schoolId);
    }
}
