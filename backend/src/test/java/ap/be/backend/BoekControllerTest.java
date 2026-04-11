package ap.be.backend;

import ap.be.backend.controller.BoekController;
import ap.be.backend.dto.BoekDTO;
import ap.be.backend.entity.Boek;
import ap.be.backend.repository.BoekRepository;
import ap.be.backend.service.BoekImportService;
import ap.be.backend.service.BoekService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Sort;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.mock.web.MockMultipartFile;
import ap.be.backend.exception.IsbnAlreadyExistsException;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(BoekController.class)
public class BoekControllerTest {

        @Autowired
        private MockMvc mockMvc;
        @MockBean
        private BoekImportService boekImportService;
        @MockBean
        private BoekRepository boekRepository;

        @MockBean
        private BoekService boekService;

        @Test
        public void givenSearchTerm_whenGetAlleBoekenCalled_thenCallsFilterBoekenInService() throws Exception {
                // Given
                String term = "java";
                Mockito.when(boekService.filterBoeken(eq(term), any(), any(), any(), any(),
                                any(), any(), any(), any(), any()))
                                .thenReturn(List.of(new Boek()));

                // When & Then
                mockMvc.perform(get("/api/boeken").param("zoekterm", term))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$").isArray());

                Mockito.verify(boekService).filterBoeken(eq(term), any(), any(), any(), any(),
                                any(), any(), any(), any(), any());
        }

        @Test
        public void givenNoSearchTerm_whenGetAlleBoekenCalled_thenCallsFilterBoekenInService() throws Exception {
                // Given
                Mockito.when(boekService.filterBoeken(any(), any(), any(), any(), any(),
                                any(), any(), any(), any(), any()))
                                .thenReturn(List.of());

                // When & Then
                mockMvc.perform(get("/api/boeken"))
                                .andExpect(status().isOk());

                Mockito.verify(boekService).filterBoeken(any(), any(), any(), any(), any(),
                                any(), any(), any(), any(), any());
        }

        // --- POST: BOEK TOEVOEGEN ---

        @Test
        public void givenGeldigBoek_whenPostBoek_thenStatus201EnBoekTerug() throws Exception {
                // Given
                Boek opgeslagen = new Boek("Clean Code", "Robert Martin");
                opgeslagen.setId(1L);
                opgeslagen.setIsbn("9780132350884");
                Mockito.when(boekService.addBoek(any(BoekDTO.class))).thenReturn(opgeslagen);

                String json = """
                                {
                                    "titel": "Clean Code",
                                    "auteur": "Robert Martin",
                                    "isbn": "9780132350884",
                                    "uitgever": "Prentice Hall",
                                    "uitgaveJaar": "2008",
                                    "pagina": 464,
                                    "genre": ["Software"],
                                    "beschrijving": "A handbook of agile software craftsmanship",
                                    "aantalExemplaren": 3,
                                    "locatie": "A1",
                                    "afbeelding": ""
                                }
                                """;

                // When & Then
                mockMvc.perform(post("/api/boeken")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(json))
                                .andExpect(status().isCreated())
                                .andExpect(jsonPath("$.titel").value("Clean Code"))
                                .andExpect(jsonPath("$.auteur").value("Robert Martin"));
        }

        @Test
        public void givenOngeldigBoekZonderTitel_whenPostBoek_thenStatus400() throws Exception {
                // Given - titel ontbreekt
                String json = """
                                {
                                    "titel": "",
                                    "auteur": "Auteur",
                                    "isbn": "9780132350884",
                                    "uitgever": "Uitgever",
                                    "uitgaveJaar": "2020",
                                    "pagina": 100,
                                    "genre": ["Fictie"],
                                    "beschrijving": "Beschrijving",
                                    "aantalExemplaren": 1,
                                    "locatie": "A1",
                                    "afbeelding": ""
                                }
                                """;

                // When & Then
                mockMvc.perform(post("/api/boeken")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(json))
                                .andExpect(status().isBadRequest());
        }

