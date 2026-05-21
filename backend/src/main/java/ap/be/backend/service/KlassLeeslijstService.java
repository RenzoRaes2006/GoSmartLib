package ap.be.backend.service;

import ap.be.backend.config.SecurityUtils;
import ap.be.backend.dto.KlassLeeslijstRequestDTO;
import ap.be.backend.dto.KlassLeeslijstResponseDTO;
import ap.be.backend.entity.Boek;
import ap.be.backend.entity.KlassLeeslijst;
import ap.be.backend.entity.User;
import ap.be.backend.exception.BoekNotFoundException;
import ap.be.backend.exception.GebruikerNotFoundException;
import ap.be.backend.exception.KlassLeeslijstNotFoundException;
import ap.be.backend.mappers.KlassLeeslijstMapper;
import ap.be.backend.repository.BoekRepository;
import ap.be.backend.repository.KlassLeeslijstRepository;
import ap.be.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Beheert de klassen-leeslijsten die leerkrachten aanmaken.
 *
 * Een leeslijst kan optioneel gekoppeld worden aan een klas (string), aan een
 * individuele leerling, of aan geen van beide (privé concept van de leerkracht).
 * Een leeslijst is pas zichtbaar voor leerlingen als 'zichtbaar' op true staat.
 */
@Service
@RequiredArgsConstructor
public class KlassLeeslijstService {

    private final KlassLeeslijstRepository leeslijstRepository;
    private final UserRepository userRepository;
    private final BoekRepository boekRepository;

    /**
     * Maakt een nieuwe leeslijst aan voor een leerkracht.
     *
     * @param request      de aanmaakgegevens (naam verplicht; klas, leerlingId,
     *                     beschrijving en zichtbaar optioneel)
     * @param leerkrachtId het database-ID van de leerkracht
     * @return de aangemaakte leeslijst als DTO
     * @throws GebruikerNotFoundException als de leerkracht of leerling niet bestaat
     * @throws IllegalArgumentException   als geen of beide koppelingen zijn opgegeven
     */
    @Transactional
    public KlassLeeslijstResponseDTO createLeeslijst(KlassLeeslijstRequestDTO request, Long leerkrachtId) {
        boolean heeftKlas = request.klas() != null && !request.klas().isBlank();
        boolean heeftLeerlingen = request.leerlingIds() != null && !request.leerlingIds().isEmpty();

        if (heeftKlas && heeftLeerlingen) {
            throw new IllegalArgumentException("Een leeslijst kan niet tegelijk aan een klas en leerlingen gekoppeld worden.");
        }
        if (!heeftKlas && !heeftLeerlingen) {
            throw new IllegalArgumentException("Een leeslijst moet aan een klas of leerling(en) gekoppeld worden.");
        }
        if (heeftKlas && !userRepository.findDistinctKlassen().contains(request.klas())) {
            throw new IllegalArgumentException("Klas '" + request.klas() + "' bestaat niet.");
        }

        User leerkracht = userRepository.findById(leerkrachtId)
                .orElseThrow(() -> new GebruikerNotFoundException("Leerkracht niet gevonden"));

        KlassLeeslijst leeslijst = new KlassLeeslijst();
        leeslijst.setNaam(request.naam());
        leeslijst.setLeerkracht(leerkracht);
        leeslijst.setBeschrijving(request.beschrijving());
        leeslijst.setZichtbaar(Boolean.TRUE.equals(request.zichtbaar()));

        if (heeftKlas) {
            leeslijst.setKlas(request.klas());
        } else {
            Set<User> leerlingen = new HashSet<>();
            for (Long leerlingId : request.leerlingIds()) {
                User leerling = userRepository.findById(leerlingId)
                        .orElseThrow(() -> new GebruikerNotFoundException("Leerling niet gevonden: " + leerlingId));
                leerlingen.add(leerling);
            }
            leeslijst.setLeerlingen(leerlingen);
        }

        return KlassLeeslijstMapper.toDTO(leeslijstRepository.save(leeslijst));
    }

    /**
     * Maakt een nieuwe leeslijst aan voor de huidige ingelogde leerkracht.
     */
    @Transactional
    public KlassLeeslijstResponseDTO createLeeslijstVoorHuidigeLeerkracht(KlassLeeslijstRequestDTO request) {
        User leerkracht = SecurityUtils.getCurrentUser(userRepository);
        return createLeeslijst(request, leerkracht.getId());
    }

    /**
     * Geeft alle leeslijsten van de huidige ingelogde leerkracht terug.
     */
    @Transactional(readOnly = true)
    public List<KlassLeeslijstResponseDTO> getLeeslijstenVoorHuidigeLeerkracht() {
        User leerkracht = SecurityUtils.getCurrentUser(userRepository);
        return getLeeslijstenByLeerkracht(leerkracht.getId());
    }

