package ap.be.backend.dto;

import java.util.List;

import ap.be.backend.entity.Tag;
import ap.be.backend.entity.TagType;

public record TagDTO(Long id, String naam, TagType type, Long parentId, List<TagDTO> subTags) {
    public static TagDTO from(Tag tag) {
        return new TagDTO(
                tag.getId(),
                tag.getNaam(),
                tag.getType(),
                tag.getParent() != null ? tag.getParent().getId() : null,
                null);
    }

    public static TagDTO fromWithSubs(Tag tag, List<TagDTO> subs) {
        return new TagDTO(
                tag.getId(),
                tag.getNaam(),
                tag.getType(),
                tag.getParent() != null ? tag.getParent().getId() : null,
                subs);
    }
}
