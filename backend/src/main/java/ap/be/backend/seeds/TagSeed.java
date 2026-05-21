package ap.be.backend.seeds;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import ap.be.backend.entity.Tag;
import ap.be.backend.entity.TagType;
import ap.be.backend.repository.TagRepository;

/**
 * Bootstrapt de standaardlijst van genres / thematische / didactische tags
 * uitsluitend wanneer de tags-tabel volledig leeg is (fresh deploy / nieuwe dev-DB).
 * Na de eerste run draait deze seed nooit meer, zodat admin-wijzigingen
 * (verwijderen, hernoemen, toevoegen) bij restart behouden blijven.
 *
 * Voor bestaande databases die al tags bevatten zonder hierarchie, zie
 * {@link TagHierarchieMigratie}.
 */
@Component
@Profile("!test")
@Order(50)
public class TagSeed implements CommandLineRunner {

    private static final Map<String, List<String>> GENRE_HIERARCHIE = new LinkedHashMap<>();
    static {
        GENRE_HIERARCHIE.put("Non-fictie algemeen", List.of(
                "Biografie / autobiografie",
                "Filosofie",
                "Geschiedenis",
                "Kunst & cultuur",
                "Maatschappij & politiek",
                "Psychologie",
                "Wetenschap & technologie"));
        GENRE_HIERARCHIE.put("Fictie algemeen", List.of());
        GENRE_HIERARCHIE.put("Literaire roman", List.of());
        GENRE_HIERARCHIE.put("Spanning / thriller", List.of());
        GENRE_HIERARCHIE.put("Detective / misdaad", List.of());
        GENRE_HIERARCHIE.put("Fantasy", List.of());
        GENRE_HIERARCHIE.put("Dystopie", List.of());
        GENRE_HIERARCHIE.put("Historische roman", List.of());
        GENRE_HIERARCHIE.put("Romantiek", List.of());
        GENRE_HIERARCHIE.put("Coming-of-age", List.of());
        GENRE_HIERARCHIE.put("Avontuur", List.of());
        GENRE_HIERARCHIE.put("Oorlog & conflict", List.of());
        GENRE_HIERARCHIE.put("Horror", List.of());
        GENRE_HIERARCHIE.put("Humor", List.of());
        GENRE_HIERARCHIE.put("Graphic novel / strip", List.of());
        GENRE_HIERARCHIE.put("Poëzie", List.of());
    }

    private static final String[] THEMATISCHE = {
            "Liefde & relaties", "Vriendschap", "Identiteit & zelfbeeld", "Gender & seksualiteit",
            "Diversiteit & inclusie", "Mentale gezondheid", "Rouw & verlies", "Familie",
            "School & prestatiedruk", "Sociale media", "Migratie & afkomst",
            "Armoede & ongelijkheid", "Macht & onrecht", "Avontuur & ontdekking", "Overleven",
            "Toekomst & technologie"
    };

    private static final String[] DIDACTISCHE = {
            "LESMETHODES", "DIDACTISCHE_HANDBOEKEN", "EVALUATIEMATERIAAL", "NASLAGWERKEN"
    };

    private final TagRepository tagRepository;

    public TagSeed(TagRepository tagRepository) {
        this.tagRepository = tagRepository;
    }

    @Override
    public void run(String... args) {
        if (tagRepository.count() > 0) {
            return;
        }
        seedGenresMetHierarchie();
        seedFlatTags(TagType.THEMATISCH, THEMATISCHE);
        seedFlatTags(TagType.DIDACTISCH, DIDACTISCHE);
    }

    private void seedGenresMetHierarchie() {
        for (Map.Entry<String, List<String>> entry : GENRE_HIERARCHIE.entrySet()) {
            Tag parent = tagRepository.save(new Tag(entry.getKey(), TagType.GENRE));
            for (String subNaam : entry.getValue()) {
                tagRepository.save(new Tag(subNaam, TagType.GENRE, parent));
            }
        }
    }

    private void seedFlatTags(TagType type, String[] namen) {
        for (String naam : namen) {
            tagRepository.save(new Tag(naam, type));
        }
    }
}
