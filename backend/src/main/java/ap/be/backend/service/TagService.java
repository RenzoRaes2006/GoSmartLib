package ap.be.backend.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import ap.be.backend.entity.Tag;
import ap.be.backend.entity.TagType;
import ap.be.backend.exception.DuplicateTagException;
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

    @Transactional
    public Tag create(TagType type, String naam) {
        String trimmed = normaliseer(naam);
        if (tagRepository.existsByTypeAndNaamIgnoreCase(type, trimmed)) {
            throw new DuplicateTagException(trimmed);
        }
        return tagRepository.save(new Tag(trimmed, type));
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
