package ap.be.backend.seeds;

import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import ap.be.backend.entity.Tag;
import ap.be.backend.entity.TagType;
import ap.be.backend.repository.TagRepository;

/**
 * Eenmalige data-migratie voor bestaande databases die al genre-tags bevatten
 * van vóór de parent/child hierarchie bestond. Zet parent_id op de Non-fictie
 * subs als die nog null is en als de tag nog onder z'n originele naam bestaat.
 *
 * Volledig idempotent: zodra parent_id is gevuld, slaat de migratie die rij
 * over. Renames door admin worden gerespecteerd — een hernoemde sub wordt
 * niet meer herkend en blijft staan zoals de admin hem heeft achtergelaten.
 *
 * Mag verwijderd worden zodra alle deployments deze migratie minstens één keer
 * hebben uitgevoerd (richtlijn: 1-2 releases na introductie).
 */
@Component
@Profile("!test")
@Order(51)
public class TagHierarchieMigratie implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(TagHierarchieMigratie.class);

    private static final Map<String, List<String>> PARENT_TO_SUBS = Map.of(
            "Non-fictie algemeen", List.of(
                    "Biografie / autobiografie",
                    "Filosofie",
                    "Geschiedenis",
                    "Kunst & cultuur",
                    "Maatschappij & politiek",
                    "Psychologie",
                    "Wetenschap & technologie"));

    private final TagRepository tagRepository;

    public TagHierarchieMigratie(TagRepository tagRepository) {
        this.tagRepository = tagRepository;
    }

    @Override
    @Transactional
    public void run(String... args) {
        for (Map.Entry<String, List<String>> entry : PARENT_TO_SUBS.entrySet()) {
            Tag parent = tagRepository.findByTypeAndNaamIgnoreCase(TagType.GENRE, entry.getKey()).orElse(null);
            if (parent == null) {
                continue;
            }
            for (String subNaam : entry.getValue()) {
                Tag sub = tagRepository.findByTypeAndNaamIgnoreCase(TagType.GENRE, subNaam).orElse(null);
                if (sub == null || sub.getParent() != null) {
                    continue;
                }
                sub.setParent(parent);
                tagRepository.save(sub);
                log.info("TagHierarchieMigratie: '{}' gekoppeld aan parent '{}'", subNaam, entry.getKey());
            }
        }
    }
}
