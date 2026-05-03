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

@Service
@RequiredArgsConstructor
public class KlassLeeslijstService {

    private final KlassLeeslijstRepository leeslijstRepository;
    private final UserRepository userRepository;
    private final BoekRepository boekRepository;

    @Transactional
    public KlassLeeslijstResponseDTO createLeeslijst(String naam, Long leerkrachtId) {
        User leerkracht = userRepository.findById(leerkrachtId)
                .orElseThrow(() -> new GebruikerNotFoundException("Leerkracht niet gevonden"));

        KlassLeeslijst leeslijst = new KlassLeeslijst();
        leeslijst.setNaam(naam);
        leeslijst.setLeerkracht(leerkracht);

        return KlassLeeslijstMapper.toDTO(leeslijstRepository.save(leeslijst));
    }

    @Transactional(readOnly = true)
    public List<KlassLeeslijstResponseDTO> getLeeslijstenByLeerkracht(Long leerkrachtId) {
        // Haal de entiteiten op uit de repository[cite: 2]
        return leeslijstRepository.findByLeerkrachtId(leerkrachtId).stream()
                .map(KlassLeeslijstMapper::toDTO) // Zet elke entiteit om naar een DTO
                .toList();
    }

    @Transactional
    public KlassLeeslijstResponseDTO addBoekToLeeslijst(Long leeslijstId, Long boekId) {
        KlassLeeslijst leeslijst = leeslijstRepository.findById(leeslijstId)
                .orElseThrow(() -> new KlassLeeslijstNotFoundException("Leeslijst niet gevonden"));

        Boek boek = boekRepository.findById(boekId)
                .orElseThrow(() -> new BoekNotFoundException("Boek niet gevonden"));

        leeslijst.getBoeken().add(boek);
        return KlassLeeslijstMapper.toDTO(leeslijstRepository.save(leeslijst));
    }

    @Transactional(readOnly = true)
    public KlassLeeslijstResponseDTO getLeeslijstById(Long id) {
        KlassLeeslijst leeslijst = leeslijstRepository.findById(id)
                .orElseThrow(() -> new KlassLeeslijstNotFoundException("Leeslijst niet gevonden"));
        return KlassLeeslijstMapper.toDTO(leeslijst); // Gebruik je mapper[cite: 9]
    }

    @Transactional
    public KlassLeeslijstResponseDTO removeBoekFromLeeslijst(Long leeslijstId, Long boekId) {
        KlassLeeslijst leeslijst = leeslijstRepository.findById(leeslijstId)
                .orElseThrow(() -> new KlassLeeslijstNotFoundException("Leeslijst niet gevonden"));

        Boek boek = boekRepository.findById(boekId)
                .orElseThrow(() -> new BoekNotFoundException("Boek niet gevonden"));

        // Verwijdert de koppeling tussen de leeslijst en het boek
        leeslijst.getBoeken().remove(boek);

        return KlassLeeslijstMapper.toDTO(leeslijstRepository.save(leeslijst));
    }

    @Transactional
    public KlassLeeslijstResponseDTO updateLeeslijstNaam(Long id, String nieuweNaam) {
        KlassLeeslijst leeslijst = leeslijstRepository.findById(id)
                .orElseThrow(() -> new KlassLeeslijstNotFoundException("Leeslijst niet gevonden"));
        leeslijst.setNaam(nieuweNaam);
        return KlassLeeslijstMapper.toDTO(leeslijstRepository.save(leeslijst));
    }

    @Transactional
    public void deleteLeeslijst(Long id) {
        KlassLeeslijst leeslijst = leeslijstRepository.findById(id)
                .orElseThrow(() -> new KlassLeeslijstNotFoundException("Leeslijst niet gevonden"));
        leeslijstRepository.delete(leeslijst);
    }
}