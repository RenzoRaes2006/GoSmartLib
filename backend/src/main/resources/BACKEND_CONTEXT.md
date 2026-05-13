# Backend Context — GoSmartLib

Lees dit bestand aan het begin van elke sessie i.p.v. alle broncode te herlezen.

---

## Stack

- **Java 21 / Spring Boot 3.5**
- **Spring Security** + **OAuth2 Client** + **JWT (JJWT 0.11.5)**
- **MySQL** via Spring Data JPA / Hibernate (`ddl-auto=update`)
- **Lombok** aanwezig maar niet overal gebruikt
- Backend draait op poort **8081**
- Frontend: Angular op **https://frontend-sp09.cloud-ap.be** / localhost:4200
- Backend productie-URL: **https://api-sp09.cloud-ap.be**

---

## Authenticatie — twee flows

### 1. Smartschool OAuth2 (leerling + leerkracht)
```
Frontend /login
  → klik "Aanmelden met Smartschool"
  → window.location.href = backend/oauth2/authorization/smartschool
  → Spring Security → Smartschool OAuth
  → OAuth2LoginSuccessHandler.java
      - haalt smartschoolId, naam, username op uit OAuth2User
      - roept OneRosterService aan voor rol ("student"/"teacher") en klas ("2SOF")
      - slaat smartschoolId op in User-tabel (login tracking)
      - genereert JWT
  → redirect naar frontend /login-success?token=...
  → LoginSuccessComponent slaat token op, routeert op basis van role
```

### 2. Beheerder-login (eigen formulier)
```
Frontend /beheerder-login
  → POST /api/auth/admin/login  { gebruikersnaam, wachtwoord }
  → AdminAuthController valideert tegen admin_gebruikers-tabel (BCrypt)
  → genereert JWT met role="beheerder"
  → frontend ontvangt { token: "..." }, slaat op, routeert naar /admin
```

---

## JWT-structuur

| Claim | Waarde | Voorbeeld |
|-------|--------|-----------|
| `sub` | smartschoolId (string) of "admin_username" | `"E4LD6Wfpo22xSAjmOUHj2g=="` |
| `role` | rol van gebruiker | `"student"` / `"teacher"` / `"beheerder"` |
| `name` | weergavenaam | `"Haitam"` |
| `klas` | klasnaam | `"2SOF"` / `"nvt"` |
| `iat` | aangemaakt | timestamp |
| `exp` | vervalt na 24u | timestamp |

JWT secret staat in `application.properties` → `app.jwt.secret`

---

## Spring Security filter-flow (per request)

```
Request binnenkomt
  → JwtAuthenticationFilter
      - leest Authorization: Bearer <token>
      - valideert token (JwtService.isTokenValid)
      - extraheert smartschoolId (subject) + role
      - zet ROLE_STUDENT / ROLE_TEACHER / ROLE_BEHEERDER als GrantedAuthority
      - plaatst in SecurityContextHolder
  → SecurityConfig regels
      - permitAll: /, /login, /error, /api/boeken/**, /api/auth/admin/login
      - anyRequest: authenticated
  → Controller-methode
      - @PreAuthorize("hasRole('BEHEERDER')") etc. checkt de authority
```

---

## Rollen en routing

| JWT `role` | Spring Authority | Frontend guard | Dashboard |
|-----------|-----------------|---------------|-----------|
| `student` | `ROLE_STUDENT` | `authGuard` | `/` (HomePage) |
| `teacher` | `ROLE_TEACHER` | `teacherGuard` | `/leerkracht-dashboard` |
| `beheerder` | `ROLE_BEHEERDER` | `adminGuard` | `/admin` |

---

## Entiteiten — belangrijk onderscheid

### `User` (`users`-tabel) — auth-entiteit
- Enkel `smartschoolId` (+ Lombok `@Data`, `@Builder`)
- Wordt aangemaakt bij elke OAuth2-login (login tracking)
- **TODO (Issue 3):** Moet uitgebreid worden met `naam`, `rol`, `klas`, `email`

### `Gebruiker` (`Gebruikers`-tabel) — bibliotheek-entiteit
- Heeft `schoolId` (long), `VoorNaam`, `Achternaam`, `Email`, `klas`, `Rol`-enum
- Heeft relaties naar `LeeslijstMap`, `Beoordeling`, `Uitlening`
- **⚠️ Veldnamen zijn PascalCase — afwijkend van Java-conventie**
- **⚠️ Moet vervangen worden door `User` (BackendRefactor Issue 4 — 8 SP)**