    /**
     * Geeft alle actieve (niet-gearchiveerde) leeslijsten terug van een bepaalde leerkracht.
     */
    @Transactional(readOnly = true)
    public List<KlassLeeslijstResponseDTO> getLeeslijstenByLeerkracht(Long leerkrachtId) {
        return leeslijstRepository.findByLeerkrachtIdAndGearchiveerdFalseOrderByAangemaaktOpDesc(leerkrachtId).stream()
                .map(KlassLeeslijstMapper::toDTO)
                .toList();
    }

    /**
     * Voegt een boek toe aan een bestaande leeslijst.
     */
    @Transactional
    public KlassLeeslijstResponseDTO addBoekToLeeslijst(Long leeslijstId, Long boekId) {
        KlassLeeslijst leeslijst = leeslijstRepository.findById(leeslijstId)
                .orElseThrow(() -> new KlassLeeslijstNotFoundException("Leeslijst niet gevonden"));

        Boek boek = boekRepository.findById(boekId)
                .orElseThrow(() -> new BoekNotFoundException("Boek niet gevonden"));

        leeslijst.getBoeken().add(boek);
        return KlassLeeslijstMapper.toDTO(leeslijstRepository.save(leeslijst));
    }

    /**
     * Geeft een leeslijst terug op basis van het ID.
     */
    @Transactional(readOnly = true)
    public KlassLeeslijstResponseDTO getLeeslijstById(Long id) {
        KlassLeeslijst leeslijst = leeslijstRepository.findById(id)
                .orElseThrow(() -> new KlassLeeslijstNotFoundException("Leeslijst niet gevonden"));
        return KlassLeeslijstMapper.toDTO(leeslijst);
    }

    /**
     * Verwijdert een boek uit een leeslijst.
     */
    @Transactional
    public KlassLeeslijstResponseDTO removeBoekFromLeeslijst(Long leeslijstId, Long boekId) {
        KlassLeeslijst leeslijst = leeslijstRepository.findById(leeslijstId)
                .orElseThrow(() -> new KlassLeeslijstNotFoundException("Leeslijst niet gevonden"));

        Boek boek = boekRepository.findById(boekId)
                .orElseThrow(() -> new BoekNotFoundException("Boek niet gevonden"));

        leeslijst.getBoeken().remove(boek);

        return KlassLeeslijstMapper.toDTO(leeslijstRepository.save(leeslijst));
    }

    /**
     * Wijzigt de naam van een bestaande leeslijst.
     */
    @Transactional
    public KlassLeeslijstResponseDTO updateLeeslijstNaam(Long id, String nieuweNaam) {
        KlassLeeslijst leeslijst = leeslijstRepository.findById(id)
                .orElseThrow(() -> new KlassLeeslijstNotFoundException("Leeslijst niet gevonden"));
        leeslijst.setNaam(nieuweNaam);
        return KlassLeeslijstMapper.toDTO(leeslijstRepository.save(leeslijst));
    }

    /**
     * Wijzigt de beschrijving van een bestaande leeslijst. Een lege of null waarde
     * wist de beschrijving.
     */
    @Transactional
    public KlassLeeslijstResponseDTO updateBeschrijving(Long id, String nieuweBeschrijving) {
        KlassLeeslijst leeslijst = leeslijstRepository.findById(id)
                .orElseThrow(() -> new KlassLeeslijstNotFoundException("Leeslijst niet gevonden"));
        leeslijst.setBeschrijving(nieuweBeschrijving);
        return KlassLeeslijstMapper.toDTO(leeslijstRepository.save(leeslijst));
    }

    /**
     * Wijzigt de koppeling van een bestaande leeslijst. Net als bij aanmaken
     * moet exact één van klas of leerlingId opgegeven worden, en moet de klas
     * (als opgegeven) bestaan in de database.
     */
    @Transactional
    public KlassLeeslijstResponseDTO updateKoppeling(Long id, String nieuweKlas, List<Long> nieuweLeerlingIds) {
        boolean heeftKlas = nieuweKlas != null && !nieuweKlas.isBlank();
        boolean heeftLeerlingen = nieuweLeerlingIds != null && !nieuweLeerlingIds.isEmpty();

        if (heeftKlas && heeftLeerlingen) {
            throw new IllegalArgumentException("Een leeslijst kan niet tegelijk aan een klas en leerlingen gekoppeld worden.");
        }
        if (!heeftKlas && !heeftLeerlingen) {
            throw new IllegalArgumentException("Een leeslijst moet aan een klas of leerling(en) gekoppeld worden.");
        }
        if (heeftKlas && !userRepository.findDistinctKlassen().contains(nieuweKlas)) {
            throw new IllegalArgumentException("Klas '" + nieuweKlas + "' bestaat niet.");
        }

        KlassLeeslijst leeslijst = leeslijstRepository.findById(id)
                .orElseThrow(() -> new KlassLeeslijstNotFoundException("Leeslijst niet gevonden"));

        if (heeftKlas) {
            leeslijst.setKlas(nieuweKlas);
            leeslijst.getLeerlingen().clear();
        } else {
            Set<User> leerlingen = new HashSet<>();
            for (Long leerlingId : nieuweLeerlingIds) {
                User leerling = userRepository.findById(leerlingId)
                        .orElseThrow(() -> new GebruikerNotFoundException("Leerling niet gevonden: " + leerlingId));
                leerlingen.add(leerling);
            }
            leeslijst.setLeerlingen(leerlingen);
            leeslijst.setKlas(null);
        }

        return KlassLeeslijstMapper.toDTO(leeslijstRepository.save(leeslijst));
    }

