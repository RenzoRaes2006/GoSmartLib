package ap.be.backend.seeds;

import java.util.List;
import java.util.Map;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import ap.be.backend.BoekRepository;
import ap.be.backend.entity.Boek;

@Component
public class BoekSeed implements CommandLineRunner {

    private final BoekRepository boekRepository;
    private final RestTemplate restTemplate;

    // Spring injecteert de repository automatisch via de constructor
    public BoekSeed(BoekRepository boekRepository, RestTemplate restTemplate) {
        this.boekRepository = boekRepository;
        this.restTemplate = restTemplate;
    }

    @Override
    public void run(String... args) throws Exception {
        if (boekRepository.count() == 0) {
            // 1. Haal data op van Google Books (bijvoorbeeld boeken over 'Java
            // programming')
            String url = "https://www.googleapis.com/books/v1/volumes?q=maxResults=10";
            Map<String, Object> response = restTemplate.getForObject(url, Map.class);

            List<Map<String, Object>> items = (List<Map<String, Object>>) response.get("items");

            if (items != null) {
                for (Map<String, Object> item : items) {
                    Map<String, Object> volumeInfo = (Map<String, Object>) item.get("volumeInfo");

                    Boek nieuwBoek = new Boek();
                    nieuwBoek.setTitel((String) volumeInfo.get("title"));

                    // Auteurs is een lijst, we pakken de eerste
                    List<String> auteurs = (List<String>) volumeInfo.get("authors");
                    nieuwBoek.setAuteur(auteurs != null ? auteurs.get(0) : "Onbekende Auteur");

                    nieuwBoek.setPagina((Integer) volumeInfo.get("pageCount"));

                    // Afbeelding van de API
                    Map<String, String> imageLinks = (Map<String, String>) volumeInfo.get("imageLinks");
                    if (imageLinks != null) {
                        nieuwBoek.setAfbeelding(imageLinks.get("smallThumbnail"));
                    }

                    // ISBN toewijzen
                    List<Map<String, String>> identifiers = (List<Map<String, String>>) volumeInfo
                            .get("industryIdentifiers");

                    if (identifiers != null) {
                        for (Map<String, String> idMap : identifiers) {
                            if ("ISBN_13".equals(idMap.get("type"))) {
                                nieuwBoek.setISBN(idMap.get("identifier"));
                                break;
                            } else if ("ISBN_10".equals(idMap.get("type"))) {
                                nieuwBoek.setISBN(idMap.get("identifier"));
                            } else if ("OTHER".equals(idMap.get("type"))) {
                                nieuwBoek.setISBN(idMap.get("identifier"));
                            }
                        }

                        nieuwBoek.setAantalExemplaren("5"); // Standaard waarde
                        nieuwBoek.setLocatie("Ekeren");

                        boekRepository.save(nieuwBoek);
                    }
                }
            }

        }
    }
}