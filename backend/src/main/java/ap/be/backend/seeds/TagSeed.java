package ap.be.backend.seeds;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import ap.be.backend.entity.Tag;
import ap.be.backend.entity.TagType;
import ap.be.backend.repository.TagRepository;

/**
 * Zorgt dat de standaardlijst van genres / thematische / didactische tags
 * altijd als Tag-rijen aanwezig is. Admin kan deze later hernoemen of verwijderen.
 * Idempotent: bestaande tags blijven ongewijzigd.
 */
@Component
@Profile("!test")
@Order(50)
public class TagSeed implements CommandLineRunner {

    private static final String[] GENRES = {
            "Non-fictie algemeen", "Fictie algemeen", "Literaire roman", "Spanning / thriller",
            "Detective / misdaad", "Fantasy", "Dystopie", "Historische roman", "Romantiek",
            "Coming-of-age", "Avontuur", "Oorlog & conflict", "Horror", "Humor",
            "Graphic novel / strip", "Poëzie", "Biografie / autobiografie", "Filosofie",
            "Geschiedenis", "Kunst & cultuur", "Maatschappij & politiek", "Psychologie",
            "Wetenschap & technologie"
    };

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
        ensureTags(TagType.GENRE, GENRES);
        ensureTags(TagType.THEMATISCH, THEMATISCHE);
        ensureTags(TagType.DIDACTISCH, DIDACTISCHE);
    }

    private void ensureTags(TagType type, String[] namen) {
        for (String naam : namen) {
            if (!tagRepository.existsByTypeAndNaamIgnoreCase(type, naam)) {
                tagRepository.save(new Tag(naam, type));
            }
        }
    }
}
