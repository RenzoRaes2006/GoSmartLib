package ap.be.backend.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import ap.be.backend.config.SecurityUtils;
import ap.be.backend.repository.UserRepository;
import ap.be.backend.service.GebruikerService;
import ap.be.backend.dto.GebruikerDTO;
import ap.be.backend.dto.LeenStatusResponse;
import ap.be.backend.dto.LeeslijstMapDTO;
import ap.be.backend.dto.UitleningDTO;
import ap.be.backend.entity.User;
import ap.be.backend.mappers.GebruikerMapper;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * Behandelt alle API-endpoints voor individuele gebruikersbewerkingen:
 * leeslijstmappen, uitleningen, aanvragen en leengeschiedenis.
 *
 * Endpoints voor het beheren van aanvragen zijn enkel toegankelijk voor
 * leerkrachten en beheerders. Overige endpoints zijn beschikbaar voor
 * elke ingelogde gebruiker.
 */
@RestController
@RequestMapping("/api/gebruiker")
public class GebruikerController {
    private final GebruikerService gebruikerService;
    private final UserRepository userRepository;

    public GebruikerController(GebruikerService gebruikerService, UserRepository userRepository) {
        this.gebruikerService = gebruikerService;
        this.userRepository = userRepository;
    }

    private Long currentUserId() {
        return SecurityUtils.getCurrentUser(userRepository).getId();
    }

    /**
     * Geeft alle leeslijstmappen terug van een gebruiker.
     *
     * @param id het database-ID van de gebruiker
     * @return lijst van mappen als DTO's
     */
    @GetMapping("/{id}/mappen")
    public ResponseEntity<List<LeeslijstMapDTO>> getAlleMappenVanGebruiker(@PathVariable Long id) {
        return ResponseEntity.ok(gebruikerService.getMappenVoorGebruiker(id));
    }

    /**
     * Voegt een boek toe aan een leeslijstmap van een gebruiker.
     * Als de map nog niet bestaat, wordt ze aangemaakt.
     *
     * @param gebruikerId het ID van de gebruiker
     * @param mapNaam     de naam van de map
     * @param boekId      het ID van het toe te voegen boek
     * @param emoji       een optioneel emoji-icoon voor de map (standaard: map-emoji)
     */
    @PostMapping("/{gebruikerId}/mappen/{mapNaam}/boeken/{boekId}")
    public ResponseEntity<String> voegBoekToeAanLijst(@PathVariable Long gebruikerId,
            @PathVariable String mapNaam,
            @PathVariable Long boekId,
            @RequestParam(required = false, defaultValue = "📁") String emoji) {
        gebruikerService.voegBoekToeAanMap(gebruikerId, mapNaam, boekId, emoji);
        return ResponseEntity.ok("Boek succesvol toegevoegd aan map");
    }

    /**
     * Verwijdert een boek uit een leeslijstmap van een gebruiker.
     *
     * @param gebruikerId het ID van de gebruiker
     * @param mapNaam     de naam van de map
     * @param boekId      het ID van het te verwijderen boek
     */
    @DeleteMapping("/{gebruikerId}/mappen/{mapNaam}/boeken/{boekId}")
    public ResponseEntity<String> verwijderBoekUitLijst(
            @PathVariable Long gebruikerId,
            @PathVariable String mapNaam,
            @PathVariable Long boekId) {
        gebruikerService.verwijderBoekUitMap(gebruikerId, mapNaam, boekId);
        return ResponseEntity.ok("Boek succesvol verwijderd uit map");
    }

    /**
     * Controleert of een gebruiker een bepaald boek al heeft geleend of aangevraagd.
     *
     * @param gebruikerId het ID van de gebruiker
     * @param boekId      het ID van het boek
     * @return de huidige leenstatus voor dit boek
     */
    @GetMapping("/{gebruikerId}/lenen/{boekId}/status")
    public ResponseEntity<LeenStatusResponse> checkLeenStatus(
            @PathVariable Long gebruikerId,
            @PathVariable Long boekId) {
        LeenStatusResponse status = gebruikerService.checkLeenStatus(gebruikerId, boekId);
        return ResponseEntity.ok(status);
    }

    /**
     * Dient een leenaanvraag in voor één boek.
     *
     * @param gebruikerId het ID van de gebruiker
     * @param boekId      het ID van het te lenen boek
     */
    @PostMapping("/{gebruikerId}/lenen/{boekId}")
    public ResponseEntity<String> leenBoek(
            @PathVariable Long gebruikerId,
            @PathVariable Long boekId) {
        gebruikerService.bevestigUitlening(gebruikerId, boekId);
        return ResponseEntity.ok("Boek succesvol geleend! Je hebt 1 maand de tijd om het terug te brengen.");
    }

    /**
     * Variant van checkLeenStatus die de ingelogde gebruiker uit de
     * SecurityContext (Smartschool JWT) haalt in plaats van uit de URL.
     * Hierdoor is een lening altijd gekoppeld aan het echte smartschool-account.
     */
    @GetMapping("/me/lenen/{boekId}/status")
    public ResponseEntity<LeenStatusResponse> checkLeenStatusVoorMij(@PathVariable Long boekId) {
        return ResponseEntity.ok(gebruikerService.checkLeenStatus(currentUserId(), boekId));
    }

    /**
     * Variant van leenBoek die de ingelogde gebruiker uit de SecurityContext
     * (Smartschool JWT) haalt. Voorkomt dat de frontend per ongeluk (of moedwillig)
     * een andere user-id meestuurt.
     */
    @PostMapping("/me/lenen/{boekId}")
    public ResponseEntity<String> leenBoekVoorMij(@PathVariable Long boekId) {
        gebruikerService.bevestigUitlening(currentUserId(), boekId);
        return ResponseEntity.ok("Boek succesvol geleend! Je hebt 1 maand de tijd om het terug te brengen.");
    }

