package ap.be.backend.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import ap.be.backend.entity.Tag;
import ap.be.backend.entity.TagType;

public interface TagRepository extends JpaRepository<Tag, Long> {

    List<Tag> findByTypeOrderByNaamAsc(TagType type);

    List<Tag> findByTypeAndParentIsNullOrderByNaamAsc(TagType type);

    List<Tag> findByParentIdOrderByNaamAsc(Long parentId);

    long countByParentId(Long parentId);

    Optional<Tag> findByTypeAndNaamIgnoreCase(TagType type, String naam);

    boolean existsByTypeAndNaamIgnoreCase(TagType type, String naam);

    boolean existsByTypeAndNaamIgnoreCaseAndIdNot(TagType type, String naam, Long id);

    @Query("SELECT COUNT(b) FROM Boek b JOIN b.tags t WHERE t.id = :tagId")
    long countBoekenWithTag(@Param("tagId") Long tagId);

    @Query("SELECT t.id FROM Tag t WHERE t.parent.id IN :parentIds")
    List<Long> findSubtagIdsByParentIds(@Param("parentIds") List<Long> parentIds);
}
