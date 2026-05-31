package ap.be.backend.service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import ap.be.backend.dto.AdminStatistiekDTO;
import ap.be.backend.dto.LeerkrachtStatistiekDTO;
import ap.be.backend.dto.LeerlingStatistiekDTO;
import ap.be.backend.entity.Rol;
import ap.be.backend.entity.User;
import ap.be.backend.repository.BoekRepository;
import ap.be.backend.repository.UitleningRepository;
import ap.be.backend.repository.UserRepository;

/**
 * Berekent statistieken voor het beheerder-dashboard, het leerkracht-dashboard
 * en de leerling-profielpagina.
 *
 * Alleen leningen met de status GOEDGEKEURD of ACTIEF tellen mee als echte
 * leningen. Aanvragen en geweigerde leningen worden buiten beschouwing gelaten.
 */
@Service
public class StatistiekService {

    // Leningen met deze statussen zijn goedgekeurd door de beheerder of aangemaakt via de scan-modus.
    private static final List<String> ACTIEVE_STATUSSEN = List.of("GOEDGEKEURD", "ACTIEF");

    private final UitleningRepository uitleningRepository;
    private final BoekRepository boekRepository;
    private final UserRepository userRepository;

    public StatistiekService(UitleningRepository uitleningRepository,
                             BoekRepository boekRepository,
                             UserRepository userRepository) {
        this.uitleningRepository = uitleningRepository;
        this.boekRepository = boekRepository;
        this.userRepository = userRepository;
    }

    /**
     * Berekent de statistieken voor het beheerder-dashboard.
     *
     * Bevat het percentage uitgeleende boeken, het aantal te laat ingeleverde
     * boeken, het populairste genre en het aantal leningen deze maand.
     *
     * @return een AdminStatistiekDTO met de berekende waarden
     */
    public AdminStatistiekDTO getAdminStatistieken() {
        long actieveLeningen = uitleningRepository.countByIsIngeleverdFalseAndStatusIn(ACTIEVE_STATUSSEN);
        long totaalExemplaren = boekRepository.sumTotaalAantalExemplaren();
        long totaleCapaciteit = totaalExemplaren + actieveLeningen;

        Double percentageUitgeleend = totaleCapaciteit == 0
                ? 0.0
                : Math.round(((double) actieveLeningen / totaleCapaciteit) * 1000.0) / 10.0;

        int teLaatIngeleverd = (int) uitleningRepository
                .countByIsIngeleverdFalseAndStatusInAndInleverDatumBefore(ACTIEVE_STATUSSEN, LocalDate.now());

        List<String> populairste = uitleningRepository.findPopulairsteGenres(ACTIEVE_STATUSSEN, PageRequest.of(0, 1));
        String populairsteGenre = populairste.isEmpty() ? null : populairste.get(0);

        int uitleningenDezeMaand = (int) uitleningRepository.countLeningenSinds(
                ACTIEVE_STATUSSEN, LocalDate.now().withDayOfMonth(1));

        return new AdminStatistiekDTO(percentageUitgeleend, teLaatIngeleverd, populairsteGenre, uitleningenDezeMaand);
    }

    /**
     * Berekent de statistieken voor het leerkracht-dashboard.
     *
     * Als een klasnaam wordt meegegeven, worden de cijfers beperkt tot die klas.
     * Zonder klasnaam worden alle leerlingen meegeteld.
     *
     * @param klasId de klasnaam om op te filteren, of null voor alle klassen
     * @return een LeerkrachtStatistiekDTO met de berekende waarden
     */
    public LeerkrachtStatistiekDTO getLeerkrachtStatistieken(String klasId) {
        String klas = (klasId == null || klasId.isBlank()) ? null : klasId;

        // Tel alleen actieve leerlingen (vertrokken + andere rollen tellen niet mee).
        int aantalLeerlingen = klas == null
                ? (int) userRepository.countByRolAndActiefTrue(Rol.LEERLING)
                : (int) userRepository.countByRolAndKlasIgnoreCaseAndActiefTrue(Rol.LEERLING, klas);

        int actief = (int) uitleningRepository.countActieveLeningenVoorKlas(klas, ACTIEVE_STATUSSEN);
        int teLaat = (int) uitleningRepository.countTeLaatVoorKlas(klas, ACTIEVE_STATUSSEN, LocalDate.now());
        int afgelopenWeek = (int) uitleningRepository
                .countLeningenSindsVoorKlas(klas, ACTIEVE_STATUSSEN, LocalDate.now().minusDays(7));

        List<String> topBoeken = uitleningRepository
                .findTopBoekTitelsVoorKlas(klas, ACTIEVE_STATUSSEN, PageRequest.of(0, 3));

        return new LeerkrachtStatistiekDTO(aantalLeerlingen, actief, teLaat, afgelopenWeek, topBoeken);
    }

    /**
     * Berekent de statistieken voor de profielpagina van een leerling.
     *
     * Geeft standaardwaarden terug als de leerling niet gevonden wordt.
     * Het leesdoel is nog niet persistent opgeslagen en staat voorlopig
     * vast op 10 totdat er een instelling per leerling beschikbaar is.
     *
     * @param smartschoolId de unieke Smartschool-ID van de leerling
     * @return een LeerlingStatistiekDTO met de berekende waarden
     */
    public LeerlingStatistiekDTO getLeerlingStatistieken(String smartschoolId) {
        Optional<User> userOpt = userRepository.findBySmartschoolId(smartschoolId);
        if (userOpt.isEmpty()) {
            return new LeerlingStatistiekDTO(0, 10, 0, 0);
        }
        Long userId = userOpt.get().getId();

        int gelezen = (int) uitleningRepository.countGelezenVoorUser(userId, ACTIEVE_STATUSSEN);
        int actief = (int) uitleningRepository.countActieveLeningenVoorUser(userId, ACTIEVE_STATUSSEN);
        int teLaat = (int) uitleningRepository.countTeLaatVoorUser(userId, ACTIEVE_STATUSSEN, LocalDate.now());

        return new LeerlingStatistiekDTO(gelezen, 10, actief, teLaat);
    }
}
