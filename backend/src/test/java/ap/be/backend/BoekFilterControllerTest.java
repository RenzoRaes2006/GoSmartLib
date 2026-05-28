package ap.be.backend;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageImpl;

import ap.be.backend.dto.BoekDTO;
import ap.be.backend.dto.BoekenFilters;
import ap.be.backend.dto.BoekenSort;
import ap.be.backend.service.BoekService;

public class BoekFilterControllerTest extends AbstractIntegrationTest {

        @MockBean
        private BoekService boekService;

        private BoekDTO dto(String titel, String auteur) {
                return new BoekDTO(1L, titel, auteur, "1234567890", "Uitg", "2020", 100,
                                List.of(), "Beschrijving", 1, "Loc", "", null, null, false, null, null, false);
        }

        private BoekenFilters captureFilters() {
                ArgumentCaptor<BoekenFilters> cap = ArgumentCaptor.forClass(BoekenFilters.class);
                verify(boekService).filterBoeken(cap.capture(), any(), any(), any());
                return cap.getValue();
        }

        private BoekenSort captureSort() {
                ArgumentCaptor<BoekenSort> cap = ArgumentCaptor.forClass(BoekenSort.class);
                verify(boekService).filterBoeken(any(), cap.capture(), any(), any());
                return cap.getValue();
        }

        @Test
        void givenGeenParameters_whenGetAlleBoeken_thenStatus200EnLegeArray() throws Exception {
                when(boekService.filterBoeken(any(), any(), any(), any())).thenReturn(new PageImpl<>(List.of()));

                mockMvc.perform(get("/api/boeken"))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.content").isArray());
        }

        @Test
        void givenZoekterm_whenGetAlleBoeken_thenZoektermWordtDoorgegevenAanService() throws Exception {
                when(boekService.filterBoeken(any(), any(), any(), any()))
                                .thenReturn(new PageImpl<>(List.of(dto("De Alchemist", "Paulo Coelho"))));

                mockMvc.perform(get("/api/boeken").param("zoekterm", "alchemist"))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.content[0].titel").value("De Alchemist"));

                assertEquals("alchemist", captureFilters().zoekterm());
        }

        @Test
        void givenEnkelTagIdParam_whenGetAlleBoeken_thenTagIdWordtAlsLijstDoorgegevenAanService() throws Exception {
                when(boekService.filterBoeken(any(), any(), any(), any()))
                                .thenReturn(new PageImpl<>(List.of(dto("Socrates", "Plato"))));

                mockMvc.perform(get("/api/boeken").param("tagIds", "42"))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.content").isArray());

                assertEquals(List.of(42L), captureFilters().tagIds());
        }

        @Test
        void givenMeerdereTagIdParams_whenGetAlleBoeken_thenAlleTagIdsWordenVerzameldInLijst() throws Exception {
                when(boekService.filterBoeken(any(), any(), any(), any()))
                                .thenReturn(new PageImpl<>(List.of(dto("Sapiens", "Harari"))));

                mockMvc.perform(get("/api/boeken")
                                .param("tagIds", "1")
                                .param("tagIds", "2")
                                .param("tagIds", "3"))
                                .andExpect(status().isOk());

                assertEquals(List.of(1L, 2L, 3L), captureFilters().tagIds());
        }

        @Test
        void givenLeesniveauParam_whenGetAlleBoeken_thenLeesniveauWordtDoorgegevenAanService() throws Exception {
                BoekDTO boek = new BoekDTO(1L, "Eenvoudig Boek", "Auteur", "1234567890", "U", "2020", 50,
                                List.of(), "B", 1, "L", "", null, "A (eenvoudig taalgebruik)", false, null, null, false);
                when(boekService.filterBoeken(any(), any(), any(), any()))
                                .thenReturn(new PageImpl<>(List.of(boek)));

                mockMvc.perform(get("/api/boeken").param("leesniveau", "A (eenvoudig taalgebruik)"))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.content[0].leesniveau").value("A (eenvoudig taalgebruik)"));

                assertEquals("A (eenvoudig taalgebruik)", captureFilters().leesniveau());
        }

        @Test
        void givenBeschikbaarTrueParam_whenGetAlleBoeken_thenBeschikbaarheidWordtAlsBooleanDoorgegevenAanService()
                        throws Exception {
                when(boekService.filterBoeken(any(), any(), any(), any()))
                                .thenReturn(new PageImpl<>(List.of(dto("Beschikbaar Boek", "Auteur"))));

                mockMvc.perform(get("/api/boeken").param("beschikbaar", "true"))
                                .andExpect(status().isOk());

                assertEquals(Boolean.TRUE, captureFilters().beschikbaarAsBoolean());
        }