### `AdminGebruiker` (`admin_gebruikers`-tabel) — beheerder-auth
- `gebruikersnaam` + `wachtwoordHash` (BCrypt) + `naam`
- Wordt aangemaakt via `AdminGebruikerSeed` (standaard: admin / Admin@2024!)

---

## Beveiliging per controller

| Controller | Beveiliging | Niveau |
|-----------|-------------|--------|
| `AdminReportController` | `@PreAuthorize("hasRole('BEHEERDER')")` | class |
| `BeheerController` | `@PreAuthorize("hasRole('BEHEERDER')")` | class |
| `BoekController` | `@PreAuthorize("hasRole('BEHEERDER')")` | methode (write) |
| `InstellingController` | `@PreAuthorize("hasRole('BEHEERDER')")` | methode (PUT) |
| `ScanController` | `@PreAuthorize("hasRole('BEHEERDER')")` | methode |
| `StatistiekController` | BEHEERDER / TEACHER per methode | methode |
| `KlassLeeslijstController` | `@PreAuthorize("hasRole('TEACHER')")` | methode (write) |
| `GebruikerController` | `hasAnyRole('TEACHER','BEHEERDER')` | aanvragen-methoden |
| `LeerlingController` | **⚠️ nog niet beveiligd (Issue 11)** | — |
| `SchoolController` | **⚠️ nog niet beveiligd (Issue 12)** | — |
| `AdminAuthController` | `permitAll` | — |

---

## Bekende bugs / TODO's in de code

| Locatie | Probleem |
|---------|---------|
| `BeoordelingService` regel 42, 62 | `Long tijdelijkGebruikerId = 1L` — hardcoded, alle reviews → gebruiker 1 |
| `BeoordelingController` regel 46 | Zelfde hardcoded `1L` |
| `Uitlening` constructor | `uitleenDatum` en `inleverDatum` worden op `null` gezet ipv de param |
| `GebruikerController` | Geen eigenaarschapsvalidatie — leerling A kan data van B opvragen |
| `SchoolController` | Mock-data in productie (regels 52–61) + geen @PreAuthorize |

---

## Open refactoring issues (zie BackendRefactor.md in root)

| # | Samenvatting | SP | Status |
|---|-------------|-----|--------|
| 1 | `UitleningStatus`-enum | 2 | Open |
| 2 | Constructor-bug `Uitlening` | 1 | Open |
| 3 | `User` uitbreiden met profielgegevens | 2 | Open |
| 4 | `Gebruiker` → `User` migratie | 8 | Open |
| 5 | Hulpmethode SecurityContext | 2 | Open |
| 6 | Beheerder-login | 3 | ✅ GEDAAN |
| 7 | `@PreAuthorize` controllers | 3 | ✅ GEDAAN |
| 8 | Hardcoded `1L` verwijderen | 3 | Open |
| 9 | Data-eigenaarschap endpoints | 5 | Open |
| 10 | Eigenaarschap beoordeling verwijderen | 2 | Open |
| 11 | `LeerlingController` beveiligen | 2 | Open |
| 12 | `SchoolController` beveiligen | 2 | Open |
| 13 | Seeders samenvoegen | 2 | Open |
| 14 | Logging (System.out.println) | 1 | Open |

**33 SP open — 6 SP gedaan**

---

## Sleutelbestanden

| Doel | Pad |
|------|-----|
| JWT aanmaken + lezen | `config/JwtService.java` |
| JWT filter (per request) | `config/JwtAuthenticationFilter.java` |
| OAuth2 login handler | `config/OAuth2LoginSuccessHandler.java` |
| Beheerder login endpoint | `controller/AdminAuthController.java` |
| Beveiligingsregels | `config/SecurityConfig.java` |
| OneRoster integratie | `service/OneRosterService.java` |
| Auth-entiteit | `entity/User.java` |
| Bibliotheek-gebruiker (verouderd) | `entity/Gebruiker.java` |
| Beheerder-entiteit | `entity/AdminGebruiker.java` |
| Configuratie | `resources/application.properties` |
| Refactoring issues | `BackendRefactor.md` (root van project) |
