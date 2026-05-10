package ap.be.backend.service;

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

import java.util.List;

/**
 * Beheert de klassen-leeslijsten die leerkrachten aanmaken voor hun leerlingen.
 *
 * Een klassen-leeslijst is een verzameling boeken die een leerkracht samenstelt.
 * Leerlingen kunnen deze lijsten raadplegen om te zien welke boeken aanbevolen
 * worden voor hun klas.
 */
@Service
@RequiredArgsConstructor
public class KlassLeeslijstService {

    private final KlassLeeslijstRepository leeslijstRepository;
    private final UserRepository userRepository;
    private final BoekRepository boekRepository;

    /**
     * Maakt een nieuwe klassen-leeslijst aan voor een leerkracht.
     *
     * @param naam        de naam van de nieuwe leeslijst
     * @param leerkrachtId het database-ID van de leerkracht die de lijst aanmaakt
     * @return de aangemaakte leeslijst als DTO
     * @throws GebruikerNotFoundException als de leerkracht niet bestaat
     */
    @Transactional
    public KlassLeeslijstResponseDTO createLeeslijst(String naam, Long leerkrachtId) {
        User leerkracht = userRepository.findById(leerkrachtId)
                .orElseThrow(() -> new GebruikerNotFoundException("Leerkracht niet gevonden"));

        KlassLeeslijst leeslijst = new KlassLeeslijst();
        leeslijst.setNaam(naam);
        leeslijst.setLeerkracht(leerkracht);

        return KlassLeeslijstMapper.toDTO(leeslijstRepository.save(leeslijst));
    }

    /**
     * Geeft alle leeslijsten terug van een bepaalde leerkracht.
     *
     * @param leerkrachtId het database-ID van de leerkracht
     * @return lijst van leeslijsten als DTO's
     */
    @Transactional(readOnly = true)
    public List<KlassLeeslijstResponseDTO> getLeeslijstenByLeerkracht(Long leerkrachtId) {
        return leeslijstRepository.findByLeerkrachtId(leerkrachtId).stream()
                .map(KlassLeeslijstMapper::toDTO)
                .toList();
    }

    /**
     * Voegt een boek toe aan een bestaande leeslijst.
     *
     * @param leeslijstId het ID van de leeslijst
     * @param boekId      het ID van het toe te voegen boek
     * @return de bijgewerkte leeslijst als DTO
     * @throws KlassLeeslijstNotFoundException als de leeslijst niet bestaat
     * @throws BoekNotFoundException           als het boek niet bestaat
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
     *
     * @param id het ID van de leeslijst
     * @return de leeslijst als DTO
     * @throws KlassLeeslijstNotFoundException als de leeslijst niet bestaat
     */
    @Transactional(readOnly = true)
    public KlassLeeslijstResponseDTO getLeeslijstById(Long id) {
        KlassLeeslijst leeslijst = leeslijstRepository.findById(id)
                .orElseThrow(() -> new KlassLeeslijstNotFoundException("Leeslijst niet gevonden"));
        return KlassLeeslijstMapper.toDTO(leeslijst);
    }

    /**
     * Verwijdert een boek uit een leeslijst.
     *
     * @param leeslijstId het ID van de leeslijst
     * @param boekId      het ID van het te verwijderen boek
     * @return de bijgewerkte leeslijst als DTO
     * @throws KlassLeeslijstNotFoundException als de leeslijst niet bestaat
     * @throws BoekNotFoundException           als het boek niet bestaat
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
     *
     * @param id         het ID van de leeslijst
     * @param nieuweNaam de nieuwe naam
     * @return de bijgewerkte leeslijst als DTO
     * @throws KlassLeeslijstNotFoundException als de leeslijst niet bestaat
     */
    @Transactional
    public KlassLeeslijstResponseDTO updateLeeslijstNaam(Long id, String nieuweNaam) {
        KlassLeeslijst leeslijst = leeslijstRepository.findById(id)
                .orElseThrow(() -> new KlassLeeslijstNotFoundException("Leeslijst niet gevonden"));
        leeslijst.setNaam(nieuweNaam);
        return KlassLeeslijstMapper.toDTO(leeslijstRepository.save(leeslijst));
    }

    /**
     * Verwijdert een leeslijst volledig uit de database.
     *
     * @param id het ID van de te verwijderen leeslijst
     * @throws KlassLeeslijstNotFoundException als de leeslijst niet bestaat
     */
    @Transactional
    public void deleteLeeslijst(Long id) {
        KlassLeeslijst leeslijst = leeslijstRepository.findById(id)
                .orElseThrow(() -> new KlassLeeslijstNotFoundException("Leeslijst niet gevonden"));
        leeslijstRepository.delete(leeslijst);
    }

    /**
     * Geeft alle klassen-leeslijsten terug, van alle leerkrachten.
     *
     * @return lijst van alle leeslijsten als DTO's
     */
    @Transactional(readOnly = true)
    public List<KlassLeeslijstResponseDTO> getAllKlassenLeeslijsten() {
        return leeslijstRepository.findAll().stream()
                .map(KlassLeeslijstMapper::toDTO)
                .toList();
    }
}
