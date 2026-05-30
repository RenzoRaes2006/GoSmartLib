package ap.be.backend;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import ap.be.backend.entity.Tag;
import ap.be.backend.entity.TagType;
import ap.be.backend.exception.DuplicateTagException;
import ap.be.backend.exception.TagInGebruikException;
import ap.be.backend.exception.TagNotFoundException;
import ap.be.backend.service.TagService;

@SpringBootTest
@AutoConfigureMockMvc
public class TagControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private TagService tagService;

    private Tag tag(long id, String naam, TagType type) {
        Tag t = new Tag(naam, type);
        t.setId(id);
        return t;
    }

    @Test
    @WithMockUser(roles = "LEERLING")
    void givenTagType_whenGetTags_thenLijst() throws Exception {
        when(tagService.getByType(TagType.GENRE)).thenReturn(List.of(tag(1L, "Fantasy", TagType.GENRE)));

        mockMvc.perform(get("/api/tags").param("type", "GENRE"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].naam").value("Fantasy"))
                .andExpect(jsonPath("$[0].type").value("GENRE"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void givenAdminRol_whenCreateTag_thenOk() throws Exception {
        when(tagService.create(eq(TagType.GENRE), eq("Fantasy"), eq(null)))
                .thenReturn(tag(1L, "Fantasy", TagType.GENRE));

        mockMvc.perform(post("/api/tags")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"naam\":\"Fantasy\",\"type\":\"GENRE\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.naam").value("Fantasy"));
    }

    @Test
    @WithMockUser(roles = "BEHEERDER")
    void givenBeheerderRol_whenCreateTag_thenForbidden() throws Exception {
        mockMvc.perform(post("/api/tags")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"naam\":\"Fantasy\",\"type\":\"GENRE\"}"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void givenDuplicateTag_whenCreateTag_thenConflict() throws Exception {
        when(tagService.create(any(), any(), any())).thenThrow(new DuplicateTagException("Fantasy"));

        mockMvc.perform(post("/api/tags")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"naam\":\"Fantasy\",\"type\":\"GENRE\"}"))
                .andExpect(status().isConflict());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void givenBestaandeTag_whenRenameTag_thenOk() throws Exception {
        when(tagService.rename(eq(1L), eq("Sci-Fi"))).thenReturn(tag(1L, "Sci-Fi", TagType.GENRE));

        mockMvc.perform(put("/api/tags/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"naam\":\"Sci-Fi\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.naam").value("Sci-Fi"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void givenNietBestaandeTag_whenRenameTag_thenNotFound() throws Exception {
        when(tagService.rename(eq(99L), any())).thenThrow(new TagNotFoundException(99L));

        mockMvc.perform(put("/api/tags/99")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"naam\":\"X\"}"))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void givenBestaandeTag_whenDeleteTag_thenNoContent() throws Exception {
        mockMvc.perform(delete("/api/tags/1"))
                .andExpect(status().isNoContent());

        verify(tagService, times(1)).delete(1L);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void givenTagInGebruik_whenDeleteTag_thenConflict() throws Exception {
        doThrow(new TagInGebruikException("Fantasy", 3L)).when(tagService).delete(1L);

        mockMvc.perform(delete("/api/tags/1"))
                .andExpect(status().isConflict())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("3")));
    }

    @Test
    @WithMockUser(roles = "BEHEERDER")
    void givenBeheerderRol_whenDeleteTag_thenForbidden() throws Exception {
        mockMvc.perform(delete("/api/tags/1"))
                .andExpect(status().isForbidden());
    }
}