    /**
     * Wisselt de zichtbaarheid van een leeslijst voor leerlingen.
     */
    @Transactional
    public KlassLeeslijstResponseDTO updateZichtbaar(Long id, boolean zichtbaar) {
        KlassLeeslijst leeslijst = leeslijstRepository.findById(id)
                .orElseThrow(() -> new KlassLeeslijstNotFoundException("Leeslijst niet gevonden"));
        leeslijst.setZichtbaar(zichtbaar);
        return KlassLeeslijstMapper.toDTO(leeslijstRepository.save(leeslijst));
    }

    /**
     * Verwijdert een leeslijst volledig uit de database.
     */
    @Transactional
    public void deleteLeeslijst(Long id) {
        KlassLeeslijst leeslijst = leeslijstRepository.findById(id)
                .orElseThrow(() -> new KlassLeeslijstNotFoundException("Leeslijst niet gevonden"));
        leeslijstRepository.delete(leeslijst);
    }

    /**
     * Geeft alle klassen-leeslijsten terug, van alle leerkrachten.
     */
    @Transactional(readOnly = true)
    public List<KlassLeeslijstResponseDTO> getAllKlassenLeeslijsten() {
        return leeslijstRepository.findAll().stream()
                .map(KlassLeeslijstMapper::toDTO)
                .toList();
    }

    /**
     * Geeft het archief van gearchiveerde leeslijsten terug.
     * - Bibbeheerder (isBeheerder=true): ziet alle gearchiveerde lijsten van alle leerkrachten.
     * - Leerkracht: ziet alleen zijn eigen gearchiveerde lijsten.
     */
    @Transactional(readOnly = true)
    public List<KlassLeeslijstResponseDTO> getArchief() {
        User huidigeGebruiker = SecurityUtils.getCurrentUser(userRepository);
        if (huidigeGebruiker.isBeheerder()) {
            return leeslijstRepository.findByGearchiveerdTrueOrderByAangemaaktOpDesc().stream()
                    .map(KlassLeeslijstMapper::toDTO)
                    .toList();
        }
        return leeslijstRepository.findByLeerkrachtIdAndGearchiveerdTrueOrderByAangemaaktOpDesc(huidigeGebruiker.getId()).stream()
                .map(KlassLeeslijstMapper::toDTO)
                .toList();
    }

    /**
     * Dearchiveert een gearchiveerde leeslijst zodat hij terug actief is.
     * De lijst komt terug als concept (zichtbaar=false) zodat de leerkracht
     * hem eerst kan aanpassen (klas/leerling toewijzen) vóór hij hem publiceert.
     */
    @Transactional
    public KlassLeeslijstResponseDTO dearchiveer(Long id) {
        KlassLeeslijst lijst = leeslijstRepository.findById(id)
                .orElseThrow(() -> new KlassLeeslijstNotFoundException("Leeslijst niet gevonden"));

        lijst.setGearchiveerd(false);
        lijst.setZichtbaar(false);

        return KlassLeeslijstMapper.toDTO(leeslijstRepository.save(lijst));
    }

    /**
     * Geeft alle leeslijsten terug die zichtbaar zijn voor de huidige ingelogde
     * leerling: zowel klas-leeslijsten van de eigen klas als persoonlijk
     * toegewezen lijsten. Onzichtbare lijsten worden gefilterd.
     *
     * @return gecombineerde lijst van zichtbare leeslijsten als DTO's
     */
    @Transactional(readOnly = true)
    public List<KlassLeeslijstResponseDTO> getLeeslijstenVoorHuidigeLeerling() {
        User huidigeLeerling = SecurityUtils.getCurrentUser(userRepository);

        List<KlassLeeslijst> resultaat = new ArrayList<>();

        if (huidigeLeerling.getKlas() != null && !huidigeLeerling.getKlas().isBlank()) {
            resultaat.addAll(leeslijstRepository.findByKlasAndZichtbaarTrueAndGearchiveerdFalseOrderByAangemaaktOpDesc(huidigeLeerling.getKlas()));
        }
        resultaat.addAll(leeslijstRepository.findByLeerlingIdAndZichtbaarTrueAndGearchiveerdFalse(huidigeLeerling.getId()));

        resultaat.sort((a, b) -> {
            LocalDateTime aDate = a.getAangemaaktOp();
            LocalDateTime bDate = b.getAangemaaktOp();
            if (aDate == null && bDate == null) return 0;
            if (aDate == null) return 1;
            if (bDate == null) return -1;
            return bDate.compareTo(aDate);
        });

        return resultaat.stream()
                .map(KlassLeeslijstMapper::toDTO)
                .toList();
    }
}