        @Test
        public void givenOngeldigIsbn_whenPostBoek_thenStatus400() throws Exception {
                // Given - ISBN is geen 10 of 13 cijfers
                String json = """
                                {
                                    "titel": "Test Boek",
                                    "auteur": "Auteur",
                                    "isbn": "12345",
                                    "uitgever": "Uitgever",
                                    "uitgaveJaar": "2020",
                                    "pagina": 100,
                                    "genre": ["Fictie"],
                                    "beschrijving": "Beschrijving",
                                    "aantalExemplaren": 1,
                                    "locatie": "A1",
                                    "afbeelding": ""
                                }
                                """;

                // When & Then
                mockMvc.perform(post("/api/boeken")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(json))
                                .andExpect(status().isBadRequest());
        }

        // --- PUT: BOEK WIJZIGEN ---

        @Test
        public void givenBestaandBoek_whenPutBoek_thenStatus200EnBijgewerktBoek() throws Exception {
                // Given
                Boek bijgewerkt = new Boek("Bijgewerkte Titel", "Nieuwe Auteur");
                bijgewerkt.setId(5L);
                Mockito.when(boekService.updateBoek(eq(5L), any(BoekDTO.class))).thenReturn(bijgewerkt);

                String json = """
                                {
                                    "titel": "Bijgewerkte Titel",
                                    "auteur": "Nieuwe Auteur",
                                    "isbn": "9781234567890",
                                    "uitgever": "Uitgever",
                                    "uitgaveJaar": "2023",
                                    "pagina": 300,
                                    "genre": ["Fictie"],
                                    "beschrijving": "Bijgewerkte beschrijving",
                                    "aantalExemplaren": 5,
                                    "locatie": "B2",
                                    "afbeelding": ""
                                }
                                """;

                // When & Then
                mockMvc.perform(put("/api/boeken/5")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(json))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.titel").value("Bijgewerkte Titel"));
        }

        @Test
        public void givenNietBestaandBoek_whenPutBoek_thenStatus404() throws Exception {
                // Given
                Mockito.when(boekService.updateBoek(eq(999L), any(BoekDTO.class))).thenReturn(null);

                String json = """
                                {
                                    "titel": "Test",
                                    "auteur": "Test",
                                    "isbn": "9781234567890",
                                    "uitgever": "Test",
                                    "uitgaveJaar": "2023",
                                    "pagina": 100,
                                    "genre": ["Fictie"],
                                    "beschrijving": "Beschrijving",
                                    "aantalExemplaren": 1,
                                    "locatie": "A1",
                                    "afbeelding": ""
                                }
                                """;

                // When & Then
                mockMvc.perform(put("/api/boeken/999")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(json))
                                .andExpect(status().isNotFound());
        }

        // --- DELETE: BOEK VERWIJDEREN ---

        @Test
        public void givenBestaandBoek_whenDeleteBoek_thenStatus200() throws Exception {
                // Given
                Mockito.when(boekService.deleteBoek(10L)).thenReturn(true);

                // When & Then
                mockMvc.perform(delete("/api/boeken/10"))
                                .andExpect(status().isOk())
                                .andExpect(content().string("Boek is verwijderd."));
        }

        @Test
        public void givenNietBestaandBoek_whenDeleteBoek_thenStatus404() throws Exception {
                // Given
                Mockito.when(boekService.deleteBoek(999L)).thenReturn(false);

                // When & Then
                mockMvc.perform(delete("/api/boeken/999"))
                                .andExpect(status().isNotFound());
        }

        // --- PAGED: BOEKENOVERZICHT MET PAGINERING ---

        @Test
        public void givenBoeken_whenGetBoekenPaged_thenStatus200EnPaginaData() throws Exception {
                // Given
                Boek boek = new Boek("Test Boek", "Auteur");
                Page<Boek> page = new PageImpl<>(List.of(boek));
                Mockito.when(boekService.getBoekenPaged(0, 6)).thenReturn(page);

                // When & Then
                mockMvc.perform(get("/api/boeken/paged")
                                .param("page", "0")
                                .param("size", "6"))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.content[0].titel").value("Test Boek"))
                                .andExpect(jsonPath("$.totalElements").value(1));
        }

