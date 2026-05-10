package ap.be.backend.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.HttpServletRequest;

import java.io.IOException;
import java.nio.file.*;
import java.util.List;
import java.util.UUID;

/**
 * Behandelt het uploaden en serveren van afbeeldingen voor boekomslagen.
 *
 * Geüploade afbeeldingen worden opgeslagen in de map die is geconfigureerd
 * via upload.path in application.properties. Elke afbeelding krijgt een
 * unieke bestandsnaam (UUID) om naamconflicten te voorkomen.
 *
 * Toegestane bestandstypen: JPEG en PNG.
 */
@RestController
@RequestMapping("/api/images")
public class ImageController {

    @Value("${upload.path}")
    private String bestandsPad;

    private Path uploadDirectory;

    @PostConstruct
    public void init() throws IOException {
        uploadDirectory = Paths.get(bestandsPad).toAbsolutePath();
        Files.createDirectories(uploadDirectory);
    }

    /**
     * Slaat een geüploade afbeelding op en geeft het relatieve pad terug.
     *
     * Het teruggegeven pad kan direct worden opgeslagen als afbeeldings-URL
     * bij een boek. Alleen JPEG en PNG zijn toegestaan.
     *
     * @param file    de te uploaden afbeelding
     * @param request het HTTP-verzoek (gereserveerd voor toekomstige base-URL-logica)
     * @return het relatieve pad naar de opgeslagen afbeelding
     * @throws IOException als het opslaan van het bestand mislukt
     */
    @PostMapping("/upload")
    public ResponseEntity<String> upload(@RequestParam("file") MultipartFile file, HttpServletRequest request)
            throws IOException {
        String contentType = file.getContentType();
        if (contentType == null || !List.of("image/jpeg", "image/png").contains(contentType)) {
            return ResponseEntity.badRequest().body("Alleen JPEG en PNG afbeeldingen zijn toegestaan.");
        }

        String extensie = contentType.equals("image/png") ? ".png" : ".jpg";
        String bestandsnaam = UUID.randomUUID().toString() + extensie;

        Files.copy(file.getInputStream(), uploadDirectory.resolve(bestandsnaam));

        return ResponseEntity.ok("/api/images/" + bestandsnaam);
    }

    /**
     * Serveert een opgeslagen afbeelding op basis van de bestandsnaam.
     *
     * De padtraversal-controle (startsWith) voorkomt dat aanvallers via ".."
     * in de bestandsnaam bestanden buiten de uploadmap kunnen opvragen.
     *
     * @param bestandsnaam de naam van het op te halen bestand
     * @return de afbeelding als binaire response, of 404 als het bestand niet bestaat
     * @throws IOException als het lezen van het bestand mislukt
     */
    @GetMapping("/{bestandsnaam}")
    public ResponseEntity<Resource> getImage(@PathVariable String bestandsnaam) throws IOException {
        Path bestandspad = uploadDirectory.resolve(bestandsnaam).normalize();

        // Voorkomt padtraversal-aanvallen waarbij ".." in de URL wordt gebruikt.
        if (!bestandspad.startsWith(uploadDirectory)) {
            return ResponseEntity.badRequest().build();
        }

        Resource resource = new UrlResource(bestandspad.toUri());

        if (!resource.exists()) {
            return ResponseEntity.notFound().build();
        }

        String type = bestandsnaam.endsWith(".png") ? "image/png" : "image/jpeg";
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(type))
                .body(resource);
    }
}
