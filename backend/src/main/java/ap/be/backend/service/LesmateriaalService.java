package ap.be.backend.service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import ap.be.backend.config.SecurityUtils;
import ap.be.backend.dto.LesmateriaalDTO;
import ap.be.backend.entity.Boek;
import ap.be.backend.entity.Lesmateriaal;
import ap.be.backend.entity.User;
import ap.be.backend.exception.BoekNotFoundException;
import ap.be.backend.repository.BoekRepository;
import ap.be.backend.repository.LesmateriaalRepository;
import ap.be.backend.repository.UserRepository;
import jakarta.annotation.PostConstruct;

@Service
public class LesmateriaalService {

    @Value("${upload.path.lesmateriaal:uploads/lesmateriaal}")
    private String bestandsPad;

    private Path uploadDirectory;

    private final LesmateriaalRepository lesmateriaalRepository;
    private final BoekRepository boekRepository;
    private final UserRepository userRepository;
    private final OneRosterService oneRosterService;

    public LesmateriaalService(LesmateriaalRepository lesmateriaalRepository,
            BoekRepository boekRepository,
            UserRepository userRepository,
            OneRosterService oneRosterService) {
        this.lesmateriaalRepository = lesmateriaalRepository;
        this.boekRepository = boekRepository;
        this.userRepository = userRepository;
        this.oneRosterService = oneRosterService;
    }

    @PostConstruct
    public void init() throws IOException {
        uploadDirectory = Paths.get(bestandsPad).toAbsolutePath();
        Files.createDirectories(uploadDirectory);
    }

    public LesmateriaalDTO upload(Long boekId, MultipartFile file, String beschrijving) throws IOException {
        Boek boek = boekRepository.findById(boekId)
                .orElseThrow(() -> new BoekNotFoundException("Boek niet gevonden: " + boekId));
        User uploader = SecurityUtils.getCurrentUser(userRepository);

        String origineleNaam = file.getOriginalFilename() != null ? file.getOriginalFilename() : "bestand";
        String extensie = "";
        int dot = origineleNaam.lastIndexOf('.');
        if (dot >= 0 && dot < origineleNaam.length() - 1) {
            extensie = origineleNaam.substring(dot);
        }
        String opgeslagenNaam = UUID.randomUUID().toString() + extensie;

        Files.copy(file.getInputStream(), uploadDirectory.resolve(opgeslagenNaam));

        Lesmateriaal lesmateriaal = new Lesmateriaal();
        lesmateriaal.setBoek(boek);
        lesmateriaal.setUploader(uploader);
        lesmateriaal.setOrigineleBestandsnaam(origineleNaam);
        lesmateriaal.setOpgeslagenBestandsnaam(opgeslagenNaam);
        lesmateriaal.setContentType(file.getContentType() != null ? file.getContentType() : "application/octet-stream");
        lesmateriaal.setGrootte(file.getSize());
        lesmateriaal.setBeschrijving(beschrijving);

        Lesmateriaal saved = lesmateriaalRepository.save(lesmateriaal);
        return toDTO(saved, uploader);
    }

    public List<LesmateriaalDTO> listVoorBoek(Long boekId) {
        User huidige = SecurityUtils.getCurrentUser(userRepository);
        return lesmateriaalRepository.findByBoekIdOrderByUploadedAtDesc(boekId).stream()
                .map(l -> toDTO(l, huidige))
                .toList();
    }

    public DownloadBundle download(Long lesmateriaalId) throws IOException {
        Lesmateriaal l = lesmateriaalRepository.findById(lesmateriaalId)
                .orElseThrow(() -> new IllegalArgumentException("Lesmateriaal niet gevonden"));
        Path pad = uploadDirectory.resolve(l.getOpgeslagenBestandsnaam()).normalize();
        if (!pad.startsWith(uploadDirectory)) {
            throw new IllegalArgumentException("Ongeldig pad");
        }
        Resource resource = new UrlResource(pad.toUri());
        if (!resource.exists()) {
            throw new IllegalArgumentException("Bestand niet gevonden");
        }
        return new DownloadBundle(resource, l.getContentType(), l.getOrigineleBestandsnaam());
    }

    public LesmateriaalDTO updateBeschrijving(Long lesmateriaalId, String beschrijving) {
        Lesmateriaal l = lesmateriaalRepository.findById(lesmateriaalId)
                .orElseThrow(() -> new IllegalArgumentException("Lesmateriaal niet gevonden"));
        User huidige = SecurityUtils.getCurrentUser(userRepository);
        if (!l.getUploader().getId().equals(huidige.getId())) {
            throw new SecurityException("Je kunt enkel je eigen lesmateriaal wijzigen.");
        }
        l.setBeschrijving(beschrijving);
        return toDTO(lesmateriaalRepository.save(l), huidige);
    }

    public void delete(Long lesmateriaalId) throws IOException {
        Lesmateriaal l = lesmateriaalRepository.findById(lesmateriaalId)
                .orElseThrow(() -> new IllegalArgumentException("Lesmateriaal niet gevonden"));
        User huidige = SecurityUtils.getCurrentUser(userRepository);
        if (!l.getUploader().getId().equals(huidige.getId())) {
            throw new SecurityException("Je kunt enkel je eigen lesmateriaal verwijderen.");
        }
        Path pad = uploadDirectory.resolve(l.getOpgeslagenBestandsnaam()).normalize();
        if (pad.startsWith(uploadDirectory)) {
            Files.deleteIfExists(pad);
        }
        lesmateriaalRepository.delete(l);
    }

    private LesmateriaalDTO toDTO(Lesmateriaal l, User huidige) {
        User uploader = l.getUploader();
        String naam = uploader.getSmartschoolId();
        if (uploader.getSmartschoolId() != null) {
            String naamUitOneRoster = oneRosterService.getNaamBySourcedId(uploader.getSmartschoolId());
            if (naamUitOneRoster != null) {
                naam = naamUitOneRoster;
            }
        }
        return new LesmateriaalDTO(
                l.getId(),
                l.getBoek().getId(),
                uploader.getId(),
                naam,
                l.getOrigineleBestandsnaam(),
                l.getContentType(),
                l.getGrootte(),
                l.getBeschrijving(),
                l.getUploadedAt(),
                huidige != null && uploader.getId().equals(huidige.getId()));
    }

    public record DownloadBundle(Resource resource, String contentType, String origineleBestandsnaam) {
    }
}
