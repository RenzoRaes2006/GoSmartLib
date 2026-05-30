package ap.be.backend.service;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import ap.be.backend.dto.BoekSchadeDTO;
import ap.be.backend.entity.BoekSchade;
import ap.be.backend.entity.Exemplaar;
import ap.be.backend.entity.Uitlening;
import ap.be.backend.repository.BoekSchadeRepository;

/**
 * Beheert schaderapporten voor ingeleverde boeken.
 *
 * Privacy: de entity koppelt enkel naar Uitlening (en daarmee user via smartschoolId).
 * Namen worden nergens gepersisteerd; bij listing worden ze live opgehaald via
 * {@link OneRosterService#getNaamBySourcedId(String)} (in-memory cache met 30 min TTL).
 */
@Service
public class BoekSchadeService {

    private final BoekSchadeRepository boekSchadeRepository;
    private final OneRosterService oneRosterService;
    private final ExemplaarService exemplaarService;

    public BoekSchadeService(BoekSchadeRepository boekSchadeRepository,
                             OneRosterService oneRosterService,
                             ExemplaarService exemplaarService) {
        this.boekSchadeRepository = boekSchadeRepository;
        this.oneRosterService = oneRosterService;
        this.exemplaarService = exemplaarService;
    }

    @Transactional
    public BoekSchade registreer(Uitlening uitlening, String opmerking) {
        BoekSchade schade = new BoekSchade();
        schade.setUitlening(uitlening);
        schade.setOpmerking(opmerking);
        schade.setGemeldOp(LocalDateTime.now());
        return boekSchadeRepository.save(schade);
    }

    /**
     * Verwijdert een schade-rapport. De beheerder doet dit wanneer het exemplaar
     * hersteld is / er geen schade meer is, dus het bijhorende exemplaar gaat terug
     * naar BESCHIKBAAR — anders blijft de "Beschadigd"-badge in catalogus/detail staan
     * en blijft het exemplaar onterecht uit circulatie.
     */
    @Transactional
    public boolean verwijder(Long id) {
        BoekSchade schade = boekSchadeRepository.findById(id).orElse(null);
        if (schade == null) return false;

        Exemplaar exemplaar = schade.getUitlening() == null ? null : schade.getUitlening().getExemplaar();
        boekSchadeRepository.delete(schade);
        exemplaarService.herstelBeschadigd(exemplaar);
        return true;
    }

    @Transactional(readOnly = true)
    public List<BoekSchadeDTO> getAlle() {
        return boekSchadeRepository.findAll().stream()
                .sorted(Comparator.comparing(BoekSchade::getGemeldOp).reversed())
                .map(this::toDTO)
                .toList();
    }

    private BoekSchadeDTO toDTO(BoekSchade s) {
        Uitlening u = s.getUitlening();
        String smartschoolId = u.getUser().getSmartschoolId();
        String naam = oneRosterService.getNaamBySourcedId(smartschoolId);
        return new BoekSchadeDTO(
                s.getId(),
                u.getExemplaar().getBoek().getTitel(),
                u.getExemplaar().getBoek().getIsbn(),
                smartschoolId,
                naam,
                u.getUser().getKlas(),
                s.getOpmerking(),
                s.getGemeldOp());
    }
}
