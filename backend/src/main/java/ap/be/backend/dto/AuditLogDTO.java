package ap.be.backend.dto;

import java.time.LocalDateTime;

import ap.be.backend.entity.AuditActie;
import ap.be.backend.entity.AuditLog;

public record AuditLogDTO(
        Long id,
        LocalDateTime tijdstip,
        Long actorGebruikerId,
        String actorSmartschoolId,
        AuditActie actie,
        String entiteitType,
        Long entiteitId,
        String onderwerpLabel,
        String detailsJson) {

    public static AuditLogDTO from(AuditLog log, String onderwerpLabel) {
        return new AuditLogDTO(
                log.getId(),
                log.getTijdstip(),
                log.getActorGebruikerId(),
                log.getActorSmartschoolId(),
                log.getActie(),
                log.getEntiteitType(),
                log.getEntiteitId(),
                onderwerpLabel,
                log.getDetailsJson());
    }
}
