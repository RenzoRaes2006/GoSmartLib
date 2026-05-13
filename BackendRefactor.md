# Backend Refactoring — GitLab Issues

Geordend op technische afhankelijkheden. Issues die onderaan staan, hangen af van issues daarboven.

---

## Issue 1 — `UitleningStatus`-enum aanmaken
**Als ontwikkelaar** wil ik een `UitleningStatus`-enum aanmaken ter vervanging van hardcoded statusstrings (`"ACTIEF"`, `"GOEDGEKEURD"`, `"GEWEIGERD"`, `"VERLENGING_AANGEVRAAGD"`) zodat statuswaarden type-safe en consistent zijn doorheen de hele backend.

- **Story points:** 2
- **Betrokken bestanden:** `entity/Uitlening.java`, `service/GebruikerService.java`
- **Status:** Open

---

## Issue 2 — Constructor-bug in `Uitlening` fixen
**Als ontwikkelaar** wil ik de constructor van `Uitlening` fixen zodat doorgegeven datum-parameters daadwerkelijk worden opgeslagen zodat uitleendatums correct in de database terechtkomen.

> **Bug:** De constructor zet `uitleenDatum` en `inleverDatum` altijd op `null`, ongeacht de doorgegeven waarden.

- **Story points:** 1
- **Betrokken bestanden:** `entity/Uitlening.java`
- **Status:** Open

---

## Issue 3 — `User`-entity uitbreiden met profielgegevens
**Als ontwikkelaar** wil ik de `User`-entity uitbreiden met `naam`, `rol`, `klas` en `email` zodat alle profielgegevens van de ingelogde gebruiker op één plek staan en correct worden opgeslagen na de Smartschool-login.

> **Context:** Momenteel slaat `User` enkel `smartschoolId` op. Er is een TODO-comment die aangeeft dat `rol` en `klas` hier nog moeten komen. Dit is een vereiste vóór de `Gebruiker`-migratie.

- **Story points:** 2
- **Betrokken bestanden:** `entity/User.java`, `config/OAuth2LoginSuccessHandler.java`
- **Status:** Open

---

## Issue 4 — `Gebruiker`-entity vervangen door `User`
**Als ontwikkelaar** wil ik de verouderde `Gebruiker`-entity volledig vervangen door `User` — inclusief alle relaties naar `Uitlening`, `Beoordeling` en `LeeslijstMap` — zodat er één consistente gebruikersentiteit is doorheen de hele backend en er geen verwarring meer bestaat tussen de twee entiteiten.

> **Scope:**
> - `Gebruiker`-verwijzingen in `Beoordeling`, `Uitlening`, `LeeslijstMap` omzetten naar `User`
> - `GebruikerRepository`, `GebruikerService`, `GebruikerController` updaten
> - Seeds aanpassen
> - Veldnamen in `Gebruiker` zijn PascalCase (`SchoolId`, `VoorNaam`, `Achternaam`) — bij migratie naar `User` correct camelCase maken
> - `GebruikerDTO` aanpassen of verwijderen

- **Story points:** 8
- **Betrokken bestanden:** `entity/Gebruiker.java`, `entity/Beoordeling.java`, `entity/Uitlening.java`, `entity/LeeslijstMap.java`, `repository/GebruikerRepository.java`, `service/GebruikerService.java`, `controller/GebruikerController.java`, `dto/GebruikerDTO.java`, seeds
- **Status:** Open

---

## Issue 5 — Hulpmethode voor ingelogde gebruiker uit SecurityContext
**Als ontwikkelaar** wil ik een herbruikbare hulpmethode hebben die de `smartschoolId` van de ingelogde gebruiker ophaalt uit de Spring Security-context zodat alle controllers dit op een consistente manier kunnen gebruiken in plaats van elke keer `SecurityContextHolder` direct aan te roepen.

> **Aanpak:** Een `AuthHelper`-klasse of methode in een bestaande service die `SecurityContextHolder.getContext().getAuthentication().getName()` inkapselt.