        @Test
        void givenBeschikbaarAfwezig_whenGetAlleBoeken_thenBeschikbaarheidIsNullInService() throws Exception {
                when(boekService.filterBoeken(any(), any(), any(), any()))
                                .thenReturn(new PageImpl<>(List.of()));

                mockMvc.perform(get("/api/boeken"))
                                .andExpect(status().isOk());

                assertNull(captureFilters().beschikbaarAsBoolean());
        }

        @Test
        void givenSorteerOpEnRichting_whenGetAlleBoeken_thenSortWordtJuistMeegegeven() throws Exception {
                when(boekService.filterBoeken(any(), any(), any(), any())).thenReturn(new PageImpl<>(List.of()));

                mockMvc.perform(get("/api/boeken")
                                .param("sorteerOp", "titel")
                                .param("sorteerRichting", "DESC"))
                                .andExpect(status().isOk());

                BoekenSort sort = captureSort();
                assertEquals("titel", sort.sorteerOp());
                assertEquals("DESC", sort.sorteerRichting());
                assertNotNull(sort.toSort().getOrderFor("titel"));
                assertTrue(sort.toSort().getOrderFor("titel").isDescending());
        }

        @Test
        void givenZoektermOpAuteur_whenGetAlleBoeken_thenGeeftMatchendeBoeken() throws Exception {
                when(boekService.filterBoeken(any(), any(), any(), any()))
                                .thenReturn(new PageImpl<>(List.of(dto("De Alchemist", "Paulo Coelho"))));

                mockMvc.perform(get("/api/boeken").param("zoekterm", "coelho"))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.content[0].auteur").value("Paulo Coelho"));

                assertEquals("coelho", captureFilters().zoekterm());
        }

        @Test
        void givenZoektermOpIsbn_whenGetAlleBoeken_thenGeeftMatchendeBoeken() throws Exception {
                BoekDTO boek = new BoekDTO(1L, "Clean Code", "Robert Martin", "9780132350884", "U", "2020", 100,
                                List.of(), "B", 1, "L", "", null, null, false, null, null, false);
                when(boekService.filterBoeken(any(), any(), any(), any()))
                                .thenReturn(new PageImpl<>(List.of(boek)));

                mockMvc.perform(get("/api/boeken").param("zoekterm", "9780132350884"))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.content[0].isbn").value("9780132350884"));
        }

        @Test
        void givenZoektermZonderResultaat_whenGetAlleBoeken_thenGeeftLegeArray() throws Exception {
                when(boekService.filterBoeken(any(), any(), any(), any())).thenReturn(new PageImpl<>(List.of()));

                mockMvc.perform(get("/api/boeken").param("zoekterm", "xyzbestaatnietzzzz"))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.content").isArray())
                                .andExpect(jsonPath("$.content.length()").value(0));
        }

        @Test
        void givenZoektermMetMeerdereMatches_whenGetAlleBoeken_thenGeeftAlleMatchendeBoeken() throws Exception {
                when(boekService.filterBoeken(any(), any(), any(), any()))
                                .thenReturn(new PageImpl<>(List.of(
                                                dto("Effective Java", "Joshua Bloch"),
                                                dto("Java Concurrency", "Brian Goetz"))));

                mockMvc.perform(get("/api/boeken").param("zoekterm", "java"))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.content.length()").value(2))
                                .andExpect(jsonPath("$.content[0].titel").value("Effective Java"))
                                .andExpect(jsonPath("$.content[1].titel").value("Java Concurrency"));
        }

        @Test
        void givenMinEnMaxPagina_whenGetAlleBoeken_thenPaginaFiltersWordenDoorgegevenAanService() throws Exception {
                when(boekService.filterBoeken(any(), any(), any(), any()))
                                .thenReturn(new PageImpl<>(List.of(dto("Kort Boek", "Auteur"))));

                mockMvc.perform(get("/api/boeken")
                                .param("minPagina", "100")
                                .param("maxPagina", "300"))
                                .andExpect(status().isOk());

                BoekenFilters f = captureFilters();
                assertEquals(100, f.minPagina());
                assertEquals(300, f.maxPagina());
        }

        @Test
        void givenDidactischMateriaalTrue_whenGetAlleBoeken_thenFiltertOpDidactischMateriaal() throws Exception {
                when(boekService.filterBoeken(any(), any(), any(), any()))
                                .thenReturn(new PageImpl<>(List.of(dto("Lesmethode Wiskunde", "Auteur"))));

                mockMvc.perform(get("/api/boeken").param("didactischMateriaal", "true"))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.content[0].titel").value("Lesmethode Wiskunde"));

                assertEquals(Boolean.TRUE, captureFilters().didactischMateriaal());
        }
}
