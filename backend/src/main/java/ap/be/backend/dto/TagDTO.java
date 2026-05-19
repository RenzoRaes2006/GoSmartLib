package ap.be.backend.dto;

import ap.be.backend.entity.Tag;
import ap.be.backend.entity.TagType;

public record TagDTO(Long id, String naam, TagType type) {
    public static TagDTO from(Tag tag) {
        return new TagDTO(tag.getId(), tag.getNaam(), tag.getType());
    }
}
