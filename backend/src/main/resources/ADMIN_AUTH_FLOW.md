# Admin Authenticatie — hoe het werkt

Dit document beschrijft de volledige login-flow voor de **applicatiebeheerder** (`admin`), inclusief de eerste-login-wachtwoordwijziging. Bedoeld om later snel terug grip te krijgen op wat er gebeurt tussen frontend en backend.

---

## 1. Twee werelden, twee logins

In dit project bestaan twee soorten gebruikers die volledig gescheiden inloggen:

| Wereld | Wie | Hoe inloggen | Rol in JWT |
|---|---|---|---|
| **Smartschool** | leerlingen, leerkrachten, bibliotheekbeheerders | OAuth2-redirect naar Smartschool | `student`, `teacher`, `beheerder` |
| **Intern** | applicatiebeheerder (school-IT) | Gebruikersnaam + wachtwoord op `/admin-login` | `admin` |

De `admin` heeft géén Smartschool-account nodig. Hij/zij staat als rij in de tabel `admin_gebruikers` in de eigen database. Daarom is er een aparte login-flow.

---

## 2. De spelers (welk bestand doet wat)

### Backend
- `AdminGebruiker.java` — entiteit/tabel met `gebruikersnaam`, `wachtwoordHash`, `naam`, `moetWachtwoordWijzigen`.
- `AdminGebruikerSeed.java` — maakt bij opstarten een `admin`-rij aan met wachtwoord `Admin@2024!` als die nog niet bestaat. Vlag staat op `true`.
- `AdminAuthController.java` — twee endpoints: `/api/auth/admin/login` en `/api/auth/admin/wachtwoord-wijzigen`.
- `JwtService.java` — maakt en valideert JWT-tokens. Twee soorten: gewoon token (24u) en reset-token (15 min, met `scope: "wachtwoord-wijzigen"`).
- `JwtAuthenticationFilter.java` — leest bij **elke** request de `Authorization`-header, valideert het token, zet de gebruiker in de SecurityContext zodat de controllers weten wie er belt.
- `SecurityConfig.java` — bepaalt welke URL's publiek zijn en welke een geldig token nodig hebben.

### Frontend
- `admin-login.ts` + `admin-login.html` — het scherm. Toont **één van twee** formulieren: login of wachtwoord-wijzig.
- `AuthService.ts` — leest het JWT uit `localStorage`, decodeert het, en houdt een `currentUser` signal bij. De rest van de app kijkt naar dat signal.
- `interceptor.ts` — hangt bij elke uitgaande HTTP-call automatisch `Authorization: Bearer <jwtToken>` aan, **tenzij** de aanroeper zelf al een header heeft gezet.
- `admin-guard.ts` — bewaakt routes onder `/admin`: alleen door als `currentUser.role === 'admin'`.

---

## 3. De twee tokens — niet door elkaar halen

| | Gewoon token | Reset-token |
|---|---|---|
| Wanneer aangemaakt | Na geslaagde login als vlag `false` is, of na succesvol wachtwoord wijzigen | Na geslaagde login als vlag `true` is |
| Geldig | 24 uur | 15 minuten |
| Claims | `sub`, `role`, `name`, `klas` | `sub`, `scope: "wachtwoord-wijzigen"` |
| Mag wat | Toegang tot alle endpoints die `role=admin` toelaten | **Enkel** `/api/auth/admin/wachtwoord-wijzigen` |
| Opgeslagen in | `localStorage` als `jwtToken` | Alleen in component-geheugen van `AdminLogin` (verdwijnt bij pagina-refresh) |

Het reset-token wordt **bewust niet** in `localStorage` gezet. Het heeft een specifiek, eenmalig doel. Als je het kwijt bent, log je gewoon opnieuw in.

---

## 4. Flow A — Gewone admin-login (vlag al `false`)

