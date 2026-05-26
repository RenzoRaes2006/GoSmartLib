package ap.be.backend.entity;

/**
 * Lifecycle-acties die in de audit-log terechtkomen. Suffix _SYNC duidt op
 * automatische actie vanuit OneRoster-sync (geen ingelogde actor).
 */
public enum AuditActie {
    GEBRUIKER_TOEGEVOEGD,
    GEBRUIKER_TOEGEVOEGD_SYNC,
    GEBRUIKER_VERTROKKEN,
    GEBRUIKER_VERTROKKEN_SYNC,
    GEBRUIKER_GEREACTIVEERD,
    KLAS_GEWIJZIGD,
    KLAS_GEWIJZIGD_SYNC,
    ROLLOVER_UITGEVOERD,
    SCHOOL_ONBOARDED
}
