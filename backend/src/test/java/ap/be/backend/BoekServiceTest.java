package ap.be.backend;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

import ap.be.backend.dto.BoekDTO;
import ap.be.backend.entity.Boek;
import ap.be.backend.exception.IsbnAlreadyExistsException;
import ap.be.backend.repository.BoekRepository;
import ap.be.backend.repository.TagRepository;
import ap.be.backend.service.BoekService;
import ap.be.backend.service.TagService;

@ExtendWith(MockitoExtension.class)
public class BoekServiceTest {

    @Mock
    private BoekRepository boekRepository;

    @Mock
    private TagRepository tagRepository;

    @Mock
    private TagService tagService;

    @InjectMocks
    private BoekService boekService;

    @Test
    void givenNieuwBoek_whenAddBoek_thenTrimIsbnAndSave() {
        BoekDTO dto = new BoekDTO(
                null,
                "Clean Code",
                "Robert C. Martin",
                " 9780132350884 ",
                "Prentice Hall",
                "2008",
                464,
                null,
                "A handbook of agile software craftsmanship",
                3,
                "A1",
                "", null, null, true);

        when(boekRepository.findByIsbn("9780132350884")).thenReturn(Optional.empty());

        ArgumentCaptor<Boek> boekCaptor = ArgumentCaptor.forClass(Boek.class);
        when(boekRepository.save(boekCaptor.capture())).thenAnswer(invocation -> invocation.getArgument(0));

        Boek result = boekService.addBoek(dto);

        assertEquals("9780132350884", result.getIsbn());
        assertEquals("Clean Code", result.getTitel());
        assertEquals("Robert C. Martin", result.getAuteur());
        assertTrue(result.isDidactischMateriaal());

        verify(boekRepository, times(1)).findByIsbn("9780132350884");
        verify(boekRepository, times(1)).save(boekCaptor.getValue());
        assertEquals("9780132350884", boekCaptor.getValue().getIsbn());
        assertTrue(boekCaptor.getValue().isDidactischMateriaal());
    }

    @Test
    void givenBoekZonderOptioneleVelden_whenAddBoek_thenFallbackWaardenWordenGezet() {
        BoekDTO dto = new BoekDTO(
                null,
                "Titel",
                "Auteur",
                "9780132350884",
                "",
                null,
                0,
                null,
                "Dit is een beschrijving",
                5,
                "",
                null,
                null,
                "",
                null);

        when(boekRepository.findByIsbn("9780132350884")).thenReturn(Optional.empty());

        ArgumentCaptor<Boek> boekCaptor = ArgumentCaptor.forClass(Boek.class);
        when(boekRepository.save(boekCaptor.capture())).thenAnswer(invocation -> invocation.getArgument(0));

        boekService.addBoek(dto);

        Boek opgeslagen = boekCaptor.getValue();
        assertEquals("Geen uitgever", opgeslagen.getUitgever());
        assertEquals("Geen uitgavejaar", opgeslagen.getUitgaveJaar());
        assertTrue(opgeslagen.getTags().isEmpty());
        assertEquals("Geen locatie", opgeslagen.getLocatie());
        assertEquals("", opgeslagen.getAfbeelding());
        assertEquals("Geen leesniveau", opgeslagen.getLeesniveau());
        assertFalse(opgeslagen.isDidactischMateriaal());
    }

    @Test
    void givenBestaandIsbn_whenAddBoek_thenThrowIsbnAlreadyExistsException() {
        BoekDTO dto = new BoekDTO(
                null,
                "Domain-Driven Design",
                "Eric Evans",
                " 9780321125217 ",
                "Addison-Wesley",
                "2003",
                560,
                null,
                "DDD blue book",
                2,
                "B2",
                "", null, null, false);

        when(boekRepository.findByIsbn("9780321125217")).thenReturn(Optional.of(new Boek()));

        IsbnAlreadyExistsException ex = assertThrows(IsbnAlreadyExistsException.class, () -> boekService.addBoek(dto));

        assertEquals("Boek met ISBN 9780321125217 bestaat al.", ex.getMessage());
        verify(boekRepository, times(1)).findByIsbn("9780321125217");
        verify(boekRepository, never()).save(org.mockito.ArgumentMatchers.any(Boek.class));
    }

    @Test
    void givenBestaandId_whenDeleteBoek_thenDeleteAndReturnTrue() {
        Long boekId = 10L;
        when(boekRepository.existsById(boekId)).thenReturn(true);

        boolean deleted = boekService.deleteBoek(boekId);

        assertTrue(deleted);
        verify(boekRepository, times(1)).existsById(boekId);
        verify(boekRepository, times(1)).deleteById(boekId);
    }

    @Test
    void givenNietBestaandId_whenDeleteBoek_thenReturnFalseWithoutDelete() {
        Long boekId = 99L;
        when(boekRepository.existsById(boekId)).thenReturn(false);

        boolean deleted = boekService.deleteBoek(boekId);

        assertFalse(deleted);
        verify(boekRepository, times(1)).existsById(boekId);
        verify(boekRepository, never()).deleteById(boekId);
    }

