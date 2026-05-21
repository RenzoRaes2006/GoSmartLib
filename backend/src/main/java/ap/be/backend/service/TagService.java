package ap.be.backend.service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import ap.be.backend.dto.TagDTO;
import ap.be.backend.entity.Tag;
import ap.be.backend.entity.TagType;
import ap.be.backend.exception.DuplicateTagException;
import ap.be.backend.exception.TagHeeftSubtagsException;
import ap.be.backend.exception.TagInGebruikException;
import ap.be.backend.exception.TagNotFoundException;
import ap.be.backend.repository.TagRepository;

@Service
public class TagService {

    private final TagRepository tagRepository;

    public TagService(TagRepository tagRepository) {
        this.tagRepository = tagRepository;
    }

    public List<Tag> getByType(TagType type) {
        return tagRepository.findByTypeOrderByNaamAsc(type);
    }

    public List<Tag> getAll() {
        return tagRepository.findAll();
    }

    public List<TagDTO> getTreeByType(TagType type) {
        List<Tag> hoofdTags = tagRepository.findByTypeAndParentIsNullOrderByNaamAsc(type);
        List<TagDTO> result = new ArrayList<>();
        for (Tag hoofd : hoofdTags) {
            List<TagDTO> subs = tagRepository.findByParentIdOrderByNaamAsc(hoofd.getId())
                    .stream().map(TagDTO::from).toList();
            result.add(TagDTO.fromWithSubs(hoofd, subs));
        }
        return result;
    }

    public List<Long> expandWithSubtags(List<Long> tagIds) {
        if (tagIds == null || tagIds.isEmpty()) {
            return tagIds;
        }
        List<Long> subIds = tagRepository.findSubtagIdsByParentIds(tagIds);
        if (subIds.isEmpty()) {
            return tagIds;
        }
        Set<Long> expanded = new HashSet<>(tagIds);
        expanded.addAll(subIds);
        return new ArrayList<>(expanded);
    }

    @Transactional
    public Tag create(TagType type, String naam, Long parentId) {
        String trimmed = normaliseer(naam);
        if (tagRepository.existsByTypeAndNaamIgnoreCase(type, trimmed)) {
            throw new DuplicateTagException(trimmed);
        }
        Tag parent = null;
        if (parentId != null) {
            if (type != TagType.GENRE) {
                throw new IllegalArgumentException("Subtags zijn enkel toegestaan voor GENRE.");
            }
            parent = tagRepository.findById(parentId)
                    .orElseThrow(() -> new TagNotFoundException(parentId));
            if (parent.getType() != TagType.GENRE) {
                throw new IllegalArgumentException("Parent-tag moet van type GENRE zijn.");
            }
            if (parent.getParent() != null) {
                throw new IllegalArgumentException("Een subgenre mag zelf geen subgenres hebben.");
            }
        }
        return tagRepository.save(new Tag(trimmed, type, parent));
    }

    @Transactional
    public Tag rename(Long id, String nieuweNaam) {
        Tag tag = tagRepository.findById(id).orElseThrow(() -> new TagNotFoundException(id));
        String trimmed = normaliseer(nieuweNaam);
        if (tagRepository.existsByTypeAndNaamIgnoreCaseAndIdNot(tag.getType(), trimmed, id)) {
            throw new DuplicateTagException(trimmed);
        }
        tag.setNaam(trimmed);
        return tagRepository.save(tag);
    }

    @Transactional
    public void delete(Long id) {
        Tag tag = tagRepository.findById(id).orElseThrow(() -> new TagNotFoundException(id));
        long aantalSubs = tagRepository.countByParentId(id);
        if (aantalSubs > 0) {
            throw new TagHeeftSubtagsException(tag.getNaam(), aantalSubs);
        }
        long inGebruik = tagRepository.countBoekenWithTag(id);
        if (inGebruik > 0) {
            throw new TagInGebruikException(tag.getNaam(), inGebruik);
        }
        tagRepository.delete(tag);
    }

    private String normaliseer(String naam) {
        if (naam == null) {
            throw new IllegalArgumentException("Tag-naam mag niet leeg zijn.");
        }
        String trimmed = naam.trim();
        if (trimmed.isEmpty()) {
            throw new IllegalArgumentException("Tag-naam mag niet leeg zijn.");
        }
        return trimmed;
    }
}
