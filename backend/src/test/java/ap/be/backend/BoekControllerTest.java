package ap.be.backend;

import ap.be.backend.controller.BoekController;
import ap.be.backend.entity.Boek;
import ap.be.backend.repository.BoekRepository;
import ap.be.backend.service.BoekService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Sort;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(BoekController.class)
public class BoekControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private BoekRepository boekRepository;

    @MockBean
    private BoekService boekService;

    @Test
    public void givenSearchTerm_whenGetAlleBoekenCalled_thenCallsSearchBoekenInRepository() throws Exception {
        // Given
        String term = "java";
        Sort sort = Sort.by(Sort.Direction.ASC, "locatie");
        Mockito.when(boekRepository.filterBoeken(term, null, null, null, null, null, null, null, term, term, sort))
                .thenReturn(List.of(new Boek()));

        // When & Then
        mockMvc.perform(get("/api/boeken").param("zoekterm", term))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());

        Mockito.verify(boekRepository).filterBoeken(term, null, null, null, null, null, null, null, term, term, sort);
    }

    @Test
    public void givenNoSearchTerm_whenGetAlleBoekenCalled_thenCallsFindAllInRepository() throws Exception {
        // Given (No specific setup needed for params)

        // When & Then
        mockMvc.perform(get("/api/boeken"))
                .andExpect(status().isOk());

        Mockito.verify(boekRepository).findAll(Mockito.any(Sort.class));
    }
}