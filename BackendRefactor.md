# Backend Refactor — GitLab Issues

> **Project:** GoSmartLib Backend (Spring Boot / Java)
> **Doel:** Volledige migratie van `Gebruiker` naar `User` entiteit, strikte datascoping per ingelogde gebruiker, en consolidatie van de beveiligingslaag.
> **Volgorde:** Issues zijn gesorteerd op uitvoeringsvolgorde; een issue hangt steeds af van alle issues met een lager nummer (tenzij anders aangegeven).

---

## Legenda gewichten

| Gewicht | Omschrijving |
|---------|-------------|
| 1 | Triviaal — één bestand, minimale impact |
| 2 | Klein — beperkte wijzigingen, duidelijk afgebakend |
| 3 | Gemiddeld — meerdere bestanden, overzichtelijke impact |
| 5 | Groot — cross-cutting changes over meerdere lagen |
| 8 | Zeer groot — systemische refactor met brede impact |

---

## Issues

---

### #1 — [GEDAAN] Beheerder-login configureren

**Als beheerder wil ik via een apart `/api/auth/admin/login`-endpoint inloggen met gebruikersnaam en gehashed wachtwoord, zodat ik toegang krijg tot het beheerderspaneel zonder gebruik te maken van Smartschool OAuth2.**

- **Gewicht:** 3
- **Status:** ✅ GEDAAN
- **Geïmplementeerd in:** `AdminAuthController`, `AdminGebruiker`, `AdminGebruikerRepository`, `AdminGebruikerSeed`

---

### #2 — [GEDAAN] Endpoints beveiligen via `hasRole`-annotaties

**Als systeembeheerder wil ik dat gevoelige endpoints zijn beveiligd met `@PreAuthorize("hasRole('...')")`, zodat onbevoegde rollen geen toegang krijgen tot beheer- of leerkrachtfuncties.**

- **Gewicht:** 5
- **Status:** ✅ GEDAAN
- **Geïmplementeerd in:** `BeheerController` (klasse-niveau), `BoekController`, `KlassLeeslijstController`, `StatistiekController`, `AdminReportController`

---

### #3 — `User` entiteit uitbreiden met profieldata

**Als ontwikkelaar wil ik dat de `User` entiteit wordt uitgebreid met de relevante velden uit `Gebruiker` (schoolId, voornaam, achternaam, email, klas, rol), zodat één entiteit de volledige identiteit van een ingelogde gebruiker bijhoudt en de `Gebruiker` entiteit overbodig wordt.**

- **Gewicht:** 5
- **Afhankelijkheden:** geen (foundational)
- **Betrokken bestanden:**
  - `entity/User.java` — velden toevoegen + relaties voorbereiden
  - `entity/Rol.java` — controleer of enum-waarden overeenkomen met JWT-rollen (`STUDENT`/`TEACHER`/`BEHEERDER`)
- **Aandachtspunten:**
  - Houd `AdminGebruiker` los; dit is geen `User`.
  - Voeg de `OneToMany`-relaties (`mappen`, `beoordelingen`, `uitleningen`) alvast toe als `@OneToMany(mappedBy = "user")`.

---

### #4 — `OAuth2LoginSuccessHandler` aanpassen om `User` volledig te vullen

**Als systeem wil ik dat na een succesvolle Smartschool-login de `User` entiteit direct wordt aangevuld met voornaam, achternaam, rol en klas (via OneRoster), zodat de gebruiker zonder verdere stappen de juiste rechten en profieldata heeft.**

- **Gewicht:** 3
- **Afhankelijkheden:** #3
- **Betrokken bestanden:**
  - `config/OAuth2LoginSuccessHandler.java` — update upsert-logica
  - `service/OneRosterService.java` — geeft al rol + klas terug, koppelen aan `User`
- **Aandachtspunten:**
  - Fallback bij OneRoster-fout: sla `rol = ONBEKEND` op, blokkeer geen login.
  - JWT-claims (`role`, `name`, `klas`) blijven ongewijzigd.