        @Test
        public void givenGeenBoeken_whenGetBoekenPaged_thenStatus200EnLegePagina() throws Exception {
                // Given
                Page<Boek> page = new PageImpl<>(List.of());
                Mockito.when(boekService.getBoekenPaged(0, 6)).thenReturn(page);

                // When & Then
                mockMvc.perform(get("/api/boeken/paged")
                                .param("page", "0")
                                .param("size", "6"))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.content").isArray())
                                .andExpect(jsonPath("$.content.length()").value(0));
        }

        // --- ISBN LOOKUP ---

        @Test
        public void givenBestaandIsbn_whenGetBoekByIsbn_thenStatus200EnBoek() throws Exception {
                // Given
                Boek boek = new Boek("Clean Code", "Robert Martin");
                boek.setIsbn("9780132350884");
                Mockito.when(boekService.getBoekByIsbn("9780132350884")).thenReturn(boek);

                // When & Then
                mockMvc.perform(get("/api/boeken/isbn/9780132350884"))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.isbn").value("9780132350884"))
                                .andExpect(jsonPath("$.titel").value("Clean Code"));
        }

        @Test
        public void givenNietBestaandIsbn_whenGetBoekByIsbn_thenStatus404() throws Exception {
                // Given
                Mockito.when(boekService.getBoekByIsbn("0000000000000")).thenReturn(null);

                // When & Then
                mockMvc.perform(get("/api/boeken/isbn/0000000000000"))
                                .andExpect(status().isNotFound());
        }
        // --- POST: BOEKEN IMPORTEREN (EXCEL) ---

        @Test
        public void givenGeldigExcelBestand_whenImporteerBoeken_thenStatus200() throws Exception {
                // Given: Een gesimuleerd Excel bestand
                MockMultipartFile file = new MockMultipartFile(
                                "file",
                                "boeken.xlsx",
                                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                                "dummy data".getBytes());

                // We simuleren dat de service succesvol 2 boeken opslaat
                Mockito.when(boekImportService.importeerBestand(any())).thenReturn(2);

                // When & Then: We verwachten een 200 OK en de juiste tekst
                mockMvc.perform(multipart("/api/boeken/import").file(file))
                                .andExpect(status().isOk())
                                .andExpect(content().string("Aantal geïmporteerde boeken: 2"));
        }

        @Test
        public void givenLeegBestand_whenImporteerBoeken_thenStatus400() throws Exception {
                // Given: Een leeg bestand (0 bytes)
                MockMultipartFile leegBestand = new MockMultipartFile(
                                "file",
                                "leeg.xlsx",
                                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                                new byte[0]);

                // When & Then: De controller gooit een IllegalArgumentException.
                // De GlobalExceptionHandler vangt dit op en maakt er een 400 Bad Request van.
                mockMvc.perform(multipart("/api/boeken/import").file(leegBestand))
                                .andExpect(status().isBadRequest())
                                .andExpect(content().string("Het geüploade bestand is leeg."));
        }

        @Test
        public void givenBestandMetDubbelIsbn_whenImporteerBoeken_thenStatus409() throws Exception {
                // Given: Een bestand met data
                MockMultipartFile file = new MockMultipartFile(
                                "file",
                                "boeken.xlsx",
                                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                                "dummy data met dubbel isbn".getBytes());

                // We simuleren dat de service een IsbnAlreadyExistsException gooit
                Mockito.when(boekImportService.importeerBestand(any()))
                                .thenThrow(new IsbnAlreadyExistsException(
                                                "ISBN 9780132350884 bestaat al in de database."));

                // When & Then: De GlobalExceptionHandler grijpt in en maakt er een 409 Conflict
                // van.
                mockMvc.perform(multipart("/api/boeken/import").file(file))
                                .andExpect(status().isConflict())
                                .andExpect(content().string("ISBN 9780132350884 bestaat al in de database."));
        }
}