package ap.be.backend.mappers;

import ap.be.backend.dto.BoekDTO;
import ap.be.backend.dto.KlassLeeslijstResponseDTO;
import ap.be.backend.entity.KlassLeeslijst;
import ap.be.backend.entity.User;
import ap.be.backend.service.OneRosterService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Zet KlassLeeslijst-entiteiten om naar KlassLeeslijstResponseDTO's.
 *
 * Biedt twee varianten:
 *  - statische toDTO: snel en zonder externe lookup, leerlingNaam wordt
 *    teruggegeven als smartschoolId (fallback).
 *  - instance toDTOMetNaam: vraagt de echte naam op via OneRoster. Wordt
 *    automatisch ingespoten door Spring en is null-safe als OneRoster faalt.
 */
@Component
public class KlassLeeslijstMapper {

    private static OneRosterService oneRosterServiceStatic;

    @Autowired
    public KlassLeeslijstMapper(OneRosterService oneRosterService) {
        KlassLeeslijstMapper.oneRosterServiceStatic = oneRosterService;
    }

    /**
     * Zet een KlassLeeslijst-entiteit om naar een DTO inclusief de gekoppelde boeken.
     * Voor de leerlingNaam wordt eerst OneRoster bevraagd (gecached); bij falen
     * valt de service terug op de smartschoolId zodat de UI altijd iets toont.
     */
    public static KlassLeeslijstResponseDTO toDTO(KlassLeeslijst entity) {
        List<BoekDTO> boekDTOs = entity.getBoeken().stream()
                .map(BoekMapper::toDTO)
                .toList();

        List<Long> leerlingIds = entity.getLeerlingen().stream()
                .map(User::getId)
                .toList();

        List<String> leerlingNamen = entity.getLeerlingen().stream()
                .map(leerling -> {
                    String smartschoolId = leerling.getSmartschoolId();
                    String naamUitOneRoster = null;
                    if (oneRosterServiceStatic != null && smartschoolId != null) {
                        naamUitOneRoster = oneRosterServiceStatic.getNaamBySourcedId(smartschoolId);
                    }
                    return naamUitOneRoster != null ? naamUitOneRoster : smartschoolId;
                })
                .toList();

        User leerkracht = entity.getLeerkracht();
        Long leerkrachtId = leerkracht != null ? leerkracht.getId() : null;
        String leerkrachtNaam = null;
        if (leerkracht != null) {
            String leerkrachtSmartschoolId = leerkracht.getSmartschoolId();
            String naamUitOneRoster = null;
            if (oneRosterServiceStatic != null && leerkrachtSmartschoolId != null) {
                naamUitOneRoster = oneRosterServiceStatic.getNaamBySourcedId(leerkrachtSmartschoolId);
            }
            leerkrachtNaam = naamUitOneRoster != null ? naamUitOneRoster : leerkrachtSmartschoolId;
        }

        return new KlassLeeslijstResponseDTO(
                entity.getId(),
                entity.getNaam(),
                entity.getKlas(),
                leerkrachtId,
                leerkrachtNaam,
                leerlingIds,
                leerlingNamen,
                entity.getBeschrijving(),
                entity.isZichtbaar(),
                entity.isGearchiveerd(),
                boekDTOs);
    }
}
