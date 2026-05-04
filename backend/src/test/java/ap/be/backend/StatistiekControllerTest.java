package ap.be.backend;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import ap.be.backend.controller.StatistiekController;
import ap.be.backend.dto.AdminStatistiekDTO;
import ap.be.backend.dto.LeerkrachtStatistiekDTO;
import ap.be.backend.dto.LeerlingStatistiekDTO;
import ap.be.backend.service.StatistiekService;

@ExtendWith(MockitoExtension.class)
class StatistiekControllerTest {

    @Mock
    private StatistiekService statistiekService;

    @InjectMocks
    private StatistiekController statistiekController;

    @Test
    void getAdminStatistieken_GeeftDtoVanService() {
        AdminStatistiekDTO expected = new AdminStatistiekDTO(25.0, 4, "Fantasy", 12);
        when(statistiekService.getAdminStatistieken()).thenReturn(expected);

        ResponseEntity<AdminStatistiekDTO> response = statistiekController.getAdminStatistieken();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(expected, response.getBody());
        verify(statistiekService, times(1)).getAdminStatistieken();
    }

    @Test
    void getLeerkrachtStatistieken_MetKlasIdGeeftDtoVanService() {
        String klasId = "5A";
        LeerkrachtStatistiekDTO expected = new LeerkrachtStatistiekDTO(22, 8, 2, 5, List.of("Boek A"));
        when(statistiekService.getLeerkrachtStatistieken(klasId)).thenReturn(expected);

        ResponseEntity<LeerkrachtStatistiekDTO> response = statistiekController.getLeerkrachtStatistieken(klasId);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(expected, response.getBody());
        verify(statistiekService, times(1)).getLeerkrachtStatistieken(klasId);
    }

    @Test
    void getLeerkrachtStatistieken_ZonderKlasIdGeeftDtoVanService() {
        LeerkrachtStatistiekDTO expected = new LeerkrachtStatistiekDTO(150, 40, 6, 20, List.of());
        when(statistiekService.getLeerkrachtStatistieken(null)).thenReturn(expected);

        ResponseEntity<LeerkrachtStatistiekDTO> response = statistiekController.getLeerkrachtStatistieken(null);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(150, response.getBody().getAantalLeerlingen());
        verify(statistiekService, times(1)).getLeerkrachtStatistieken(null);
    }

    @Test
    void getLeerlingStatistieken_GeeftDtoVanService() {
        String schoolId = "12345";
        LeerlingStatistiekDTO expected = new LeerlingStatistiekDTO(11, 10, 2, 1);
        when(statistiekService.getLeerlingStatistieken(schoolId)).thenReturn(expected);

        ResponseEntity<LeerlingStatistiekDTO> response = statistiekController.getLeerlingStatistieken(schoolId);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(expected, response.getBody());
        verify(statistiekService, times(1)).getLeerlingStatistieken(schoolId);
    }
}
