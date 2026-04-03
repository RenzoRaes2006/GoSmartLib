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

        // Voor lokaal testen: uncomment onderstaande 2 regels en comment de return
        // eronder
        String baseUrl = request.getScheme() + "://" + request.getServerName() + ":" + request.getServerPort();
        return ResponseEntity.ok(baseUrl + "/api/images/" + bestandsnaam);
        // return ResponseEntity.ok("/api/images/" + bestandsnaam);
    }

    @GetMapping("/{bestandsnaam}")
    public ResponseEntity<Resource> getImage(@PathVariable String bestandsnaam) throws IOException {
        Path bestandspad = uploadDirectory.resolve(bestandsnaam).normalize();

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