    @Test
    void givenBestaandId_whenUpdateBoek_thenUpdateAndSave() {
        Long boekId = 5L;
        Boek bestaandBoek = new Boek();
        bestaandBoek.setId(boekId);
        bestaandBoek.setTitel("Old Title");
        bestaandBoek.setAuteur("Old Author");
        bestaandBoek.setIsbn("9780000000000");

        BoekDTO dto = new BoekDTO(
                boekId,
                "New Title",
                "New Author",
                "9781111111111",
                "New Publisher",
                "2025",
                320,
                null,
                "Updated description",
                7,
                "C3",
                "", null, null, true);

        when(boekRepository.findById(boekId)).thenReturn(Optional.of(bestaandBoek));
        when(boekRepository.save(bestaandBoek)).thenReturn(bestaandBoek);

        Boek result = boekService.updateBoek(boekId, dto);

        assertEquals("New Title", result.getTitel());
        assertEquals("New Author", result.getAuteur());
        assertEquals("9781111111111", result.getIsbn());
        assertEquals("New Publisher", result.getUitgever());
        assertEquals("2025", result.getUitgaveJaar());
        assertEquals(320, result.getPagina());
        assertEquals("Updated description", result.getBeschrijving());
        assertEquals(7, result.getAantalExemplaren());
        assertEquals("C3", result.getLocatie());
        assertTrue(result.isDidactischMateriaal());

        verify(boekRepository, times(1)).findById(boekId);
        verify(boekRepository, times(1)).save(bestaandBoek);
    }

    @Test
    void givenNietBestaandId_whenUpdateBoek_thenReturnNullAndDoNotSave() {
        Long boekId = 404L;
        BoekDTO dto = new BoekDTO(
                boekId,
                "Any",
                "Any",
                "9782222222222",
                "Any",
                "2024",
                100,
                null,
                "Any",
                1,
                "Z1",
                "", null, null, false);

        when(boekRepository.findById(boekId)).thenReturn(Optional.empty());

        Boek result = boekService.updateBoek(boekId, dto);

        assertNull(result);
        verify(boekRepository, times(1)).findById(boekId);
        verify(boekRepository, never()).save(org.mockito.ArgumentMatchers.any(Boek.class));
    }

    @Test
    void givenBestaandIsbn_whenGetBoekByIsbn_thenGeeftBoekTerug() {
        Boek boek = new Boek("Clean Code", "Robert Martin");
        boek.setIsbn("9780132350884");
        when(boekRepository.findByIsbn("9780132350884")).thenReturn(Optional.of(boek));

        Boek result = boekService.getBoekByIsbn("9780132350884");

        assertNotNull(result);
        assertEquals("9780132350884", result.getIsbn());
        assertEquals("Clean Code", result.getTitel());
        verify(boekRepository, times(1)).findByIsbn("9780132350884");
    }

    @Test
    void givenNietBestaandIsbn_whenGetBoekByIsbn_thenGeeftNullTerug() {
        when(boekRepository.findByIsbn("0000000000000")).thenReturn(Optional.empty());

        Boek result = boekService.getBoekByIsbn("0000000000000");

        assertNull(result);
        verify(boekRepository, times(1)).findByIsbn("0000000000000");
    }

    @Test
    void givenIsbnMetSpaties_whenGetBoekByIsbn_thenTrimsEnZoekt() {
        Boek boek = new Boek("Test Boek", "Auteur");
        boek.setIsbn("9781234567890");
        when(boekRepository.findByIsbn("9781234567890")).thenReturn(Optional.of(boek));

        Boek result = boekService.getBoekByIsbn(" 9781234567890 ");

        assertNotNull(result);
        assertEquals("9781234567890", result.getIsbn());
    }

    @Test
    void givenBoeken_whenGetBoekenPaged_thenGeeftPaginaTerug() {
        Boek boek1 = new Boek("Boek A", "Auteur A");
        Boek boek2 = new Boek("Boek B", "Auteur B");
        PageRequest pageRequest = PageRequest.of(0, 6, Sort.by("titel").ascending());
        Page<Boek> verwacht = new PageImpl<>(List.of(boek1, boek2), pageRequest, 2);
        when(boekRepository.findAll(pageRequest)).thenReturn(verwacht);

        Page<Boek> result = boekService.getBoekenPaged(0, 6);

        assertEquals(2, result.getTotalElements());
        assertEquals("Boek A", result.getContent().get(0).getTitel());
        assertEquals("Boek B", result.getContent().get(1).getTitel());
        verify(boekRepository, times(1)).findAll(pageRequest);
    }

    @Test
    void givenGeenBoeken_whenGetBoekenPaged_thenGeeftLegePaginaTerug() {
        PageRequest pageRequest = PageRequest.of(0, 6, Sort.by("titel").ascending());
        Page<Boek> verwacht = new PageImpl<>(List.of(), pageRequest, 0);
        when(boekRepository.findAll(pageRequest)).thenReturn(verwacht);

        Page<Boek> result = boekService.getBoekenPaged(0, 6);

        assertEquals(0, result.getTotalElements());
        assertTrue(result.getContent().isEmpty());
    }

    @Test
    void givenTweedePagina_whenGetBoekenPaged_thenGeeftJuistePaginaTerug() {
        Boek boek = new Boek("Boek C", "Auteur C");
        PageRequest pageRequest = PageRequest.of(1, 6, Sort.by("titel").ascending());
        Page<Boek> verwacht = new PageImpl<>(List.of(boek), pageRequest, 7);
        when(boekRepository.findAll(pageRequest)).thenReturn(verwacht);

        Page<Boek> result = boekService.getBoekenPaged(1, 6);

        assertEquals(7, result.getTotalElements());
        assertEquals(1, result.getContent().size());
        assertEquals(1, result.getNumber());
    }
}
