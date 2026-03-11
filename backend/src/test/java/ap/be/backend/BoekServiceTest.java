package ap.be.backend;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
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

import ap.be.backend.dto.BoekDTO;
import ap.be.backend.entity.Boek;
import ap.be.backend.exception.IsbnAlreadyExistsException;
import ap.be.backend.repository.BoekRepository;
import ap.be.backend.service.BoekService;

@ExtendWith(MockitoExtension.class)
public class BoekServiceTest {

    @Mock
    private BoekRepository boekRepository;

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
                List.of("Software", "Programming"),
                "A handbook of agile software craftsmanship",
                3,
                "A1",
                "");

        when(boekRepository.findByIsbn("9780132350884")).thenReturn(Optional.empty());

        ArgumentCaptor<Boek> boekCaptor = ArgumentCaptor.forClass(Boek.class);
        when(boekRepository.save(boekCaptor.capture())).thenAnswer(invocation -> invocation.getArgument(0));

        Boek result = boekService.addBoek(dto);

        assertEquals("9780132350884", result.getIsbn());
        assertEquals("Clean Code", result.getTitel());
        assertEquals("Robert C. Martin", result.getAuteur());

        verify(boekRepository, times(1)).findByIsbn("9780132350884");
        verify(boekRepository, times(1)).save(boekCaptor.getValue());
        assertEquals("9780132350884", boekCaptor.getValue().getIsbn());
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
                List.of("Architecture"),
                "DDD blue book",
                2,
                "B2",
                "");

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
                List.of("Tech"),
                "Updated description",
                7,
                "C3",
                "");

        when(boekRepository.findById(boekId)).thenReturn(Optional.of(bestaandBoek));
        when(boekRepository.save(bestaandBoek)).thenReturn(bestaandBoek);

        Boek result = boekService.updateBoek(boekId, dto);

        assertEquals("New Title", result.getTitel());
        assertEquals("New Author", result.getAuteur());
        assertEquals("9781111111111", result.getIsbn());
        assertEquals("New Publisher", result.getUitgever());
        assertEquals("2025", result.getUitgaveJaar());
        assertEquals(320, result.getPagina());
        assertEquals(1, result.getGenre().size());
        assertEquals("Updated description", result.getBeschrijving());
        assertEquals(7, result.getAantalExemplaren());
        assertEquals("C3", result.getLocatie());

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
                List.of("Any"),
                "Any",
                1,
                "Z1",
                "");

        when(boekRepository.findById(boekId)).thenReturn(Optional.empty());

        Boek result = boekService.updateBoek(boekId, dto);

        assertNull(result);
        verify(boekRepository, times(1)).findById(boekId);
        verify(boekRepository, never()).save(org.mockito.ArgumentMatchers.any(Boek.class));
    }
}
