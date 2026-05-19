package ap.be.backend;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import ap.be.backend.entity.Tag;
import ap.be.backend.entity.TagType;
import ap.be.backend.exception.DuplicateTagException;
import ap.be.backend.exception.TagInGebruikException;
import ap.be.backend.exception.TagNotFoundException;
import ap.be.backend.repository.TagRepository;
import ap.be.backend.service.TagService;

@ExtendWith(MockitoExtension.class)
public class TagServiceTest {

    @Mock
    private TagRepository tagRepository;

    @InjectMocks
    private TagService tagService;

    private Tag bestaande;

    @BeforeEach
    void setUp() {
        bestaande = new Tag("Fantasy", TagType.GENRE);
        bestaande.setId(1L);
    }

    @Test
    void create_trimt_en_slaat_op() {
        when(tagRepository.existsByTypeAndNaamIgnoreCase(TagType.GENRE, "Fantasy")).thenReturn(false);
        when(tagRepository.save(any(Tag.class))).thenAnswer(inv -> inv.getArgument(0));

        Tag result = tagService.create(TagType.GENRE, "  Fantasy  ");

        assertEquals("Fantasy", result.getNaam());
        assertEquals(TagType.GENRE, result.getType());
    }

    @Test
    void create_gooit_duplicate_bij_bestaande_naam() {
        when(tagRepository.existsByTypeAndNaamIgnoreCase(TagType.GENRE, "Fantasy")).thenReturn(true);

        assertThrows(DuplicateTagException.class, () -> tagService.create(TagType.GENRE, "Fantasy"));
        verify(tagRepository, never()).save(any());
    }

    @Test
    void create_gooit_bij_lege_naam() {
        assertThrows(IllegalArgumentException.class, () -> tagService.create(TagType.GENRE, "  "));
        assertThrows(IllegalArgumentException.class, () -> tagService.create(TagType.GENRE, null));
    }

    @Test
    void rename_werkt() {
        when(tagRepository.findById(1L)).thenReturn(Optional.of(bestaande));
        when(tagRepository.existsByTypeAndNaamIgnoreCaseAndIdNot(TagType.GENRE, "Sci-Fi", 1L)).thenReturn(false);
        when(tagRepository.save(any(Tag.class))).thenAnswer(inv -> inv.getArgument(0));

        Tag result = tagService.rename(1L, "Sci-Fi");

        assertEquals("Sci-Fi", result.getNaam());
    }

    @Test
    void rename_gooit_duplicate_bij_botsing() {
        when(tagRepository.findById(1L)).thenReturn(Optional.of(bestaande));
        when(tagRepository.existsByTypeAndNaamIgnoreCaseAndIdNot(TagType.GENRE, "Roman", 1L)).thenReturn(true);

        assertThrows(DuplicateTagException.class, () -> tagService.rename(1L, "Roman"));
    }

    @Test
    void rename_gooit_not_found() {
        when(tagRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(TagNotFoundException.class, () -> tagService.rename(99L, "X"));
    }

    @Test
    void delete_verwijdert_als_niet_in_gebruik() {
        when(tagRepository.findById(1L)).thenReturn(Optional.of(bestaande));
        when(tagRepository.countBoekenWithTag(1L)).thenReturn(0L);

        tagService.delete(1L);

        verify(tagRepository, times(1)).delete(bestaande);
    }

    @Test
    void delete_blokkeert_als_in_gebruik() {
        when(tagRepository.findById(1L)).thenReturn(Optional.of(bestaande));
        when(tagRepository.countBoekenWithTag(1L)).thenReturn(3L);

        TagInGebruikException ex = assertThrows(TagInGebruikException.class, () -> tagService.delete(1L));
        verify(tagRepository, never()).delete(any(Tag.class));
        assertEquals(true, ex.getMessage().contains("3"));
    }

    @Test
    void delete_gooit_not_found() {
        when(tagRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(TagNotFoundException.class, () -> tagService.delete(99L));
    }
}
