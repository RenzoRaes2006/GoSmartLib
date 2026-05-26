package ap.be.backend.service;

import ap.be.backend.dto.CampusDTO;
import ap.be.backend.dto.SchoolOnboardDTO;
import ap.be.backend.entity.AdminGebruiker;
import ap.be.backend.entity.AuditActie;
import ap.be.backend.entity.Campus;
import ap.be.backend.entity.School;
import ap.be.backend.repository.AdminGebruikerRepository;
import ap.be.backend.repository.SchoolRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SchoolOnboardingService {

    private final SchoolRepository schoolRepository;
    private final AdminGebruikerRepository adminGebruikerRepository;
    private final OneRosterImportService oneRosterImportService;
    private final PasswordEncoder passwordEncoder;
    private final AuditService auditService;

    public SchoolOnboardingService(SchoolRepository schoolRepository,
                                   AdminGebruikerRepository adminGebruikerRepository,
                                   OneRosterImportService oneRosterImportService,
                                   PasswordEncoder passwordEncoder,
                                   AuditService auditService) {
        this.schoolRepository = schoolRepository;
        this.adminGebruikerRepository = adminGebruikerRepository;
        this.oneRosterImportService = oneRosterImportService;
        this.passwordEncoder = passwordEncoder;
        this.auditService = auditService;
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

        if (dto.getSmartschoolDomein() != null && !dto.getSmartschoolDomein().isBlank()) {
            oneRosterImportService.importSchool(opgeslagen.getId());
        }
        if (dto.getBeheerder() != null
                && dto.getBeheerder().getGebruikersnaam() != null
                && !dto.getBeheerder().getGebruikersnaam().isBlank()) {
            maakBeheerderAan(dto);
        }

        auditService.log(AuditActie.SCHOOL_ONBOARDED, "School", opgeslagen.getId(),
                AuditService.details("naam", opgeslagen.getNaam(),
                        "smartschoolDomein", opgeslagen.getSmartschoolDomein(),
                        "aantalCampussen", opgeslagen.getCampussen().size()));

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