```
Browser                          Backend
  |                                |
  |  POST /api/auth/admin/login    |
  |  Body: { gebruikersnaam,       |
  |          wachtwoord }          |
  | -----------------------------> |
  |                                | 1. Zoek admin op gebruikersnaam
  |                                | 2. passwordEncoder.matches(hash)?
  |                                | 3. Vlag moetWachtwoordWijzigen=false
  |                                | 4. jwtService.generateToken(...)
  |                                |
  |   200 { token: "eyJ..." }      |
  | <----------------------------- |
  |                                |
  | localStorage.setItem(...)      |
  | router.navigate(['/admin'])    |
  |                                |
  |  GET /api/.../iets-admin       |
  |  Header: Authorization:        |
  |          Bearer eyJ...         |
  | -----------------------------> |
  |                                | JwtAuthenticationFilter:
  |                                |   - valid? ja
  |                                |   - role=admin → SecurityContext
  |                                | Controller draait.
```

**Wat de frontend stuurt:** JSON-body met gebruikersnaam en wachtwoord. Verder niets.
**Wat de backend teruggeeft:** `{ "token": "eyJ..." }`. Niets meer.
**Wat de frontend dan doet:** token in `localStorage` zetten, decoderen om `currentUser` te vullen, doorsturen naar `/admin`.
**Bij volgende requests:** `interceptor.ts` plakt automatisch `Authorization: Bearer <token>` op elke call naar je eigen backend.

---

## 5. Flow B — Eerste admin-login (vlag = `true`)

Dit is de flow die we net hebben toegevoegd. Hij heeft **twee HTTP-calls**.

### Stap 1 — Login

```
Browser                          Backend
  |  POST /api/auth/admin/login    |
  |  Body: { admin, Admin@2024! }  |
  | -----------------------------> |
  |                                | Wachtwoord klopt, MAAR
  |                                | vlag moetWachtwoordWijzigen=true.
  |                                | → géén gewoon token!
  |                                | → reset-token aanmaken (15 min)
  |                                |
  |  200 {                         |
  |    moetWachtwoordWijzigen:true,|
  |    tijdelijkToken: "eyJ..."    |
  |  }                             |
  | <----------------------------- |
  |                                |
  | AdminLogin.ts:                 |
  |   stap = 'wijzigen'            |
  |   tijdelijkToken = response... |
  |                                |
  | Geen redirect.                 |
  | Geen localStorage.             |
  | UI toont nieuw formulier.      |
```

**Belangrijk:** er staat op dit moment **niets** in `localStorage`. De gebruiker is technisch nog niet ingelogd. Het tijdelijke token leeft alleen in het geheugen van het component. Als je nu de pagina refresht, moet je opnieuw beginnen.

### Stap 2 — Wachtwoord wijzigen

```
Browser                          Backend
  |  POST /api/auth/admin/         |
  |       wachtwoord-wijzigen      |
  |  Header: Authorization:        |
  |          Bearer <tijdelijk>    |
  |  Body: { nieuwWachtwoord }     |
  | -----------------------------> |
  |                                | 1. Bearer-token uit header lezen
  |                                | 2. jwtService.isTokenValid?
  |                                | 3. extractScope == "wachtwoord-wijzigen"?
  |                                | 4. nieuwWachtwoord.length >= 8?
  |                                | 5. Hash opslaan, vlag op false
  |                                | 6. Gewoon JWT aanmaken (24u)
  |                                |
  |   200 { token: "eyJ..." }      |
  | <----------------------------- |
  |                                |
  | localStorage.setItem('jwtToken')|
  | router.navigate(['/admin'])    |
```

Vanaf hier is de admin in dezelfde staat als Flow A: hij heeft een gewoon JWT in `localStorage`, `currentUser.role === 'admin'`, en mag overal in waar de `adminGuard` op staat.

Bij **volgende logins** is `moetWachtwoordWijzigen` nu `false` in de DB → de admin volgt voortaan Flow A.

---

## 6. Ter vergelijking — Smartschool-login (kort)

```
Browser klikt 'Login met Smartschool'
   → backend redirect naar Smartschool
   → gebruiker logt in bij Smartschool
   → Smartschool redirect terug met code
   → backend wisselt code voor user-info
   → OAuth2LoginSuccessHandler maakt JWT
   → redirect naar /login-success?token=...
   → frontend leest token uit URL en zet 'm in localStorage
```

Dezelfde `jwtToken`-sleutel in `localStorage`, dezelfde interceptor, dezelfde `JwtAuthenticationFilter`. Vanaf het moment dat het token bestaat, is er **geen verschil** tussen "ingelogd via Smartschool" en "ingelogd via admin-login" — behalve de `role`-claim in het token.

