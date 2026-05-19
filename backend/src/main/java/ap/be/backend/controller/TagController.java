package ap.be.backend.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import ap.be.backend.dto.TagDTO;
import ap.be.backend.entity.TagType;
import ap.be.backend.service.TagService;

@RestController
@RequestMapping("/api/tags")
public class TagController {

    private final TagService tagService;

    public TagController(TagService tagService) {
        this.tagService = tagService;
    }

    @GetMapping
    public ResponseEntity<List<TagDTO>> list(@RequestParam(required = false) TagType type) {
        List<TagDTO> tags = (type == null ? tagService.getAll() : tagService.getByType(type))
                .stream().map(TagDTO::from).toList();
        return ResponseEntity.ok(tags);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public ResponseEntity<TagDTO> create(@RequestBody TagRequest request) {
        TagDTO created = TagDTO.from(tagService.create(request.type(), request.naam()));
        return ResponseEntity.ok(created);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}")
    public ResponseEntity<TagDTO> rename(@PathVariable Long id, @RequestBody RenameRequest request) {
        return ResponseEntity.ok(TagDTO.from(tagService.rename(id, request.naam())));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        tagService.delete(id);
        return ResponseEntity.noContent().build();
    }

    public record TagRequest(String naam, TagType type) {
    }

    public record RenameRequest(String naam) {
    }
}
