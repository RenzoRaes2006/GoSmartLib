package ap.be.backend.service;

import ap.be.backend.dto.OpenLeningPreviewDTO;
import ap.be.backend.dto.RolloverPreviewDTO;
import ap.be.backend.dto.RolloverResultaatDTO;
import ap.be.backend.dto.SchooljaarDTO;
import ap.be.backend.entity.*;
import ap.be.backend.exception.OpenLeningenBlokkadeException;
import ap.be.backend.exception.SchoolNotFoundException;
import ap.be.backend.exception.SchooljaarNotFoundException;
import ap.be.backend.mappers.SchooljaarMapper;
import ap.be.backend.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Beheert schooljaren en voert de jaarlijkse rollover uit.
 *
 * De rollover doet het volgende in volgorde:
 *   1. OneRoster synchroniseren → nieuwe leerlingen/klassen binnenhalen
 *   2. Vertrokken leerlingen detecteren (actief in DB maar niet in OneRoster)
 *   3. Open uitleningen van vertrekkende leerlingen controleren
 *      → blokkeer rollover of markeer ze als VERLOREN (keuze admin)
 *   4. Vertrokken leerlingen op actief=false zetten
 *   5. Reviews van vertrokken leerlingen anonimiseren
 *   6. KlassLeeslijsten archiveren (klas-gebonden + leerling-gebonden van vertrokkenen)
 *   7. Nieuw schooljaar aanmaken, huidig deactiveren
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SchooljaarService {

    private final SchooljaarRepository schooljaarRepository;
    private final SchoolRepository schoolRepository;
    private final UserRepository userRepository;
    private final BeoordelingRepository beoordelingRepository;
    private final KlassLeeslijstRepository leeslijstRepository;
    private final UitleningRepository uitleningRepository;
    private final OneRosterImportService oneRosterImportService;
    private final AuditService auditService;

    /**
     * Geeft een preview van wat er gaat veranderen bij een rollover,
     * zonder iets aan te passen in de database.
     */
    @Transactional(readOnly = true)
    public RolloverPreviewDTO previewRollover(Long schoolId) {
        School school = haalSchoolOp(schoolId);

        // Haal de huidige leerlingen op uit OneRoster zonder ze op te slaan
        List<java.util.Map<String, Object>> oneRosterLeerlingen =
                oneRosterImportService.fetchGebruikers(schoolId).get("leerlingen");

        Set<String> gevondenIds = extractSmartschoolIds(oneRosterLeerlingen);

        if (gevondenIds.isEmpty()) {
            throw new IllegalStateException(
                    "OneRoster stuurde geen leerlingen terug voor school " + schoolId +
                    ". Rollover geannuleerd om te vermijden dat alle leerlingen als vertrokken worden gemarkeerd.");
        }

        // Vertrokken leerlingen = actief in DB maar niet in OneRoster
        List<User> vertrokken = userRepository.findVertrokkenLeerlingen(schoolId, gevondenIds);
        List<Long> vertrokkenIds = vertrokken.stream().map(User::getId).toList();

        // Open uitleningen van vertrekkende leerlingen
        List<Uitlening> openLeningen = vertrokkenIds.isEmpty()
                ? List.of()
                : uitleningRepository.findByUser_IdInAndIsIngeleverdFalse(vertrokkenIds);

        List<OpenLeningPreviewDTO> openLeningDTOs = openLeningen.stream()
                .map(u -> new OpenLeningPreviewDTO(
                        u.getId(),
                        u.getUser().getSmartschoolId(),
                        u.getBoek().getTitel(),
                        u.getBoek().getIsbn()))
                .toList();

        // Klas-leeslijsten die gearchiveerd worden: alle actieve klas-leeslijsten van leerkrachten van deze school
        List<Long> leerkrachtIds = userRepository.findBySchool_IdAndRol(schoolId, Rol.LEERKRACHT)
                .stream().map(User::getId).toList();

        int aantalKlasLeeslijsten = leerkrachtIds.isEmpty() ? 0 :
                leeslijstRepository.findByLeerkracht_IdInAndKlasIsNotNullAndGearchiveerdFalse(leerkrachtIds).size();

        return new RolloverPreviewDTO(
                vertrokken.size(),
                openLeningDTOs,
                aantalKlasLeeslijsten,
                openLeningen.isEmpty()
        );
    }

    /**
     * Voert de schooljaar-rollover uit.
     * forceerVerloren=true → open uitleningen van vertrekkende leerlingen worden als VERLOREN gemarkeerd.
     * forceerVerloren=false → rollover wordt geblokkeerd als er open uitleningen zijn.
     */
    @Transactional
    public RolloverResultaatDTO voerRolloverUit(
            Long schoolId,
            boolean forceerVerloren,
            String nieuwNaam,
            LocalDate nieuwStartDatum,
            LocalDate nieuwEindDatum) {

        haalSchoolOp(schoolId);

        // --- Stap 1: OneRoster synchroniseren ---
        // Haalt nieuwe leerlingen en klassen op en updatet de database.
        // De set gevondenLeerlingSmartschoolIds bevat alle smartschoolIds die OneRoster terugstuurde.
        OneRosterImportService.ImportResultaat importResultaat = oneRosterImportService.importSchool(schoolId);
        Set<String> gevondenIds = importResultaat.gevondenLeerlingSmartschoolIds();

        if (gevondenIds.isEmpty()) {
            throw new IllegalStateException(
                    "OneRoster stuurde geen leerlingen terug. Rollover geannuleerd.");
        }

        // --- Stap 2: Vertrokken leerlingen detecteren ---
        List<User> vertrokken = userRepository.findVertrokkenLeerlingen(schoolId, gevondenIds);
        List<Long> vertrokkenIds = vertrokken.stream().map(User::getId).toList();

        log.info("Rollover school {}: {} leerlingen vertrokken", schoolId, vertrokken.size());

        // --- Stap 3: Open uitleningen afhandelen ---
        List<Uitlening> openLeningen = vertrokkenIds.isEmpty()
                ? List.of()
                : uitleningRepository.findByUser_IdInAndIsIngeleverdFalse(vertrokkenIds);

        if (!openLeningen.isEmpty() && !forceerVerloren) {
            throw new OpenLeningenBlokkadeException(openLeningen.size());
        }

        int aantalVerloren = 0;
        if (!openLeningen.isEmpty()) {
            openLeningen.forEach(u -> { u.setStatus("VERLOREN"); u.setIngeleverd(true); });
            uitleningRepository.saveAll(openLeningen);
            aantalVerloren = openLeningen.size();
            log.info("Rollover school {}: {} uitleningen als VERLOREN gemarkeerd", schoolId, aantalVerloren);
        }

        // --- Stap 4: Vertrokken leerlingen deactiveren ---
        LocalDate vandaag = LocalDate.now();
        vertrokken.forEach(l -> {
            l.setActief(false);
            l.setVertrokkenOp(vandaag);
            auditService.logSysteem(AuditActie.GEBRUIKER_VERTROKKEN_SYNC, "Gebruiker", l.getId(),
                    AuditService.details("smartschoolId", l.getSmartschoolId(),
                            "rol", l.getRol() == null ? null : l.getRol().name(),
                            "klas", l.getKlas(), "bron", "ROLLOVER"));
        });
        userRepository.saveAll(vertrokken);

        // --- Stap 5: Reviews van vertrokken leerlingen anonimiseren ---
        // De review blijft zichtbaar op het boek maar zonder naam van de leerling.
        int aantalGeanonimiseerd = 0;
        if (!vertrokkenIds.isEmpty()) {
            List<Beoordeling> teAnonimiseren =
                    beoordelingRepository.findByUser_IdInAndIsAnoniemFalse(vertrokkenIds);
            teAnonimiseren.forEach(b -> b.setAnoniem(true));
            beoordelingRepository.saveAll(teAnonimiseren);
            aantalGeanonimiseerd = teAnonimiseren.size();
            log.info("Rollover school {}: {} beoordelingen geanonimiseerd", schoolId, aantalGeanonimiseerd);
        }

        // --- Stap 6: KlassLeeslijsten archiveren ---
        // Alle klas-gebonden leeslijsten van leerkrachten van deze school worden gearchiveerd:
        // de klassen zijn volgend jaar anders samengesteld, dus de lijsten zijn niet meer actueel.
        // Leerling-gebonden leeslijsten worden NIET gearchiveerd: vertrokken leerlingen kunnen
        // niet meer inloggen, dus hun lijsten zijn al onbereikbaar — archiveren zou alleen het
        // archief van de bibbeheerder vervuilen.
        int aantalGearchiveerd = 0;

        List<Long> leerkrachtIds = userRepository.findBySchool_IdAndRol(schoolId, Rol.LEERKRACHT)
                .stream().map(User::getId).toList();

        if (!leerkrachtIds.isEmpty()) {
            List<KlassLeeslijst> klasLeeslijsten =
                    leeslijstRepository.findByLeerkracht_IdInAndKlasIsNotNullAndGearchiveerdFalse(leerkrachtIds);
            klasLeeslijsten.forEach(l -> l.setGearchiveerd(true));
            leeslijstRepository.saveAll(klasLeeslijsten);
            aantalGearchiveerd += klasLeeslijsten.size();
        }

        log.info("Rollover school {}: {} leeslijsten gearchiveerd", schoolId, aantalGearchiveerd);

        // --- Stap 7: Nieuw schooljaar aanmaken, huidig deactiveren ---
        schooljaarRepository.findByActiefTrue().ifPresent(huidig -> {
            huidig.setActief(false);
            schooljaarRepository.save(huidig);
        });

        Schooljaar nieuwSchooljaar = new Schooljaar(nieuwNaam, nieuwStartDatum, nieuwEindDatum, true);
        schooljaarRepository.save(nieuwSchooljaar);

        auditService.log(AuditActie.ROLLOVER_UITGEVOERD, "School", schoolId,
                AuditService.details("nieuwSchooljaar", nieuwNaam,
                        "vertrokken", vertrokken.size(),
                        "verlorenUitleningen", aantalVerloren,
                        "geanonimiseerdeReviews", aantalGeanonimiseerd,
                        "gearchiveerdeLeeslijsten", aantalGearchiveerd));

        log.info("Rollover school {} voltooid. Nieuw schooljaar: {}", schoolId, nieuwNaam);

        return new RolloverResultaatDTO(
                nieuwNaam,
                vertrokken.size(),
                aantalGeanonimiseerd,
                aantalGearchiveerd,
                aantalVerloren
        );
    }

    @Transactional(readOnly = true)
    public List<SchooljaarDTO> getAllSchooljaren() {
        return schooljaarRepository.findAll().stream()
                .sorted((a, b) -> b.getStartDatum().compareTo(a.getStartDatum()))
                .map(SchooljaarMapper::toDTO)
                .toList();
    }

    @Transactional(readOnly = true)
    public SchooljaarDTO getActiefSchooljaar() {
        return schooljaarRepository.findByActiefTrue()
                .map(SchooljaarMapper::toDTO)
                .orElseThrow(SchooljaarNotFoundException::new);
    }

    private School haalSchoolOp(Long schoolId) {
        return schoolRepository.findById(schoolId)
                .orElseThrow(() -> new SchoolNotFoundException(schoolId));
    }

    private Set<String> extractSmartschoolIds(List<java.util.Map<String, Object>> rijen) {
        return rijen.stream()
                .map(r -> String.valueOf(r.get("id")))
                .filter(id -> id != null && !id.isBlank() && !"null".equals(id))
                .collect(Collectors.toSet());
    }
}