- **Story points:** 2
- **Betrokken bestanden:** Nieuwe klasse `config/AuthHelper.java` of in bestaande service
- **Status:** Open

---

## Issue 6 — Beheerder-login implementeren
**Als beheerder** wil ik kunnen inloggen met een eigen gebruikersnaam en wachtwoord zodat ik toegang heb tot het beheerdersportaal zonder een Smartschool-account nodig te hebben.

- **Story points:** 3
- **Status:** ✅ GEDAAN

---

## Issue 7 — Controller-methoden beveiligen met `@PreAuthorize`
**Als ontwikkelaar** wil ik dat controller-methoden beveiligd zijn met `@PreAuthorize` op basis van gebruikersrollen zodat onbevoegde gebruikers geen toegang krijgen tot gevoelige endpoints vanuit de backend zelf.

- **Story points:** 3
- **Status:** ✅ GEDAAN

---

## Issue 8 — Hardcoded `gebruikerId = 1L` verwijderen
**Als ontwikkelaar** wil ik de hardcoded `gebruikerId = 1L` in `BeoordelingService` en `BeoordelingController` vervangen door de werkelijke ingelogde gebruiker zodat beoordelingen correct worden toegewezen en niet altijd aan gebruiker 1.

> **Bug:** `BeoordelingService` regel 42 en 62 bevatten `Long tijdelijkGebruikerId = 1L` met TODO-comment. Idem in `BeoordelingController` regel 46. Dit vereist Issue 5.

- **Story points:** 3
- **Betrokken bestanden:** `service/BeoordelingService.java`, `controller/BeoordelingController.java`
- **Afhankelijk van:** Issue 4, Issue 5
- **Status:** Open

---

## Issue 9 — Data-eigenaarschap afdwingen op gebruikersendpoints
**Als leerling** wil ik dat enkel ik mijn eigen leeslijsten, leningen en leengeschiedenis kan opvragen of wijzigen zodat andere leerlingen mijn persoonlijke data niet kunnen inzien of manipuleren.

> **Scope:** Controllers accepteren nu een `gebruikerId` als path-parameter zonder te valideren dat dit de ingelogde gebruiker is. De backend moet verifiëren dat `{gebruikerId}` overeenkomt met de `smartschoolId` uit het JWT-token.
>
> Betrokken endpoints:
> - `GET /api/gebruiker/{id}/mappen`
> - `POST /api/gebruiker/{gebruikerId}/mappen/.../boeken/{boekId}`
> - `DELETE /api/gebruiker/{gebruikerId}/mappen/.../boeken/{boekId}`
> - `GET /api/gebruiker/{gebruikerId}/lenen/{boekId}/status`
> - `POST /api/gebruiker/{gebruikerId}/lenen/{boekId}`
> - `POST /api/gebruiker/{gebruikerId}/lenen/bulk`
> - `GET /api/gebruiker/{gebruikerId}/leningen`
> - `GET /api/gebruiker/{gebruikerId}/geschiedenis`

- **Story points:** 5
- **Betrokken bestanden:** `controller/GebruikerController.java`, `service/GebruikerService.java`
- **Afhankelijk van:** Issue 4, Issue 5
- **Status:** Open

---

## Issue 10 — Eigenaarschap valideren bij verwijderen van beoordelingen
**Als leerling** wil ik dat enkel ik mijn eigen beoordelingen kan verwijderen zodat andere gebruikers mijn reviews niet kunnen verwijderen.

> **Scope:** `verwijderBeoordeling()` in `BeoordelingService` controleert momenteel niet of de beoordeling toebehoort aan de ingelogde gebruiker.

- **Story points:** 2
- **Betrokken bestanden:** `service/BeoordelingService.java`, `controller/BeoordelingController.java`
- **Afhankelijk van:** Issue 4, Issue 5
- **Status:** Open

---

