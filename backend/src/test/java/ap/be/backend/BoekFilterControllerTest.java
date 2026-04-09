package ap.be.backend;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Sort;
import org.springframework.test.web.servlet.MockMvc;

import ap.be.backend.controller.BoekController;
import ap.be.backend.entity.Boek;
import ap.be.backend.repository.BoekRepository;
import ap.be.backend.service.BoekService;

@WebMvcTest(BoekController.class)
public class BoekFilterControllerTest {

        @Autowired
        private MockMvc mockMvc;

        @MockBean
        private BoekService boekService;

        @MockBean
        private BoekRepository boekRepository;

        // --- GEEN PARAMETERS ---

        @Test
        void givenGeenParameters_whenGetAlleBoeken_thenStatus200EnLegeArray() throws Exception {
                // Given
                when(boekService.filterBoeken(any(), any(), any(), any(), any(),
                                any(), any(), any(), any(), any(), any(), any(), any()))
                                .thenReturn(new PageImpl<>(List.of()));

                // When & Then
                mockMvc.perform(get("/api/boeken"))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.content").isArray());
        }

        // --- ZOEKTERM ---

        @Test
        void givenZoekterm_whenGetAlleBoeken_thenZoektermWordtDoorgegevenAanService() throws Exception {
                // Given
                Boek boek = new Boek("De Alchemist", "Paulo Coelho");
                when(boekService.filterBoeken(eq("alchemist"), any(), any(), any(), any(),
                                any(), any(), any(), any(), any(), any(), any(), any()))
                                .thenReturn(new PageImpl<>(List.of(boek)));

                // When & Then
                mockMvc.perform(get("/api/boeken").param("zoekterm", "alchemist"))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.content[0].titel").value("De Alchemist"));

                verify(boekService).filterBoeken(eq("alchemist"), any(), any(), any(), any(),
                                any(), any(), any(), any(), any(), any(), any(), any());
        }

        // --- GENRE PARAMETER ---

        @Test
        void givenEnkelGenreParam_whenGetAlleBoeken_thenGenreWordtAlsLijstDoorgegevenAanService() throws Exception {
                // Given
                when(boekService.filterBoeken(any(), eq(List.of("Filosofie")), any(), any(), any(),
                                any(), any(), any(), any(), any(), any(), any(), any()))
                                .thenReturn(new PageImpl<>(List.of(new Boek("Socrates", "Plato"))));

                // When & Then
                mockMvc.perform(get("/api/boeken").param("genre", "Filosofie"))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.content").isArray());

                verify(boekService).filterBoeken(any(), eq(List.of("Filosofie")), any(), any(), any(),
                                any(), any(), any(), any(), any(), any(), any(), any());
        }

        @Test
        void givenMeerdereGenreParams_whenGetAlleBoeken_thenAlleGenresWordenVerzameldInLijst() throws Exception {
                // Given - frontend stuurt
                // ?genre=Non-fictie+algemeen&genre=Filosofie&genre=Psychologie
                List<String> verwachteGenres = List.of("Non-fictie algemeen", "Filosofie", "Psychologie");
                when(boekService.filterBoeken(any(), eq(verwachteGenres), any(), any(), any(),
                                any(), any(), any(), any(), any(), any(), any(), any()))
                                .thenReturn(new PageImpl<>(List.of(new Boek("Sapiens", "Harari"))));

                // When & Then
                mockMvc.perform(get("/api/boeken")
                                .param("genre", "Non-fictie algemeen")
                                .param("genre", "Filosofie")
                                .param("genre", "Psychologie"))
                                .andExpect(status().isOk());

                verify(boekService).filterBoeken(any(), eq(verwachteGenres), any(), any(), any(),
                                any(), any(), any(), any(), any(), any(), any(), any());
        }

        // --- LEESNIVEAU ---

        @Test
        void givenLeesniveauParam_whenGetAlleBoeken_thenLeesniveauWordtDoorgegevenAanService() throws Exception {
                // Given
                Boek boek = new Boek("Eenvoudig Boek", "Auteur");
                boek.setLeesniveau("A (eenvoudig taalgebruik)");
                when(boekService.filterBoeken(any(), any(), any(), any(), any(),
                                any(), any(), any(), any(), eq("A (eenvoudig taalgebruik)"), any(), any(), any()))
                                .thenReturn(new PageImpl<>(List.of(boek)));

                // When & Then
                mockMvc.perform(get("/api/boeken").param("leesniveau", "A (eenvoudig taalgebruik)"))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.content[0].leesniveau").value("A (eenvoudig taalgebruik)"));

                verify(boekService).filterBoeken(any(), any(), any(), any(), any(),
                                any(), any(), any(), any(), eq("A (eenvoudig taalgebruik)"), any(), any(), any());
        }

        // --- BESCHIKBAARHEID ---

        @Test
        void givenBeschikbaarTrueParam_whenGetAlleBoeken_thenBeschikbaarheidWordtAlsBooleanDoorgegevenAanService()
                        throws Exception {
                // Given
                when(boekService.filterBoeken(any(), any(), any(), any(), any(),
                                any(), eq(true), any(), any(), any(), any(), any(), any()))
                                .thenReturn(new PageImpl<>(List.of(new Boek("Beschikbaar Boek", "Auteur"))));

                // When & Then
                mockMvc.perform(get("/api/boeken").param("beschikbaar", "true"))
                                .andExpect(status().isOk());

                verify(boekService).filterBoeken(any(), any(), any(), any(), any(),
                                any(), eq(true), any(), any(), any(), any(), any(), any());
        }