Dat is precies wat je wilt: de rest van de codebase hoeft niet te weten via welke deur iemand binnenkwam.

---

## 7. Waar je makkelijk de controle verliest (en hoe je 'm terugkrijgt)

### Plek 1: oude tokens in `localStorage`
Als je herhaaldelijk test, blijft een oud `jwtToken` plakken. Symptomen: je denkt dat je uitgelogd bent, maar de app stuurt je toch naar `/admin`. Of: je krijgt 403's omdat het oude token verlopen is.

**Fix:** voor elke test: open DevTools → Application → Local Storage → wis `jwtToken`. Of plak in de console: `localStorage.clear(); location.reload();`.

### Plek 2: interceptor overschrijft je eigen header
De interceptor hangt automatisch `Authorization: Bearer <jwtToken>` aan elke call. Tot voor kort overschreef hij **ook** een header die je zelf al gezet had — dat zou je reset-token vervangen door een oud, ongeldig token. Daarom staat er nu:

```ts
if (req.headers.has('Authorization')) {
    return next(req);
}
```

→ Als jij in een service al een `Authorization`-header zet, laat de interceptor 'm met rust. Belangrijk om te weten als je later andere "speciale" tokens introduceert.

### Plek 3: de twee tokens door elkaar halen
Het reset-token heeft géén `role`-claim. Als je het per ongeluk in `localStorage` als `jwtToken` zou opslaan, zou `JwtAuthenticationFilter` 'm valid vinden, maar `extractRole` zou `null` teruggeven en bij `role.toUpperCase()` crashen — of stilletjes geen authority toekennen. Resultaat: 401's terwijl je "ingelogd" lijkt. Daarom: reset-token blijft in component-geheugen, klaar.

### Plek 4: de seeded admin in een bestaande database
`ddl-auto=update` voegt de nieuwe kolom `moet_wachtwoord_wijzigen` toe aan een al bestaande tabel. Voor rijen die er al stonden is de default `false` (0). Dus als jouw lokale DB al een admin-rij heeft, zit die met de vlag op `false` en wordt de wijzig-flow niet getriggerd.

**Fix om te testen:**
```sql
UPDATE admin_gebruikers SET moet_wachtwoord_wijzigen = 1 WHERE gebruikersnaam = 'admin';
-- of harder:
DELETE FROM admin_gebruikers WHERE gebruikersnaam = 'admin';
-- en herstart de backend; de seed zet 'm opnieuw met vlag=1
```

Zodra we Flyway hebben (stap 3 in de issue), zet de init-migration de juiste defaults voor nieuwe DB's en blijft dit consistent.

---

## 8. Hoe je live controleert wat er gebeurt

Je hoeft niets te raden. Bij elke twijfel:

1. **Network-tab in DevTools, F12 → Network.** Filter op `auth`. Klik op een request → zie precies wat verstuurd is (Request Payload) en teruggekomen (Response). Daar staan al jouw vragen letterlijk in.
2. **JWT decoderen.** Plak een token in [jwt.io](https://jwt.io). Je ziet `sub`, `role`, `scope`, `exp`. Géén geheimen, een JWT is publiek leesbaar — alleen niet **wijzigbaar** zonder de server-secret.
3. **`localStorage` bekijken.** DevTools → Application → Local Storage → `http://localhost:4200`. Je ziet `jwtToken` (of niet).
4. **Backend log.** Spring logt elke request. Als je geen log ziet voor `/api/auth/admin/login`, dan komt je call niet eens aan (typisch: verkeerde poort, CORS).
5. **`currentUser` in de console.** Open de browser-console, type `localStorage.getItem('jwtToken')` om het token te zien.

---

## 9. Mentale samenvatting in vier zinnen

1. Admin logt in op een eigen endpoint, niet via Smartschool.
2. Bij eerste login krijgt hij geen gewoon token maar een tijdelijk reset-token dat alleen het wijzig-endpoint mag aanroepen.
3. Na het wijzigen krijgt hij wél een gewoon token, en vanaf dan is hij in alles gelijk aan elke andere ingelogde gebruiker.
4. Alles tussen frontend en backend gaat via HTTP-requests die je kan zien in de Network-tab — als je twijfelt, kijk daar eerst.

Dat is de hele flow.
