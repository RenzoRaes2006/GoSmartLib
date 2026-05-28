package ap.be.backend.controller;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import ap.be.backend.dto.LesmateriaalDTO;
import ap.be.backend.service.LesmateriaalService;

@RestController
@PreAuthorize("hasAnyRole('TEACHER', 'BEHEERDER', 'ADMIN')")
public class LesmateriaalController {

    private final LesmateriaalService lesmateriaalService;

    public LesmateriaalController(LesmateriaalService lesmateriaalService) {
        this.lesmateriaalService = lesmateriaalService;
    }

    @GetMapping("/api/boeken/{boekId}/lesmateriaal")
    public List<LesmateriaalDTO> list(@PathVariable Long boekId) {
        return lesmateriaalService.listVoorBoek(boekId);
    }

    @PostMapping("/api/boeken/{boekId}/lesmateriaal")
    public ResponseEntity<LesmateriaalDTO> upload(
            @PathVariable Long boekId,
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "beschrijving", required = false) String beschrijving) throws IOException {
        if (file == null || file.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }
        LesmateriaalDTO dto = lesmateriaalService.upload(boekId, file, beschrijving);
        return ResponseEntity.status(HttpStatus.CREATED).body(dto);
    }

    @GetMapping("/api/lesmateriaal/{id}/download")
    public ResponseEntity<Resource> download(@PathVariable Long id) throws IOException {
        LesmateriaalService.DownloadBundle bundle = lesmateriaalService.download(id);
        String encodedNaam = URLEncoder.encode(bundle.origineleBestandsnaam(), StandardCharsets.UTF_8)
                .replace("+", "%20");
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename*=UTF-8''" + encodedNaam)
                .contentType(MediaType.parseMediaType(bundle.contentType()))
                .body(bundle.resource());
    }

    @PutMapping("/api/lesmateriaal/{id}")
    public LesmateriaalDTO updateBeschrijving(
            @PathVariable Long id,
            @RequestBody UpdateBeschrijvingRequest request) {
        return lesmateriaalService.updateBeschrijving(id, request.beschrijving());
    }

    @DeleteMapping("/api/lesmateriaal/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) throws IOException {
        lesmateriaalService.delete(id);
        return ResponseEntity.noContent().build();
    }

    public record UpdateBeschrijvingRequest(String beschrijving) {
    }
}
