package ap.be.backend.seeds;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import ap.be.backend.entity.Boek;
import ap.be.backend.entity.Tag;
import ap.be.backend.entity.TagType;
import ap.be.backend.repository.BoekRepository;
import ap.be.backend.repository.TagRepository;
import org.springframework.transaction.annotation.Transactional;

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
            "Lesmethodes", "Didactische handboeken", "Evaluatiemateriaal", "Naslagwerken"
    };

    private static final java.util.Map<String, String> HERNOEM_DIDACTISCH = java.util.Map.of(
            "LESMETHODES", "Lesmethodes",
            "DIDACTISCHE_HANDBOEKEN", "Didactische handboeken",
            "EVALUATIEMATERIAAL", "Evaluatiemateriaal",
            "NASLAGWERKEN", "Naslagwerken"
    );

    private final TagRepository tagRepository;
    private final BoekRepository boekRepository;

    public TagSeed(TagRepository tagRepository, BoekRepository boekRepository) {
        this.tagRepository = tagRepository;
        this.boekRepository = boekRepository;
    }

    @Override
    @Transactional
    public void run(String... args) {
        if (tagRepository.count() == 0) {
            seedGenresMetHierarchie();
            seedFlatTags(TagType.THEMATISCH, THEMATISCHE);
        }
        hernoemTags(TagType.DIDACTISCH, HERNOEM_DIDACTISCH);
        seedMissingTags(TagType.DIDACTISCH, DIDACTISCHE);
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

    private void hernoemTags(TagType type, java.util.Map<String, String> mapping) {
        for (var entry : mapping.entrySet()) {
            var oudeOpt = tagRepository.findByTypeAndNaamIgnoreCase(type, entry.getKey());
            if (oudeOpt.isEmpty()) continue;
            Tag oudeTag = oudeOpt.get();

            var nieuweOpt = tagRepository.findByTypeAndNaamIgnoreCase(type, entry.getValue());
            if (nieuweOpt.isPresent()) {
                // Beide tags bestaan — hang eerst alle boeken van de oude tag op de
                // nieuwe tag (Set voorkomt duplicaten), pas dan kan de oude weg
                // zonder FK-conflict op boek_tags.
                Tag nieuweTag = nieuweOpt.get();
                List<Boek> boekenMetOude = boekRepository.findAllMetTag(oudeTag.getId());
                for (Boek b : boekenMetOude) {
                    b.getTags().remove(oudeTag);
                    b.getTags().add(nieuweTag);
                }
                boekRepository.saveAll(boekenMetOude);
                tagRepository.delete(oudeTag);
            } else {
                oudeTag.setNaam(entry.getValue());
                tagRepository.save(oudeTag);
            }
        }
    }

    private void seedMissingTags(TagType type, String[] namen) {
        for (String naam : namen) {
            if (!tagRepository.existsByTypeAndNaamIgnoreCase(type, naam)) {
                tagRepository.save(new Tag(naam, type));
            }
        }
    }
}