---

### #5 — Relaties migreren van `Gebruiker` naar `User` in entiteiten

**Als ontwikkelaar wil ik dat alle JPA-relaties die verwijzen naar `Gebruiker` worden bijgewerkt naar `User`, zodat de datastructuur consistent is en de `Gebruiker` entiteit volledig kan worden verwijderd.**

- **Gewicht:** 8
- **Afhankelijkheden:** #3
- **Te wijzigen entiteiten:**

  | Entiteit | Huidig veld | Nieuw veld |
  |----------|-------------|------------|
  | `Beoordeling` | `gebruiker (ManyToOne → Gebruiker)` | `user (ManyToOne → User)` |
  | `Uitlening` | `gebruiker (ManyToOne → Gebruiker)` | `user (ManyToOne → User)` |
  | `LeeslijstMap` | `gebruiker (ManyToOne → Gebruiker)` | `user (ManyToOne → User)` |
  | `KlassLeeslijst` | `leerkracht (ManyToOne → Gebruiker)` | `leerkracht (ManyToOne → User)` |

- **Aandachtspunten:**
  - Update alle `@JoinColumn`-namen in de DDL of via een migratiescript (zie #19).
  - Controleer `cascade`- en `orphanRemoval`-instellingen na de migratie.

---

### #6 — Repositories migreren van `Gebruiker` naar `User`

**Als ontwikkelaar wil ik dat alle repository-interfaces en JPQL/native queries worden bijgewerkt om `User` te gebruiken in plaats van `Gebruiker`, zodat de datalaag aansluit op het nieuwe entiteitsmodel.**

- **Gewicht:** 5
- **Afhankelijkheden:** #5
- **Te wijzigen repositories:**

  | Repository | Actie |
  |------------|-------|
  | `GebruikerRepository` | Methoden overnemen in `UserRepository`; bestand verwijderen |
  | `BeoordelingRepository` | `findByGebruiker_*` → `findByUser_*` |
  | `UitleningRepository` | Alle `findByGebruiker_GebruikerID*`-queries → `findByUser_Id*` |
  | `LeeslijstMapRepository` | `findByGebruiker_GebruikerID*` → `findByUser_Id*` |
  | `KlassLeeslijstRepository` | `findByLeerkracht_GebruikerID*` → `findByLeerkracht_Id*` |

---

### #7 — DTO's en Mappers bijwerken naar `User` entiteit

**Als ontwikkelaar wil ik dat alle DTO's en Mapper-klassen die `GebruikerDTO` of `Gebruiker` gebruiken worden omgezet naar equivalenten voor `User`, zodat de API-laag consistent is met het nieuwe model.**

- **Gewicht:** 3
- **Afhankelijkheden:** #6
- **Te wijzigen bestanden:**
  - `dto/GebruikerDTO.java` → hernoem naar `UserDTO.java` of pas bestaande `UserDTO` uit
  - `mappers/GebruikerMapper.java` → hernoem naar `UserMapper.java`
  - Controleer alle andere DTO's (`UitleningDTO`, `BeoordelingDTO`, `LeeslijstMapDTO`) op referenties naar `GebruikerDTO`

---

### #8 — Services migreren van `Gebruiker` naar `User`

**Als ontwikkelaar wil ik dat alle service-klassen worden bijgewerkt om `User` te gebruiken in plaats van `Gebruiker`, zodat de businesslogica aansluit op het nieuwe entiteitsmodel.**

- **Gewicht:** 5
- **Afhankelijkheden:** #7
- **Te wijzigen services:**

  | Service | Actie |
  |---------|-------|
  | `GebruikerService` | Methoden hernoemen/verplaatsen naar `UserService`; intern alle `Gebruiker`-types vervangen door `User` |
  | `BeoordelingService` | Vervang `Gebruiker`-referenties door `User` |
  | `StatistiekService` | Vervang `Gebruiker`-referenties door `User` |
  | `KlassLeeslijstService` | Vervang `Gebruiker`-referenties door `User` |

---

### #9 — Controllers migreren: `GebruikerController` → `UserController`

**Als ontwikkelaar wil ik dat de `GebruikerController` wordt omgezet naar een `UserController` die werkt met de `User` entiteit en de bijgewerkte `UserService`, zodat de REST-API consistent is met het nieuwe gebruikersmodel.**

- **Gewicht:** 3
- **Afhankelijkheden:** #8
- **Betrokken bestanden:**
  - `controller/GebruikerController.java` → hernoem + intern refactor
  - `controller/LeerlingController.java` → update queries via `UserService`

---

### #10 — `Gebruiker` entiteit en `GebruikerRepository` verwijderen

**Als ontwikkelaar wil ik dat de verouderde `Gebruiker` entiteit, `GebruikerRepository`, `GebruikerService` en `GebruikerMapper` volledig worden verwijderd, zodat er geen dubbel gebruikersmodel meer bestaat en de codebase schoon is.**

- **Gewicht:** 2
- **Afhankelijkheden:** #9
- **Te verwijderen bestanden:**
  - `entity/Gebruiker.java`
  - `repository/GebruikerRepository.java`
  - `service/GebruikerService.java`
  - `mappers/GebruikerMapper.java`
  - `dto/GebruikerDTO.java`
- **Aandachtspunten:** Compileer na verwijdering en fix eventuele resterende referenties.

---

### #11 — Hulpklasse aanmaken voor huidige gebruiker via `SecurityContext`

**Als ontwikkelaar wil ik een centrale hulpklasse (bijv. `AuthenticatedUserService`) die op basis van het JWT-subject de ingelogde `User` ophaalt uit de database, zodat alle controllers en services op een uniforme manier de huidige gebruiker kunnen identificeren zonder path-parameters te vertrouwen.**

- **Gewicht:** 2
- **Afhankelijkheden:** #3 (kan parallel lopen met #4–#10)
- **Voorstel implementatie:**
  ```java
  @Service
  public class AuthenticatedUserService {
      public User getCurrentUser() {
          String smartschoolId = SecurityContextHolder.getContext()
              .getAuthentication().getName();
          return userRepository.findBySmartschoolId(smartschoolId)
              .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED));
      }
  }
  ```

---

### #12 — Hardcoded `tijdelijkGebruikerId = 1L` vervangen in `BeoordelingService`

**Als leerling wil ik dat mijn ingediende boekrecensies correct aan mijn eigen account worden gekoppeld in plaats van aan een hardcoded tijdelijke gebruiker, zodat mijn recensies juist worden opgeslagen en aan mij toebehoord.**

- **Gewicht:** 3
- **Afhankelijkheden:** #11
- **Betrokken bestanden:**
  - `service/BeoordelingService.java` — vervang `Long tijdelijkGebruikerId = 1L` (2 locaties met TODO-commentaar) door `authenticatedUserService.getCurrentUser()`
  - `controller/BeoordelingController.java` — verwijder `gebruikerId` uit de request body indien aanwezig; gebruik SecurityContext

---

### #13 — Eigenaarschapsvalidatie voor leengeschiedenis, uitleningen en leeslijsten

**Als leerling wil ik dat ik uitsluitend mijn eigen leengeschiedenis, actieve uitleningen en persoonlijke leeslijsten kan opvragen en aanpassen, zodat de persoonlijke data van andere gebruikers niet toegankelijk is via de API.**

- **Gewicht:** 5
- **Afhankelijkheden:** #11, #9
- **Beveiligingslek (huidig):** `GET /api/gebruiker/999/leningen` werkt voor elke geldige JWT zonder eigenaarschapscontrole.
- **Aanpak:**
  - Verwijder `{gebruikerId}` als path-parameter in endpoints die louter de eigen data tonen.
  - Gebruik `authenticatedUserService.getCurrentUser().getId()` intern in de service.
  - Behoud `{gebruikerId}` als path-parameter alleen voor beheerder- en leerkracht-endpoints (maar voeg daar een `@PreAuthorize`-controle aan toe).
- **Betrokken endpoints:**

  | Endpoint | Huidig | Gewenst |
  |----------|--------|---------|
  | `GET /{id}/mappen` | Geen eigenaarschapscheck | Gebruik SecurityContext |
  | `POST /{id}/mappen/...` | Geen eigenaarschapscheck | Gebruik SecurityContext |
  | `GET /{id}/leningen` | Geen eigenaarschapscheck | Gebruik SecurityContext |
  | `GET /{id}/geschiedenis` | Geen eigenaarschapscheck | Gebruik SecurityContext |
  | `POST /{id}/lenen/{boekId}` | Geen eigenaarschapscheck | Gebruik SecurityContext |
  | `POST /{id}/lenen/bulk` | Geen eigenaarschapscheck | Gebruik SecurityContext |

---

### #14 — Eigenaarschapsvalidatie voor recensies (aanmaken en verwijderen)

**Als leerling wil ik dat ik uitsluitend mijn eigen recensies kan verwijderen, en dat bij het aanmaken van een recensie automatisch mijn account wordt gebruikt, zodat andere gebruikers mijn recensies niet kunnen manipuleren.**

- **Gewicht:** 3
- **Afhankelijkheden:** #12, #13
- **Betrokken bestanden:**
  - `controller/BeoordelingController.java`:
    - `DELETE /api/reviews/{id}` — voeg eigenaarschapscheck toe (of `hasRole('BEHEERDER')`)
    - `POST /api/reviews/{id}/report` — koppel report aan huidig ingelogde `User`
  - `service/BeoordelingService.java` — voeg validatie toe in `verwijderBeoordeling`

---

### #15 — Klassikale leeslijsten valideren op leerkracht-eigenaarschap

**Als leerkracht wil ik dat ik uitsluitend mijn eigen klassikale leeslijsten kan aanpassen of verwijderen, zodat andere leerkrachten mijn leeslijsten niet kunnen wijzigen.**

- **Gewicht:** 3
- **Afhankelijkheden:** #11, #13
- **Betrokken bestanden:**
  - `controller/KlassLeeslijstController.java`:
    - `POST /{leeslijstId}/boeken/{boekId}` — controleer dat de ingelogde leerkracht eigenaar is
    - `DELETE /{leeslijstId}/boeken/{boekId}` — idem
    - `PUT /{id}/naam` — idem
    - `DELETE /{id}` — idem
  - `service/KlassLeeslijstService.java` — validatie toevoegen

---

### #16 — Statistieken-endpoints beveiligen en scopen per rol

**Als leerling wil ik dat mijn statistieken alleen door mijzelf of een beheerder kunnen worden ingezien. Als leerkracht wil ik alleen de statistieken van mijn eigen klas kunnen inzien, zodat leerlingdata privé blijft.**

- **Gewicht:** 3
- **Afhankelijkheden:** #11, #13
- **Beveiligingslekken (huidig):**
  - `GET /api/statistieken/leerling/{schoolId}` — geen `@PreAuthorize`, publiek toegankelijk
  - `GET /api/statistieken/leerkracht` — leerkracht kan elke `klasId` opvragen
- **Aanpak:**
  - Voeg `@PreAuthorize("hasAnyRole('BEHEERDER', 'STUDENT')")` toe aan `/statistieken/leerling/{schoolId}` + valideer dat de ingelogde leerling de eigen `schoolId` opvraagt.
  - In `StatistiekService.getLeerkrachtStatistieken(klasId)` — valideer dat de ingelogde leerkracht verantwoordelijk is voor die klas.

---

### #17 — Seed-data aanpassen naar nieuwe `User` entiteit

**Als ontwikkelaar wil ik dat de `GebruikerSeed` en `LeerlingenSeed` worden bijgewerkt om `User` entiteiten aan te maken met de juiste profielvelden, zodat de testdata consistent is met het nieuwe gebruikersmodel en de applicatie correct opstart.**

- **Gewicht:** 2
- **Afhankelijkheden:** #10
- **Betrokken bestanden:**
  - `seeds/GebruikerSeed.java` → hernoem naar `UserSeed.java`, vervang `Gebruiker` door `User`
  - `seeds/LeerlingenSeed.java` → idem

---

### #18 — Integration tests schrijven voor beveiligde endpoints

**Als ontwikkelaar wil ik integration tests die verifiëren dat een leerling geen data van een andere leerling kan opvragen en dat rolgebaseerde endpoints worden afgeschermd, zodat regressies in de beveiligingslaag vroeg worden gedetecteerd.**

- **Gewicht:** 5
- **Afhankelijkheden:** #13, #14, #15, #16
- **Scenario's:**
  - Leerling A vraagt leengeschiedenis van leerling B op → `403 Forbidden`
  - Leerling vraagt eigen leengeschiedenis op → `200 OK`
  - Niet-beheerder roept beheerder-endpoint aan → `403 Forbidden`
  - Leerkracht verwijdert leeslijst van andere leerkracht → `403 Forbidden`
  - Anoniem verzoek naar beveiligd endpoint → `401 Unauthorized`

---

### #19 — Databasemigratiescript opstellen voor productie

**Als ontwikkelaar wil ik een SQL-migratiescript (Flyway of Liquibase) dat de bestaande `gebruiker`-tabeldata migreert naar de uitgebreide `user`-tabel en alle foreign keys bijwerkt, zodat productiedata behouden blijft na de refactor.**

- **Gewicht:** 5
- **Afhankelijkheden:** #10 (alle entiteitswijzigingen zijn afgerond)
- **Stappen in het script:**
  1. Voeg nieuwe kolommen toe aan `user`-tabel (`school_id`, `voornaam`, `achternaam`, `klas`, `rol`, etc.)
  2. Kopieer data van `gebruiker` → `user` (matching op `schoolId` of handmatig als er geen overlap is)
  3. Update foreign keys in `beoordeling`, `uitlening`, `leeslijst_map`, `klass_leeslijst`
  4. Drop `gebruiker`-tabel
- **Aandachtspunten:** Test het script eerst op een lokale kopie van de productiedatabase; maak een backup vóór uitvoering.

---

## Samenvatting

| # | Titel | Gewicht | Status |
|---|-------|---------|--------|
| 1 | Beheerder-login configureren | 3 | ✅ GEDAAN |
| 2 | Endpoints beveiligen via `hasRole` | 5 | ✅ GEDAAN |
| 3 | `User` entiteit uitbreiden met profieldata | 5 | — |
| 4 | `OAuth2LoginSuccessHandler` aanpassen | 3 | — |
| 5 | Relaties migreren van `Gebruiker` → `User` in entiteiten | 8 | — |
| 6 | Repositories migreren naar `User` | 5 | — |
| 7 | DTO's en Mappers bijwerken | 3 | — |
| 8 | Services migreren naar `User` | 5 | — |
| 9 | `GebruikerController` → `UserController` | 3 | — |
| 10 | `Gebruiker` entiteit en repository verwijderen | 2 | — |
| 11 | Hulpklasse voor huidige gebruiker via SecurityContext | 2 | — |
| 12 | Hardcoded `tijdelijkGebruikerId` vervangen | 3 | — |
| 13 | Eigenaarschapsvalidatie voor leningen en leeslijsten | 5 | — |
| 14 | Eigenaarschapsvalidatie voor recensies | 3 | — |
| 15 | Klassikale leeslijsten valideren op eigenaarschap | 3 | — |
| 16 | Statistieken beveiligen en scopen per rol | 3 | — |
| 17 | Seed-data aanpassen naar `User` entiteit | 2 | — |
| 18 | Integration tests voor beveiligde endpoints | 5 | — |
| 19 | Databasemigratiescript voor productie | 5 | — |
| | **Totaal** | **76** | |
