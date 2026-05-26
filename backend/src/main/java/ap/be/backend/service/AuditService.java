package ap.be.backend.service;

import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import ap.be.backend.config.SecurityUtils;
import ap.be.backend.entity.AuditActie;
import ap.be.backend.entity.AuditLog;
import ap.be.backend.entity.User;
import ap.be.backend.repository.AuditLogRepository;
import ap.be.backend.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;

/**
 * Centrale helper voor het schrijven van audit-rijen. Roep aan vanuit dezelfde
 * transactie als de actie zelf; faalt nooit luid — bij een serialisatiefout
 * loggen we en gaan we door zodat een audit-bug nooit een lifecycle-actie kan
 * tegenhouden.
 */
@Slf4j
@Service
public class AuditService {

    private final AuditLogRepository repository;
    private final UserRepository userRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public AuditService(AuditLogRepository repository, UserRepository userRepository) {
        this.repository = repository;
        this.userRepository = userRepository;
    }

    /**
     * Log een actie uitgevoerd door de huidige ingelogde admin (uit SecurityContext).
     */
    public void log(AuditActie actie, String entiteitType, Long entiteitId, Map<String, Object> details) {
        User actor = SecurityUtils.getCurrentUserOptional(userRepository).orElse(null);
        schrijf(actor, actie, entiteitType, entiteitId, details);
    }

    /**
     * Log een systeem-actie (sync, scheduled job) waar geen ingelogde actor is.
     */
    public void logSysteem(AuditActie actie, String entiteitType, Long entiteitId, Map<String, Object> details) {
        schrijf(null, actie, entiteitType, entiteitId, details);
    }

    private void schrijf(User actor, AuditActie actie, String entiteitType, Long entiteitId,
                         Map<String, Object> details) {
        try {
            AuditLog rij = new AuditLog();
            rij.setActie(actie);
            rij.setEntiteitType(entiteitType);
            rij.setEntiteitId(entiteitId);
            if (actor != null) {
                rij.setActorGebruikerId(actor.getId());
                rij.setActorSmartschoolId(actor.getSmartschoolId());
            }
            rij.setDetailsJson(serialiseer(details));
            repository.save(rij);
        } catch (Exception e) {
            log.error("AuditLog schrijven mislukt voor actie {} op {}#{}: {}",
                    actie, entiteitType, entiteitId, e.getMessage());
        }
    }

    private String serialiseer(Map<String, Object> details) {
        if (details == null || details.isEmpty()) return null;
        try {
            return objectMapper.writeValueAsString(details);
        } catch (JsonProcessingException e) {
            return null;
        }
    }

    /**
     * Compacte builder voor details — `details("van", oud, "naar", nieuw)`.
     */
    public static Map<String, Object> details(Object... keyValues) {
        if (keyValues.length % 2 != 0) {
            throw new IllegalArgumentException("details() vereist key/value-paren");
        }
        Map<String, Object> map = new LinkedHashMap<>();
        for (int i = 0; i < keyValues.length; i += 2) {
            map.put(String.valueOf(keyValues[i]), keyValues[i + 1]);
        }
        return map;
    }
}