    /**
     * Variant van leenBoekenBulk die de ingelogde gebruiker gebruikt.
     */
    @PostMapping("/me/lenen/bulk")
    public ResponseEntity<String> leenBoekenBulkVoorMij(@RequestBody List<Long> boekIds) {
        gebruikerService.bevestigBulkUitlening(currentUserId(), boekIds);
        return ResponseEntity
                .ok(boekIds.size() + " boeken succesvol geleend! Je hebt 1 maand de tijd om ze terug te brengen.");
    }

    /**
     * Dient leenaanvragen in voor meerdere boeken tegelijk.
     *
     * @param gebruikerId het ID van de gebruiker
     * @param boekIds     lijst van boek-ID's die geleend moeten worden
     */
    @PostMapping("/{gebruikerId}/lenen/bulk")
    public ResponseEntity<String> leenBoekenBulk(
            @PathVariable Long gebruikerId,
            @RequestBody List<Long> boekIds) {
        gebruikerService.bevestigBulkUitlening(gebruikerId, boekIds);
        return ResponseEntity
                .ok(boekIds.size() + " boeken succesvol geleend! Je hebt 1 maand de tijd om ze terug te brengen.");
    }

    /**
     * Geeft alle openstaande leenaanvragen terug. Alleen voor leerkrachten en beheerders.
     *
     * @return lijst van openstaande aanvragen als DTO's
     */
    @PreAuthorize("hasAnyRole('TEACHER', 'BEHEERDER')")
    @GetMapping("/aanvragen")
    public ResponseEntity<List<UitleningDTO>> getAlleAanvragen() {
        return ResponseEntity.ok(gebruikerService.getAlleAanvragenDTO());
    }

    /**
     * Keurt een leenaanvraag goed en start de uitleentermijn. Alleen voor leerkrachten
     * en beheerders.
     *
     * @param uitleningId het ID van de goed te keuren aanvraag
     */
    @PreAuthorize("hasAnyRole('TEACHER', 'BEHEERDER')")
    @PutMapping("/aanvragen/{uitleningId}/goedkeuren")
    public ResponseEntity<String> keurGoed(@PathVariable Long uitleningId) {
        gebruikerService.keurAanvraagGoed(uitleningId);
        return ResponseEntity.ok("Aanvraag goedgekeurd. Uitleentermijn is gestart!");
    }

    /**
     * Weigert een leenaanvraag en zet het exemplaar terug in de beschikbare voorraad.
     * Alleen voor leerkrachten en beheerders.
     *
     * @param uitleningId het ID van de te weigeren aanvraag
     */
    @PreAuthorize("hasAnyRole('TEACHER', 'BEHEERDER')")
    @PutMapping("/aanvragen/{uitleningId}/weigeren")
    public ResponseEntity<String> weiger(@PathVariable Long uitleningId) {
        gebruikerService.weigerAanvraag(uitleningId);
        return ResponseEntity.ok("Aanvraag geweigerd en boek is teruggezet in de voorraad.");
    }

    /**
     * Geeft de actieve leningen van een gebruiker terug.
     *
     * @param gebruikerId het ID van de gebruiker
     * @return lijst van actieve leningen als DTO's
     */
    @GetMapping("/{gebruikerId}/leningen")
    public ResponseEntity<List<UitleningDTO>> getActieveLeningen(@PathVariable Long gebruikerId) {
        return ResponseEntity.ok(gebruikerService.getActieveLeningenVanGebruikerDTO(gebruikerId));
    }

    /**
     * Verwijdert een lening en herstelt het bijbehorende exemplaar in de voorraad.
     *
     * @param uitleningId het ID van de te verwijderen lening
     */
    @DeleteMapping("/leningen/{uitleningId}")
    public ResponseEntity<String> verwijderLening(@PathVariable Long uitleningId) {
        gebruikerService.verwijderLening(uitleningId);
        return ResponseEntity.ok("Lening succesvol verwijderd en voorraad hersteld.");
    }

    /**
     * Verlengt de uitleentermijn van een actieve lening met 14 dagen.
     *
     * @param uitleningId het ID van de te verlengen lening
     */
    @PatchMapping("/leningen/{uitleningId}/verlengen")
    public ResponseEntity<String> verlengLening(@PathVariable("uitleningId") Long uitleningId) {
        gebruikerService.verlengUitlening(uitleningId);
        return ResponseEntity.ok("Uitleentermijn succesvol verlengd met 14 dagen.");
    }

    /**
     * Geeft de volledige leengeschiedenis van een gebruiker terug, inclusief
     * reeds ingeleverde boeken.
     *
     * @param gebruikerId het ID van de gebruiker
     * @return lijst van historische leningen als DTO's
     */
    @GetMapping("/{gebruikerId}/geschiedenis")
    public ResponseEntity<List<UitleningDTO>> getLeengeschiedenis(@PathVariable Long gebruikerId) {
        return ResponseEntity.ok(gebruikerService.getLeengeschiedenisDTO(gebruikerId));
    }

    /**
     * Zoekt een gebruiker op via zijn Smartschool-ID.
     *
     * @param smartschoolId de unieke Smartschool-identificatie
     * @return de gevonden gebruiker als DTO
     */
    @GetMapping("/by-smartschool/{smartschoolId}")
    public ResponseEntity<GebruikerDTO> getLeerlingBySmartschoolId(@PathVariable String smartschoolId) {
        User leerling = gebruikerService.getLeerlingBySmartschoolId(smartschoolId);
        return ResponseEntity.ok(GebruikerMapper.toDTO(leerling));
    }
}
