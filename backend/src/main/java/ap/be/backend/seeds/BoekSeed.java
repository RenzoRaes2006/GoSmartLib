package ap.be.backend.seeds;

import java.util.List;
import java.util.Map;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import ap.be.backend.entity.Boek;
import ap.be.backend.repository.BoekRepository;

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
            try {
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
            } catch (Exception e) {
                System.out.println("Google Books API niet beschikbaar, fallback boeken worden toegevoegd: " + e.getMessage());
                String[][] boeken = {
                    {"De Hobbit", "J.R.R. Tolkien", "9780261102217", "https://covers.openlibrary.org/b/isbn/9780261102217-M.jpg"},
                    {"Harry Potter en de Steen der Wijzen", "J.K. Rowling", "9789022326626", "https://covers.openlibrary.org/b/isbn/9789022326626-M.jpg"},
                    {"1984", "George Orwell", "9789044965544", "https://covers.openlibrary.org/b/isbn/9789044965544-M.jpg"},
                    {"De Kleine Prins", "Antoine de Saint-Exupéry", "9789047600183", "https://covers.openlibrary.org/b/isbn/9789047600183-M.jpg"},
                    {"Het Dagboek van een Jonge Meid", "Anne Frank", "9789041741707", "https://covers.openlibrary.org/b/isbn/9789041741707-M.jpg"}
                };
                for (String[] b : boeken) {
                    Boek boek = new Boek();
                    boek.setTitel(b[0]);
                    boek.setAuteur(b[1]);
                    boek.setISBN(b[2]);
                    boek.setAfbeelding(b[3]);
                    boek.setAantalExemplaren("5");
                    boek.setLocatie("Ekeren");
                    boekRepository.save(boek);
                }
            }
        }
    }
}