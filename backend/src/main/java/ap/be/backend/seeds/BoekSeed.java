package ap.be.backend.seeds;

import java.util.List;
import java.util.Map;
import java.util.Random;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import ap.be.backend.entity.Boek;
import ap.be.backend.entity.Tag;
import ap.be.backend.entity.TagType;
import ap.be.backend.repository.BoekRepository;
import ap.be.backend.repository.TagRepository;

@Component
@Profile("dev")
public class BoekSeed implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(BoekSeed.class);

    private final BoekRepository boekRepository;
    private final TagRepository tagRepository;
    private final RestTemplate restTemplate;

    String[] locaties = { "GO! Spectrumschool", "GO! muzische basisschool K’DO", "GO! basisschool VONK!",
            "GO! lyceum Antwerpen", "MaGO! Antwerpen" };
    String[] genres = { "Non-fictie algemeen", "Fictie algemeen", "Literaire roman", "Spanning / thriller",
            "Detective / misdaad", "Fantasy", "Dystopie", "Historische roman", "Romantiek", "Coming-of-age",
            "Avontuur", "Oorlog & conflict", "Horror", "Humor", "Graphic novel / strip", "Poëzie",
            "Biografie / autobiografie", "Filosofie", "Geschiedenis", "Kunst & cultuur",
            "Maatschappij & politiek", "Psychologie", "Wetenschap & technologie" };
    String[] thematischeTags = { "Liefde & relaties", "Vriendschap", "Identiteit & zelfbeeld",
            "Gender & seksualiteit", "Diversiteit & inclusie", "Mentale gezondheid", "Rouw & verlies",
            "Familie", "School & prestatiedruk", "Sociale media", "Migratie & afkomst",
            "Armoede & ongelijkheid", "Macht & onrecht", "Avontuur & ontdekking", "Overleven",
            "Toekomst & technologie" };
    String[] didactischeTags = { "Lesmethodes", "Didactische handboeken", "Evaluatiemateriaal", "Naslagwerken" };
    Random random = new Random();

    public BoekSeed(BoekRepository boekRepository, TagRepository tagRepository, RestTemplate restTemplate) {
        this.boekRepository = boekRepository;
        this.tagRepository = tagRepository;
        this.restTemplate = restTemplate;
    }

    private Tag findOrCreateTag(TagType type, String naam) {
        return tagRepository.findByTypeAndNaamIgnoreCase(type, naam)
                .orElseGet(() -> tagRepository.save(new Tag(naam, type)));
    }

    @Override
    public void run(String... args) throws Exception {
        if (boekRepository.count() == 0) {
            // 1. Haal data op van Google Books (bijvoorbeeld boeken over 'Java
            // programming')
            String url = "https://www.googleapis.com/books/v1/volumes?q=maxResults=10";
            Map<String, Object> response;
            try {
                response = restTemplate.getForObject(url, Map.class);
            } catch (RestClientException e) {
                log.warn("BoekSeed overgeslagen: Google Books API onbereikbaar ({}). App start verder zonder seed-boeken.",
                        e.getMessage());
                return;
            }
            if (response == null) {
                log.warn("BoekSeed overgeslagen: lege respons van Google Books API.");
                return;
            }

            List<Map<String, Object>> items = (List<Map<String, Object>>) response.get("items");

            if (items != null) {
                for (Map<String, Object> item : items) {
                    Map<String, Object> volumeInfo = (Map<String, Object>) item.get("volumeInfo");

                    Boek nieuwBoek = new Boek();
                    nieuwBoek.setTitel((String) volumeInfo.get("title"));

                    // Auteurs
                    List<String> auteurs = (List<String>) volumeInfo.get("authors");
                    nieuwBoek.setAuteur(auteurs != null ? auteurs.get(0) : "Onbekende Auteur");

                    // Pagina's (Check op null om NullPointerException te voorkomen)
                    Integer pages = (Integer) volumeInfo.get("pageCount");
                    nieuwBoek.setPagina(pages != null ? pages : 0);

                    // Genres: wijs 1-2 random genres toe uit de vaste lijst
                    nieuwBoek.getTags().add(findOrCreateTag(TagType.GENRE, genres[random.nextInt(genres.length)]));
                    nieuwBoek.getTags().add(findOrCreateTag(TagType.GENRE, genres[random.nextInt(genres.length)]));

                    // Thematische tags: wijs 1-2 random tags toe
                    nieuwBoek.getTags().add(findOrCreateTag(TagType.THEMATISCH,
                            thematischeTags[random.nextInt(thematischeTags.length)]));
                    nieuwBoek.getTags().add(findOrCreateTag(TagType.THEMATISCH,
                            thematischeTags[random.nextInt(thematischeTags.length)]));

                    // Afbeelding
                    Map<String, String> imageLinks = (Map<String, String>) volumeInfo.get("imageLinks");
                    if (imageLinks != null) {
                        nieuwBoek.setAfbeelding(imageLinks.get("smallThumbnail"));
                    }

                    // ISBN toewijzen
                    List<Map<String, String>> identifiers = (List<Map<String, String>>) volumeInfo
                            .get("industryIdentifiers");
                    if (identifiers != null) {
                        for (Map<String, String> idMap : identifiers) {
                            String type = idMap.get("type");
                            String id = idMap.get("identifier");
                            if ("ISBN_13".equals(type) || "ISBN_10".equals(type) || "OTHER".equals(type)) {
                                nieuwBoek.setIsbn(id);
                            }
                        }
                    }

                    // Random aantal exemplaren
                    nieuwBoek.setAantalExemplaren(random.nextInt(11));

                    // Ongeveer 1 op 4 boeken markeren als didactisch materiaal.
                    boolean isDidactisch = random.nextDouble() < 0.25;
                    nieuwBoek.setDidactischMateriaal(isDidactisch);
                    if (isDidactisch) {
                        nieuwBoek.getTags().add(findOrCreateTag(TagType.DIDACTISCH,
                                didactischeTags[random.nextInt(didactischeTags.length)]));
                    }

                    // Random locatie
                    nieuwBoek.setLocatie(locaties[random.nextInt(locaties.length)]);

                    // Beschrijving
                    String beschrijving = (String) volumeInfo.get("description");
                    nieuwBoek.setBeschrijving(beschrijving != null ? beschrijving : "Geen beschrijving beschikbaar.");

                    // 2. Uitgave Jaar
                    String fullDate = (String) volumeInfo.get("publishedDate");
                    if (fullDate != null && fullDate.length() >= 4) {
                        nieuwBoek.setUitgaveJaar(fullDate.substring(0, 4));
                    } else {
                        nieuwBoek.setUitgaveJaar("Onbekend");
                    }

                    // 3. Uitgever
                    String uitgever = (String) volumeInfo.get("publisher");
                    nieuwBoek.setUitgever(uitgever != null ? uitgever : "Onbekende uitgever");

                    // Opslaan
                    boekRepository.save(nieuwBoek);
                }
            }
        }

    }
}
