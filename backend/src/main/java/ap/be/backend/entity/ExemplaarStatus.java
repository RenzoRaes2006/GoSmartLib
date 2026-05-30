package ap.be.backend.entity;

/**
 * Toestand van één fysiek exemplaar van een boek.
 *
 * <ul>
 *   <li>{@code BESCHIKBAAR} – staat in de kast, kan uitgeleend worden.</li>
 *   <li>{@code UITGELEEND} – momenteel bij een leerling/leerkracht.</li>
 *   <li>{@code BESCHADIGD} – ingeleverd met schade; uit circulatie tot een
 *       beheerder het manueel terugzet (dat laatste valt buiten huidige scope).</li>
 * </ul>
 */
public enum ExemplaarStatus {
    BESCHIKBAAR,
    UITGELEEND,
    BESCHADIGD
}