        @Test
        void givenBeschikbaarAfwezig_whenGetAlleBoeken_thenBeschikbaarheidIsNullInService() throws Exception {
                // Given
                when(boekService.filterBoeken(any(), any(), any(), any(), any(),
                                any(), isNull(), any(), any(), any(), any(), any(), any()))
                                .thenReturn(new PageImpl<>(List.of()));

                // When & Then
                mockMvc.perform(get("/api/boeken"))
                                .andExpect(status().isOk());

                verify(boekService).filterBoeken(any(), any(), any(), any(), any(),
                                any(), isNull(), any(), any(), any(), any(), any(), any());
        }

        // --- SORTEREN ---

        @Test
        void givenSorteerOpEnRichting_whenGetAlleBoeken_thenSortWordtJuistMeegegeven() throws Exception {
                // Given
                Sort verwachtSort = Sort.by(Sort.Direction.DESC, "titel");
                when(boekService.filterBoeken(any(), any(), any(), any(), any(),
                                any(), any(), any(), any(), any(), any(), any(), eq(verwachtSort)))
                                .thenReturn(new PageImpl<>(List.of()));

                // When & Then
                mockMvc.perform(get("/api/boeken")
                                .param("sorteerOp", "titel")
                                .param("sorteerRichting", "DESC"))
                                .andExpect(status().isOk());

                verify(boekService).filterBoeken(any(), any(), any(), any(), any(),
                                any(), any(), any(), any(), any(), any(), any(), eq(verwachtSort));
        }

        // --- BOEKBEHEER ZOEKEN (alleen zoekterm, rest null) ---

        @Test
        void givenZoektermOpAuteur_whenGetAlleBoeken_thenGeeftMatchendeBoeken() throws Exception {
                // Given - boekbeheer zoekt op auteur
                Boek boek = new Boek("De Alchemist", "Paulo Coelho");
                when(boekService.filterBoeken(eq("coelho"), any(), any(), any(), any(),
                                any(), any(), any(), any(), any(), any(), any(), any()))
                                .thenReturn(new PageImpl<>(List.of(boek)));

                // When & Then
                mockMvc.perform(get("/api/boeken").param("zoekterm", "coelho"))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.content[0].auteur").value("Paulo Coelho"));

                verify(boekService).filterBoeken(eq("coelho"), any(), any(), any(), any(),
                                any(), any(), any(), any(), any(), any(), any(), any());
        }

        @Test
        void givenZoektermOpIsbn_whenGetAlleBoeken_thenGeeftMatchendeBoeken() throws Exception {
                // Given - boekbeheer zoekt op ISBN
                Boek boek = new Boek("Clean Code", "Robert Martin");
                boek.setIsbn("9780132350884");
                when(boekService.filterBoeken(eq("9780132350884"), any(), any(), any(), any(),
                                any(), any(), any(), any(), any(), any(), any(), any()))
                                .thenReturn(new PageImpl<>(List.of(boek)));

                // When & Then
                mockMvc.perform(get("/api/boeken").param("zoekterm", "9780132350884"))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.content[0].isbn").value("9780132350884"));
        }

        @Test
        void givenZoektermZonderResultaat_whenGetAlleBoeken_thenGeeftLegeArray() throws Exception {
                // Given - zoekterm zonder match
                when(boekService.filterBoeken(eq("xyzbestaatnietzzzz"), any(), any(), any(), any(),
                                any(), any(), any(), any(), any(), any(), any(), any()))
                                .thenReturn(new PageImpl<>(List.of()));

                // When & Then
                mockMvc.perform(get("/api/boeken").param("zoekterm", "xyzbestaatnietzzzz"))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.content").isArray())
                                .andExpect(jsonPath("$.content.length()").value(0));
        }

        @Test
        void givenZoektermMetMeerdereMatches_whenGetAlleBoeken_thenGeeftAlleMatchendeBoeken() throws Exception {
                // Given - zoekterm "java" matcht meerdere boeken
                Boek boek1 = new Boek("Effective Java", "Joshua Bloch");
                Boek boek2 = new Boek("Java Concurrency", "Brian Goetz");
                when(boekService.filterBoeken(eq("java"), any(), any(), any(), any(),
                                any(), any(), any(), any(), any(), any(), any(), any()))
                                .thenReturn(new PageImpl<>(List.of(boek1, boek2)));

                // When & Then
                mockMvc.perform(get("/api/boeken").param("zoekterm", "java"))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.content.length()").value(2))
                                .andExpect(jsonPath("$.content[0].titel").value("Effective Java"))
                                .andExpect(jsonPath("$.content[1].titel").value("Java Concurrency"));
        }

        // --- PAGINABEREIK ---

        @Test
        void givenMinEnMaxPagina_whenGetAlleBoeken_thenPaginaFiltersWordenDoorgegevenAanService() throws Exception {
                // Given
                when(boekService.filterBoeken(any(), any(), any(), any(), eq(100), eq(300),
                                any(), any(), any(), any(), any(), any(), any()))
                                .thenReturn(new PageImpl<>(List.of(new Boek("Kort Boek", "Auteur"))));

                // When & Then
                mockMvc.perform(get("/api/boeken")
                                .param("minPagina", "100")
                                .param("maxPagina", "300"))
                                .andExpect(status().isOk());

                verify(boekService).filterBoeken(any(), any(), any(), any(), eq(100), eq(300),
                                any(), any(), any(), any(), any(), any(), any());
        }
}
