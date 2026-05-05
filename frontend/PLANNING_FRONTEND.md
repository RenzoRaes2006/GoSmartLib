# Planning: Frontend taken

## Context

De backend is volledig werkend:
- Login via Smartschool OAuth2
- Rol en klas worden opgehaald via OneRoster API
- JWT-token bevat: `id` (smartschoolId), `name`, `role` ("student"/"teacher"), `klas` (bv. "2ITSOF2")
- Token wordt opgeslagen in `localStorage` via `AuthService.ts`

De frontend heeft al routes, guards, services en componenten — maar een aantal zaken zijn nog niet correct gekoppeld.

---

## Stap 1 — Auth interceptor aanzetten

**Bestand:** `frontend/src/app/app.config.ts`

De interceptor die de JWT-token meestuurt bij elke API-call staat uitgecommentarieerd.
Zonder dit mislukken alle beveiligde API-calls stilletjes.

Zet de `authInterceptor` aan in `app.config.ts`.

**Bestand interceptor:** `frontend/src/app/interceptors/interceptor.ts`

---

## Stap 2 — Guards toepassen op routes

**Bestand:** `frontend/src/app/app.routes.ts`

De guards `authGuard` en `adminGuard` zijn gebouwd maar nergens toegepast.
Elke route is nu toegankelijk zonder login.

Pas per route de juiste guard toe:

| Route | Guard |
|---|---|
| `/catalogus`, `/leeslijst`, `/reserveren`, `/leengeschiedenis`, ... | `authGuard` |
| `/admin`, `/beheer/aanvragen`, `/beheer/scan`, `/beheer/actieve-leningen` | `adminGuard` |
| `/leerkracht-dashboard`, `/leeslijsten` | guard voor role = "teacher" |
| `/login`, `/login-success` | geen guard |

**Bestaande guards:**
- `frontend/src/app/guards/auth.guard.ts` — controleert of gebruiker ingelogd is
- `frontend/src/app/guards/admin-guard.ts` — controleert of role = "beheerder"

---

## Stap 3 — Redirect na login op basis van rol

**Bestand:** `frontend/src/app/login-success/login-success.ts`

Nu gaat iedereen na login naar `/`. Pas de redirect aan op basis van rol:

| Rol | Redirect naar |
|---|---|
| `student` | `/` (homepagina) |
| `teacher` | `/leerkracht-dashboard` |
| `beheerder` | `/admin` |

De rol lees je via `authService.currentUser().role`.

---

## Stap 4 — Navbar rol-bewust maken

**Bestand:** `frontend/src/app/navbar/navbar.ts` en `navbar.html`

De navbar toont nu altijd dezelfde links voor iedereen.
Pas dit aan zodat navigatie-items afhangen van de rol:

- `student` → ziet: Catalogus, Mijn lijsten, Klasleeslijsten, Winkelmandje
- `teacher` → ziet: Catalogus, Klasleeslijsten beheren, Leerkracht dashboard
- `beheerder` → ziet: Admin dashboard, Beheer-links

De rol haal je op via `authService.currentUser()` (signal).

---

## Stap 5 — Feature branch: klassenleeslijsten voor leerling

**Branch:** `feature-74-KlasleeslijstLeerling`

De componenten bestaan al:
- `frontend/src/app/klassenleeslijsten-leerling/` — overzicht van klasleeslijsten voor leerling
- `frontend/src/app/klassenleeslijst-detail-leerling/` — detail van één klasleeslijst

Deze moeten de `klas` uit de JWT-token gebruiken om de juiste leeslijsten op te halen via de backend.

De klas lees je via `authService.currentUser().klas` (bv. `"2ITSOF2"`).

**Service:** `frontend/src/app/services/KlassLeeslijstService.ts`

---

## Checklist

- [ ] Stap 1: auth interceptor aanzetten in `app.config.ts`
- [ ] Stap 2: guards toepassen op routes in `app.routes.ts`
- [ ] Stap 3: redirect na login op basis van rol in `login-success.ts`
- [ ] Stap 4: navbar rol-bewust maken in `navbar.ts` / `navbar.html`
- [ ] Stap 5: klassenleeslijsten-leerling koppelen aan `klas` uit JWT
