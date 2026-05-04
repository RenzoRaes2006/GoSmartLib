package ap.be.backend.service;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import ap.be.backend.dto.AdminStatistiekDTO;
import ap.be.backend.dto.LeerkrachtStatistiekDTO;
import ap.be.backend.dto.LeerlingStatistiekDTO;
import ap.be.backend.repository.BoekRepository;
import ap.be.backend.repository.GebruikerRepository;
import ap.be.backend.repository.UitleningRepository;

@Service
public class StatistiekService {
    // Echte leningen: aanvragen die door admin zijn goedgekeurd of via scan zijn aangemaakt.
    // Inlevering flipt enkel isIngeleverd=true, dus deze lijst dekt zowel lopende als teruggebrachte leningen.
    private static final List<String> ACTIEVE_STATUSSEN = List.of("GOEDGEKEURD", "ACTIEF");

    private final UitleningRepository uitleningRepository;
    private final BoekRepository boekRepository;
    private final GebruikerRepository gebruikerRepository;

    public StatistiekService(UitleningRepository uitleningRepository,
                             BoekRepository boekRepository,
                             GebruikerRepository gebruikerRepository) {
        this.uitleningRepository = uitleningRepository;
        this.boekRepository = boekRepository;
        this.gebruikerRepository = gebruikerRepository;
    }

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

    public LeerkrachtStatistiekDTO getLeerkrachtStatistieken(String klasId) {
        String klas = (klasId == null || klasId.isBlank()) ? null : klasId;

        int aantalLeerlingen = klas == null
                ? (int) gebruikerRepository.count()
                : (int) gebruikerRepository.countByKlasIgnoreCase(klas);

        int actief = (int) uitleningRepository.countActieveLeningenVoorKlas(klas, ACTIEVE_STATUSSEN);
        int teLaat = (int) uitleningRepository.countTeLaatVoorKlas(klas, ACTIEVE_STATUSSEN, LocalDate.now());
        int afgelopenWeek = (int) uitleningRepository
                .countLeningenSindsVoorKlas(klas, ACTIEVE_STATUSSEN, LocalDate.now().minusDays(7));

        List<String> topBoeken = uitleningRepository
                .findTopBoekTitelsVoorKlas(klas, ACTIEVE_STATUSSEN, PageRequest.of(0, 3));

        return new LeerkrachtStatistiekDTO(aantalLeerlingen, actief, teLaat, afgelopenWeek, topBoeken);
    }

    public LeerlingStatistiekDTO getLeerlingStatistieken(String schoolId) {
        long sid;
        try {
            sid = Long.parseLong(schoolId);
        } catch (NumberFormatException e) {
            return new LeerlingStatistiekDTO(0, 10, 0, 0);
        }

        int gelezen = (int) uitleningRepository.countLeengeschiedenisVoorSchoolId(sid, "GEWEIGERD");
        int actief = (int) uitleningRepository.countActieveLeningenVoorSchoolId(sid, ACTIEVE_STATUSSEN);
        int teLaat = (int) uitleningRepository.countTeLaatVoorSchoolId(sid, ACTIEVE_STATUSSEN, LocalDate.now());

        // leesDoel wordt nog niet persistent opgeslagen; default 10 tot dat er een instelling per leerling is
        return new LeerlingStatistiekDTO(gelezen, 10, actief, teLaat);
    }
}
