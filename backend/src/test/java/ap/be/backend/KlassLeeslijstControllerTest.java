package ap.be.backend;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import ap.be.backend.controller.KlassLeeslijstController;
import ap.be.backend.dto.KlassLeeslijstRequestDTO;
import ap.be.backend.dto.KlassLeeslijstResponseDTO;
import ap.be.backend.service.KlassLeeslijstService;

@ExtendWith(MockitoExtension.class)
class KlassLeeslijstControllerTest {

    @Mock
    private KlassLeeslijstService leeslijstService;

    @InjectMocks
    private KlassLeeslijstController leeslijstController;

    /**
     * Test of de controller een HTTP 200 OK retourneert met het aangemaakte DTO
     * nadat de service succesvol een nieuwe leeslijst heeft gegenereerd.
     */
    @Test
    void createLeeslijst_ReturnsCreatedDTO() {
        KlassLeeslijstRequestDTO request = new KlassLeeslijstRequestDTO("Mijn Lijst");
        KlassLeeslijstResponseDTO expected = new KlassLeeslijstResponseDTO(1L, "Mijn Lijst", List.of());

        when(leeslijstService.createLeeslijst("Mijn Lijst", 1L)).thenReturn(expected);

        ResponseEntity<KlassLeeslijstResponseDTO> response = leeslijstController.createLeeslijst(1L, request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(expected, response.getBody());
    }

    /**
     * Test of de controller de correcte lijst met leeslijsten voor een specifieke
     * leerkracht retourneert samen met een HTTP 200 OK status.
     */
    @Test
    void getLeeslijstenByLeerkracht_ReturnsList() {
        Long leerkrachtId = 1L;
        List<KlassLeeslijstResponseDTO> expected = List.of(
                new KlassLeeslijstResponseDTO(1L, "Lijst 1", List.of()));
        when(leeslijstService.getLeeslijstenByLeerkracht(leerkrachtId)).thenReturn(expected);

        ResponseEntity<List<KlassLeeslijstResponseDTO>> response = leeslijstController
                .getLeeslijstenByLeerkracht(leerkrachtId);

        assertEquals(1, response.getBody().size());
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    /**
     * Test of de controller een HTTP 200 OK en de bijgewerkte leeslijst retourneert
     * nadat een boek is toegevoegd.
     */
    @Test
    void addBoekToLeeslijst_ReturnsUpdatedDTO() {
        Long lijstId = 10L;
        Long boekId = 5L;
        KlassLeeslijstResponseDTO expected = new KlassLeeslijstResponseDTO(lijstId, "Lijst", List.of());

        when(leeslijstService.addBoekToLeeslijst(lijstId, boekId)).thenReturn(expected);

        ResponseEntity<KlassLeeslijstResponseDTO> response = leeslijstController.addBoekToLeeslijst(lijstId, boekId);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(leeslijstService, times(1)).addBoekToLeeslijst(lijstId, boekId);
    }

    /**
     * Test of de controller de correcte leeslijst ophaalt op basis van ID
     * en retourneert met een HTTP 200 OK status.
     */
    @Test
    void getLeeslijstById_ReturnsDTO() {
        Long lijstId = 10L;
        KlassLeeslijstResponseDTO expected = new KlassLeeslijstResponseDTO(lijstId, "Lijst", List.of());

        when(leeslijstService.getLeeslijstById(lijstId)).thenReturn(expected);

        ResponseEntity<KlassLeeslijstResponseDTO> response = leeslijstController.getLeeslijstById(lijstId);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(expected, response.getBody()); // Bevestigt dat de controller het juiste DTO teruggeeft[cite: 11]
    }

    /**
     * Test of de controller de bijgewerkte leeslijst retourneert na
     * het succesvol verwijderen van een boek via de service.
     */
    @Test
    void removeBoekFromLeeslijst_ReturnsUpdatedDTO() {
        Long lijstId = 10L;
        Long boekId = 5L;
        KlassLeeslijstResponseDTO expected = new KlassLeeslijstResponseDTO(lijstId, "Lijst", List.of());

        when(leeslijstService.removeBoekFromLeeslijst(lijstId, boekId)).thenReturn(expected);

        ResponseEntity<KlassLeeslijstResponseDTO> response = leeslijstController.removeBoekFromLeeslijst(lijstId,
                boekId);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(leeslijstService, times(1)).removeBoekFromLeeslijst(lijstId, boekId); // Bevestigt dat de HTTP DELETE het
                                                                                     // boek verwijdert via de
                                                                                     // service[cite: 11]
    }

    /**
     * Test of de controller de bijgewerkte leeslijst retourneert
     * na het succesvol aanpassen van de naam.
     */
    @Test
    void updateLeeslijstNaam_ReturnsUpdatedDTO() {
        Long lijstId = 10L;
        String nieuweNaam = "Nieuwe Naam";
        KlassLeeslijstResponseDTO expected = new KlassLeeslijstResponseDTO(lijstId, nieuweNaam, List.of());

        when(leeslijstService.updateLeeslijstNaam(lijstId, nieuweNaam)).thenReturn(expected);

        ResponseEntity<KlassLeeslijstResponseDTO> response = leeslijstController.updateLeeslijstNaam(lijstId,
                nieuweNaam);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(expected, response.getBody()); // Controleert de naam update via de HTTP PUT methode[cite: 11]
    }

    /**
     * Test of de controller een HTTP 204 No Content retourneert
     * wanneer een leeslijst succesvol is verwijderd.
     */
    @Test
    void deleteLeeslijst_ReturnsNoContent() {
        Long lijstId = 10L;
        doNothing().when(leeslijstService).deleteLeeslijst(lijstId);

        ResponseEntity<Void> response = leeslijstController.deleteLeeslijst(lijstId);

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode()); // Bevestigt dat de status 204 No Content is na
                                                                       // een succesvolle HTTP DELETE[cite: 11]
        verify(leeslijstService, times(1)).deleteLeeslijst(lijstId);
    }

    /**
     * Test of de controller een HTTP 200 OK retourneert met de volledige lijst
     * van alle klassenleeslijsten.
     */
    @Test
    void getAllKlassenLeeslijsten_ReturnsList() {
        List<KlassLeeslijstResponseDTO> expected = List.of(
                new KlassLeeslijstResponseDTO(1L, "Lijst 1", List.of()));
        when(leeslijstService.getAllKlassenLeeslijsten()).thenReturn(expected);

        ResponseEntity<List<KlassLeeslijstResponseDTO>> response = leeslijstController.getAllKlassenLeeslijsten();

        assertEquals(1, response.getBody().size());
        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(leeslijstService, times(1)).getAllKlassenLeeslijsten();
    }
}