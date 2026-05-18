package ap.be.backend.service;

import ap.be.backend.dto.CampusDTO;
import ap.be.backend.dto.SchoolOnboardDTO;
import ap.be.backend.entity.AdminGebruiker;
import ap.be.backend.entity.Campus;
import ap.be.backend.entity.Rol;
import ap.be.backend.entity.School;
import ap.be.backend.entity.User;
import ap.be.backend.repository.AdminGebruikerRepository;
import ap.be.backend.repository.SchoolRepository;
import ap.be.backend.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Service
public class SchoolOnboardingService {

    private final SchoolRepository schoolRepository;
    private final AdminGebruikerRepository adminGebruikerRepository;
    private final UserRepository userRepository;
    private final OneRosterClient oneRosterClient;
    private final PasswordEncoder passwordEncoder;

    public SchoolOnboardingService(SchoolRepository schoolRepository,
                                   AdminGebruikerRepository adminGebruikerRepository,
                                   UserRepository userRepository,
                                   OneRosterClient oneRosterClient,
                                   PasswordEncoder passwordEncoder) {
        this.schoolRepository = schoolRepository;
        this.adminGebruikerRepository = adminGebruikerRepository;
        this.userRepository = userRepository;
        this.oneRosterClient = oneRosterClient;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional(rollbackFor = Exception.class)
    public School onboardNieuweSchool(SchoolOnboardDTO dto) {
        valideer(dto);

        School nieuweSchool = new School();
        nieuweSchool.setNaam(dto.getSchoolNaam());
        nieuweSchool.setAdres(dto.getAdres());
        nieuweSchool.setSmartschoolDomein(dto.getSmartschoolDomein());
        nieuweSchool.setOauthClientId(dto.getOauthClientId());
        nieuweSchool.setOauthClientSecret(dto.getOauthClientSecret());
        nieuweSchool.setApiCode("go-" + System.currentTimeMillis());

        if (dto.getCampussen() != null) {
            for (CampusDTO c : dto.getCampussen()) {
                Campus campus = new Campus();
                campus.setNaam(c.getNaam());
                campus.setSchool(nieuweSchool);
                nieuweSchool.getCampussen().add(campus);
            }
        }

        School opgeslagen = schoolRepository.save(nieuweSchool);

        importeerGebruikers(dto);
        if (dto.getBeheerder() != null
                && dto.getBeheerder().getGebruikersnaam() != null
                && !dto.getBeheerder().getGebruikersnaam().isBlank()) {
            maakBeheerderAan(dto);
        }

        return opgeslagen;
    }

    private void valideer(SchoolOnboardDTO dto) {
        if (dto.getSchoolNaam() == null || dto.getSchoolNaam().isBlank()) {
            throw new IllegalArgumentException("Schoolnaam is verplicht");
        }
        if (dto.getBeheerder() != null
                && dto.getBeheerder().getGebruikersnaam() != null
                && !dto.getBeheerder().getGebruikersnaam().isBlank()
                && adminGebruikerRepository.findByGebruikersnaam(dto.getBeheerder().getGebruikersnaam()).isPresent()) {
            throw new IllegalArgumentException("Beheerder gebruikersnaam bestaat al");
        }
    }

    @Transactional
    public AdminGebruiker duidBeheerderAan(Long schoolId, SchoolOnboardDTO.BeheerderDTO b) {
        if (!schoolRepository.existsById(schoolId)) {
            throw new IllegalArgumentException("School bestaat niet");
        }
        if (b == null || b.getGebruikersnaam() == null || b.getGebruikersnaam().isBlank()) {
            throw new IllegalArgumentException("Beheerder gebruikersnaam is verplicht");
        }
        if (adminGebruikerRepository.findByGebruikersnaam(b.getGebruikersnaam()).isPresent()) {
            throw new IllegalArgumentException("Beheerder gebruikersnaam bestaat al");
        }
        String wachtwoord = (b.getWachtwoord() == null || b.getWachtwoord().isBlank())
                ? "wijzigDirect!"
                : b.getWachtwoord();
        return adminGebruikerRepository.save(new AdminGebruiker(
                b.getGebruikersnaam(), passwordEncoder.encode(wachtwoord), b.getNaam()));
    }

    @SuppressWarnings("unchecked")
    private void importeerGebruikers(SchoolOnboardDTO dto) {
        if (dto.getSmartschoolDomein() == null || dto.getSmartschoolDomein().isBlank()) {
            return;
        }
        Map<String, Object> data = oneRosterClient.haalLiveSchoolDataOp(
                dto.getSmartschoolDomein(), dto.getOauthClientId(), dto.getOauthClientSecret());

        List<Map<String, Object>> leerlingen = (List<Map<String, Object>>) data.getOrDefault("leerlingen", List.of());
        for (Map<String, Object> ll : leerlingen) {
            String smartschoolId = String.valueOf(ll.get("id"));
            if (userRepository.findBySmartschoolId(smartschoolId).isPresent()) {
                continue;
            }
            String klas = (String) ll.get("klas");
            userRepository.save(new User(smartschoolId, klas, Rol.LEERLING));
        }

        List<Map<String, Object>> leerkrachten = (List<Map<String, Object>>) data.getOrDefault("leerkrachten", List.of());
        for (Map<String, Object> lk : leerkrachten) {
            String smartschoolId = String.valueOf(lk.get("id"));
            if (userRepository.findBySmartschoolId(smartschoolId).isPresent()) {
                continue;
            }
            String klas = (String) lk.getOrDefault("klas", "");
            userRepository.save(new User(smartschoolId, klas, Rol.LEERKRACHT));
        }
    }

    private void maakBeheerderAan(SchoolOnboardDTO dto) {
        SchoolOnboardDTO.BeheerderDTO b = dto.getBeheerder();
        String wachtwoord = (b.getWachtwoord() == null || b.getWachtwoord().isBlank())
                ? "wijzigDirect!"
                : b.getWachtwoord();
        AdminGebruiker beheerder = new AdminGebruiker(
                b.getGebruikersnaam(),
                passwordEncoder.encode(wachtwoord),
                b.getNaam()
        );
        adminGebruikerRepository.save(beheerder);
    }
}