## Issue 11 — `LeerlingController` beveiligen met rollen
**Als ontwikkelaar** wil ik de `LeerlingController` beveiligen met de juiste `@PreAuthorize`-annotaties zodat leerlingdata enkel opvraagbaar is door bevoegde gebruikers (leerkracht of beheerder).

> **Scope:**
> - `GET /api/leerlingen` → `hasAnyRole('TEACHER', 'BEHEERDER')`
> - `GET /api/leerlingen/{id}/stats` → `hasAnyRole('TEACHER', 'BEHEERDER')`

- **Story points:** 2
- **Betrokken bestanden:** `controller/LeerlingController.java`
- **Afhankelijk van:** Issue 7
- **Status:** Open

---

## Issue 12 — `SchoolController` beveiligen en mock-data verwijderen
**Als ontwikkelaar** wil ik de `SchoolController` beveiligen met `@PreAuthorize("hasRole('BEHEERDER')")` en de hardcoded mock-data vervangen door echte logica zodat de controller productieklaar is.

> **Scope:**
> - `@PreAuthorize` toevoegen op alle endpoints
> - Hardcoded mock Smartschool-data op regels 52–61 verwijderen
> - `System.out.println`-calls vervangen door logger

- **Story points:** 2
- **Betrokken bestanden:** `controller/SchoolController.java`
- **Afhankelijk van:** Issue 7
- **Status:** Open

---

## Issue 13 — Seeders samenvoegen en opkuisen
**Als ontwikkelaar** wil ik de twee conflicterende seeders (`GebruikerSeed` en `LeerlingenSeed`) samenvoegen tot één consistente seeder zodat er geen dubbele of onvolledige testdata aangemaakt wordt bij het opstarten van de applicatie.

> **Probleem:** `LeerlingenSeed` roept de constructor `Gebruiker(voorNaam, achternaam, klas)` aan zonder `schoolId`, `email` en `rol` in te stellen, wat leidt tot NULL-waarden in de database.

- **Story points:** 2
- **Betrokken bestanden:** `seeds/GebruikerSeed.java`, `seeds/LeerlingenSeed.java`
- **Afhankelijk van:** Issue 4
- **Status:** Open

---

## Issue 14 — `System.out.println` vervangen door SLF4J-logging
**Als ontwikkelaar** wil ik alle `System.out.println`-aanroepen vervangen door SLF4J-logging (`@Slf4j` via Lombok) zodat de applicatie productieklare en configureerbare logging heeft.

- **Story points:** 1
- **Betrokken bestanden:** `controller/SchoolController.java`, `seeds/LeerlingenSeed.java`, `seeds/GebruikerSeed.java`
- **Status:** Open

---

## Overzicht

| # | Issue | SP | Afhankelijk van | Status |
|---|-------|----|-----------------|--------|
| 1 | `UitleningStatus`-enum | 2 | — | Open |
| 2 | Constructor-bug `Uitlening` | 1 | — | Open |
| 3 | `User`-entity uitbreiden | 2 | — | Open |
| 4 | `Gebruiker` → `User` migratie | 8 | 3 | Open |
| 5 | Hulpmethode SecurityContext | 2 | — | Open |
| 6 | Beheerder-login | 3 | — | ✅ GEDAAN |
| 7 | `@PreAuthorize` controllers | 3 | — | ✅ GEDAAN |
| 8 | Hardcoded `1L` verwijderen | 3 | 4, 5 | Open |
| 9 | Data-eigenaarschap endpoints | 5 | 4, 5 | Open |
| 10 | Eigenaarschap beoordeling verwijderen | 2 | 4, 5 | Open |
| 11 | `LeerlingController` beveiligen | 2 | 7 | Open |
| 12 | `SchoolController` beveiligen | 2 | 7 | Open |
| 13 | Seeders samenvoegen | 2 | 4 | Open |
| 14 | Logging (`System.out.println`) | 1 | — | Open |

**Totaal open story points: 33**
**Reeds gedaan: 6**
