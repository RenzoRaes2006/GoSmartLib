package ap.be.backend;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
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
import ap.be.backend.exception.TagHeeftSubtagsException;
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

        Tag result = tagService.create(TagType.GENRE, "  Fantasy  ", null);

        assertEquals("Fantasy", result.getNaam());
        assertEquals(TagType.GENRE, result.getType());
    }

    @Test
    void create_gooit_duplicate_bij_bestaande_naam() {
        when(tagRepository.existsByTypeAndNaamIgnoreCase(TagType.GENRE, "Fantasy")).thenReturn(true);

        assertThrows(DuplicateTagException.class, () -> tagService.create(TagType.GENRE, "Fantasy", null));
        verify(tagRepository, never()).save(any());
    }

    @Test
    void create_gooit_bij_lege_naam() {
        assertThrows(IllegalArgumentException.class, () -> tagService.create(TagType.GENRE, "  ", null));
        assertThrows(IllegalArgumentException.class, () -> tagService.create(TagType.GENRE, null, null));
    }

    @Test
    void create_met_parent_werkt_voor_genre() {
        Tag parent = new Tag("Non-fictie algemeen", TagType.GENRE);
        parent.setId(10L);
        when(tagRepository.existsByTypeAndNaamIgnoreCase(TagType.GENRE, "Biografie")).thenReturn(false);
        when(tagRepository.findById(10L)).thenReturn(Optional.of(parent));
        when(tagRepository.save(any(Tag.class))).thenAnswer(inv -> inv.getArgument(0));

        Tag result = tagService.create(TagType.GENRE, "Biografie", 10L);

        assertNotNull(result.getParent());
        assertEquals(10L, result.getParent().getId());
    }

    @Test
    void create_met_parent_weigert_thematisch() {
        when(tagRepository.existsByTypeAndNaamIgnoreCase(TagType.THEMATISCH, "Sub")).thenReturn(false);

        assertThrows(IllegalArgumentException.class,
                () -> tagService.create(TagType.THEMATISCH, "Sub", 10L));
        verify(tagRepository, never()).save(any());
    }

    @Test
    void create_met_parent_weigert_didactisch() {
        when(tagRepository.existsByTypeAndNaamIgnoreCase(TagType.DIDACTISCH, "Sub")).thenReturn(false);

        assertThrows(IllegalArgumentException.class,
                () -> tagService.create(TagType.DIDACTISCH, "Sub", 10L));
    }

    @Test
    void create_weigert_subtag_van_subtag() {
        Tag hoofdtag = new Tag("Non-fictie algemeen", TagType.GENRE);
        hoofdtag.setId(10L);
        Tag bestaandeSub = new Tag("Biografie", TagType.GENRE, hoofdtag);
        bestaandeSub.setId(20L);
        when(tagRepository.existsByTypeAndNaamIgnoreCase(TagType.GENRE, "Diepere")).thenReturn(false);
        when(tagRepository.findById(20L)).thenReturn(Optional.of(bestaandeSub));

        assertThrows(IllegalArgumentException.class,
                () -> tagService.create(TagType.GENRE, "Diepere", 20L));
    }

    @Test
    void create_gooit_not_found_bij_onbekende_parent() {
        when(tagRepository.existsByTypeAndNaamIgnoreCase(TagType.GENRE, "X")).thenReturn(false);
        when(tagRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(TagNotFoundException.class,
                () -> tagService.create(TagType.GENRE, "X", 99L));
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
        when(tagRepository.countByParentId(1L)).thenReturn(0L);
        when(tagRepository.countBoekenWithTag(1L)).thenReturn(0L);

        tagService.delete(1L);

        verify(tagRepository, times(1)).delete(bestaande);
    }

    @Test
    void delete_blokkeert_als_in_gebruik() {
        when(tagRepository.findById(1L)).thenReturn(Optional.of(bestaande));
        when(tagRepository.countByParentId(1L)).thenReturn(0L);
        when(tagRepository.countBoekenWithTag(1L)).thenReturn(3L);

        TagInGebruikException ex = assertThrows(TagInGebruikException.class, () -> tagService.delete(1L));
        verify(tagRepository, never()).delete(any(Tag.class));
        assertTrue(ex.getMessage().contains("3"));
    }

    @Test
    void delete_blokkeert_als_subs_aanwezig() {
        when(tagRepository.findById(1L)).thenReturn(Optional.of(bestaande));
        when(tagRepository.countByParentId(1L)).thenReturn(2L);

        TagHeeftSubtagsException ex = assertThrows(TagHeeftSubtagsException.class, () -> tagService.delete(1L));
        verify(tagRepository, never()).delete(any(Tag.class));
        assertTrue(ex.getMessage().contains("2"));
    }

    @Test
    void delete_gooit_not_found() {
        when(tagRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(TagNotFoundException.class, () -> tagService.delete(99L));
    }

    @Test
    void expandWithSubtags_geeft_originelen_terug_als_geen_subs() {
        when(tagRepository.findSubtagIdsByParentIds(List.of(1L, 2L))).thenReturn(List.of());

        List<Long> result = tagService.expandWithSubtags(List.of(1L, 2L));

        assertEquals(2, result.size());
        assertTrue(result.containsAll(List.of(1L, 2L)));
    }

    @Test
    void expandWithSubtags_voegt_subs_toe() {
        when(tagRepository.findSubtagIdsByParentIds(List.of(1L))).thenReturn(List.of(11L, 12L));

        List<Long> result = tagService.expandWithSubtags(List.of(1L));

        assertEquals(3, result.size());
        assertTrue(result.containsAll(List.of(1L, 11L, 12L)));
    }

    @Test
    void expandWithSubtags_lege_input_wordt_doorgegeven() {
        List<Long> result = tagService.expandWithSubtags(List.of());

        assertTrue(result.isEmpty());
        verify(tagRepository, never()).findSubtagIdsByParentIds(anyList());
    }
}
