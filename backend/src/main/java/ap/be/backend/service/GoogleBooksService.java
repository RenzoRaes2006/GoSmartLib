package ap.be.backend.service;

import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import ap.be.backend.dto.GoogleBoekDTO;

/**
 * Praat met de Google Books API. De API-sleutel staat in een
 * omgevingsvariabele (GOOGLE_BOOKS_API_KEY) en komt zo nooit in git of in de
 * browser terecht.
 */
@Service
public class GoogleBooksService {

    private static final Logger log = LoggerFactory.getLogger(GoogleBooksService.class);
    private static final String API_URL = "https://www.googleapis.com/books/v1/volumes";

    private final RestTemplate restTemplate;

    @Value("${google.books.api-key:}")
    private String apiKey;

    public GoogleBooksService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @SuppressWarnings("unchecked")
    public GoogleBoekDTO zoekOpIsbn(String isbn) {
        UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(API_URL)
                .queryParam("q", "isbn:" + isbn);
        if (apiKey != null && !apiKey.isBlank()) {
            builder.queryParam("key", apiKey);
        }

        Map<String, Object> response;
        try {
            response = restTemplate.getForObject(builder.toUriString(), Map.class);
        } catch (RestClientException e) {
            log.warn("Google Books onbereikbaar voor ISBN {}: {}", isbn, e.getMessage());
            return null;
        }
        if (response == null) {
            return null;
        }

        List<Map<String, Object>> items = (List<Map<String, Object>>) response.get("items");
        if (items == null || items.isEmpty()) {
            return null;
        }

        Map<String, Object> volumeInfo = (Map<String, Object>) items.get(0).get("volumeInfo");
        if (volumeInfo == null) {
            return null;
        }

        List<String> auteurs = (List<String>) volumeInfo.get("authors");
        String auteur = auteurs != null ? String.join(", ", auteurs) : "";

        String publishedDate = tekst(volumeInfo.get("publishedDate"));
        String uitgaveJaar = publishedDate.length() >= 4 ? publishedDate.substring(0, 4) : publishedDate;

        Object pageCount = volumeInfo.get("pageCount");
        String pagina = pageCount != null ? pageCount.toString() : "";

        Map<String, Object> imageLinks = (Map<String, Object>) volumeInfo.get("imageLinks");
        String afbeelding = "";
        if (imageLinks != null) {
            Object thumb = imageLinks.getOrDefault("thumbnail", imageLinks.get("smallThumbnail"));
            if (thumb != null) {
                afbeelding = thumb.toString().replace("http://", "https://");
            }
        }

        return new GoogleBoekDTO(
                tekst(volumeInfo.get("title")),
                auteur,
                isbn,
                tekst(volumeInfo.get("publisher")),
                uitgaveJaar,
                pagina,
                tekst(volumeInfo.get("description")),
                afbeelding);
    }

    private String tekst(Object waarde) {
        return waarde != null ? waarde.toString() : "";
    }
}
