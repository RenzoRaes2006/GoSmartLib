# Smartschool Authenticatie — hoe het werkt

Dit document beschrijft hoe een Smartschool-gebruiker (leerling, leerkracht, bibliotheekbeheerder) inlogt en uiteindelijk in de app verschijnt met de juiste rol en klas. Bedoeld als naslagwerk wanneer je het overzicht kwijt bent.

Lees eerst `ADMIN_AUTH_FLOW.md` als je dat nog niet gedaan hebt — vanaf het moment dat het JWT bestaat is alles identiek aan de admin-flow.

---

## 1. Het kernidee in één alinea

De gebruiker klikt op "Aanmelden met Smartschool". Vanaf dat moment wordt hij door drie partijen heen geslagen: zijn eigen browser, Smartschool (waar hij zijn echte wachtwoord intypt) en jouw backend. Aan het einde van die reis heeft jouw backend bewijs dat Smartschool zegt: "ja, dit is gebruiker X". Op basis daarvan maakt je backend een gewoon JWT-token (zelfde soort als bij admin-login) en plakt dat aan de URL waar hij de browser naartoe stuurt. De frontend leest het token uit de URL en is klaar.

---

## 2. Twee externe API's, niet één

Belangrijk om uit elkaar te houden:

| API | Wat doet het | Hoe wordt het aangesproken |
|---|---|---|
| **Smartschool OAuth2** | Zegt wie de gebruiker is (`userID`, `name`, `username`) | Browser-redirect (de gebruiker logt zelf in bij Smartschool) |
| **OneRoster** | Zegt welke rol en welke klas die gebruiker heeft | Server-to-server REST (jouw backend belt zelf) |

Smartschool weet **niet** of iemand leerling of leerkracht is. Daarom moet je daarna OneRoster bellen. Twee aparte tokens, twee aparte API's.

---

## 3. De spelers

### Backend
- `application.properties` — registreert de Smartschool OAuth2-client (`client-id`, `client-secret`, `redirect-uri`, scope `userinfo`) en de OneRoster credentials.
- `SecurityConfig.java` — zegt: in prod-profiel, gebruik OAuth2-login met `OAuth2LoginSuccessHandler` als succes-handler.
- `OAuth2LoginSuccessHandler.java` — jouw eigen code die **na** de Smartschool-login draait.
- `OneRosterService.java` — belt OneRoster om rol en klas op te halen.
- `UserRepository` / `User`-entiteit — bewaart alleen `smartschoolId`, `klas`, `rol`. Géén naam, géén e-mail.
- `JwtService.java` — zelfde service als bij admin-login. `generateToken(...)` maakt het 24u-JWT.
- `JwtAuthenticationFilter.java` — leest bij volgende requests het JWT en zet `currentUser` in de SecurityContext.

### Frontend
- `login.ts` + `login.html` — pagina met de knop "Aanmelden met Smartschool". Eén regel werk: `window.location.href = ".../oauth2/authorization/smartschool"`.
- `login-success.ts` — pagina waar de gebruiker landt na de hele OAuth-dans. Pakt `?token=...` uit de URL, zet 'm in `localStorage`, navigeert op basis van rol.
- `AuthService.ts` — decodeert het token, vult de `currentUser` signal.
- `interceptor.ts` — plakt `Authorization: Bearer ...` op alle volgende calls (zelfde mechanisme als bij admin).
- `authGuard`, `teacherGuard`, `beheerderGuard` — bewaken routes op basis van `currentUser.role`.

---

## 4. De volledige flow, stap voor stap

```
Browser              Backend                Smartschool         OneRoster
  |                    |                       |                   |
  | Klik 'Aanmelden    |                       |                   |
  | met Smartschool'   |                       |                   |
  | GET /oauth2/authorization/smartschool ---> |                   |
  |                                            |                   |
  |   302 redirect naar Smartschool                                |
  | <----------------------------------------- |                   |
  |                                                                |
  | GET oauth.smartschool.be/.../authorize?client_id=...&redirect_uri=...
  | ------------------------------------> Smartschool              |
  |                                            |                   |
  | (gebruiker logt in bij Smartschool zelf)   |                   |
  |                                            |                   |
  |   302 redirect terug naar onze backend:                        |
  |   /login/oauth2/code/smartschool?code=ABC                      |
  | <----------------------------------------- |                   |
  |                                            |                   |
  | GET /login/oauth2/code/smartschool?code=ABC                    |
  | -----------------------------> Backend (Spring Security pakt op)
  |                       |                                        |
  |                       | POST oauth.smartschool.be/.../token    |
  |                       | (code ruilen voor access_token)        |
  |                       | --------------------> Smartschool      |
  |                       |    { access_token: ... }               |
  |                       | <-------------------- Smartschool      |
  |                       |                                        |
  |                       | GET oauth.smartschool.be/.../userinfo  |
  |                       | (met Bearer access_token)              |
  |                       | --------------------> Smartschool      |
  |                       |    { userID, name, username }          |
  |                       | <-------------------- Smartschool      |
  |                       |                                        |
  |             Spring roept nu OAuth2LoginSuccessHandler aan.     |
  |                       |                                        |
  |                       | OneRosterService.getUserByUsername     |
  |                       | -----------------------------------> OneRoster
  |                       |   { sourcedId, role: "student" }       |
  |                       | <----------------------------------- OneRoster
  |                       |                                        |
  |                       | OneRosterService.getKlasBySourcedId    |
  |                       | (3 calls: /students, /enrollments, /classes)
  |                       | -----------------------------------> OneRoster
  |                       |   "2SOF"                               |
  |                       | <----------------------------------- OneRoster
  |                       |                                        |
  |                       | userRepository.save(user) — upsert     |
  |                       |   smartschoolId, klas, rol             |
  |                       |                                        |
  |                       | jwtService.generateToken(...)          |
  |                       |   → "eyJ..."                           |
  |                       |                                        |
  |   302 redirect naar:                                           |
  |   {frontendBaseUrl}/login-success?token=eyJ...                 |
  | <----------------------------------------- |                   |
  |                                                                |
  | GET /login-success?token=eyJ...                                |
  | (Angular component LoginSuccess komt op)                       |
  |                                                                |
  | localStorage.setItem('jwtToken', token)                        |
  | authService.setUserFromToken(token)                            |
  | router.navigate(['/leerkracht-dashboard' | '/beheerder' | '/'])|
  |                                                                |
  | Vanaf hier: interceptor plakt Bearer op elke call,             |
  | JwtAuthenticationFilter herkent gebruiker op de backend.       |
```

---

## 5. Wat draait Spring Security zelf, en wat is jouw code?

Dit is het stuk dat veel mensen verwart. De OAuth2-dans (de zes stappen in het diagram tussen "klik op knop" en "OAuth2LoginSuccessHandler aanroepen") is **volledig** geconfigureerd via `application.properties` + `SecurityConfig.oauth2Login(...)`. Jij hebt daar geen regel code voor geschreven. Spring weet:

- "Als iemand `/oauth2/authorization/smartschool` belt, redirect hem naar `authorization-uri`."
- "Als hij terugkomt op `/login/oauth2/code/smartschool` met een code, wissel die in op `token-uri`."
- "Haal user-info op via `user-info-uri`."
- "Bel daarna mijn `successHandler`."

Jouw code begint pas bij `OAuth2LoginSuccessHandler.onAuthenticationSuccess`. Dat is precies de plek waar jij beslist: wat doe ik met die gebruiker?

---

## 6. Wat `OAuth2LoginSuccessHandler` precies doet

Vier stappen, allemaal even belangrijk:

1. **Smartschool-data uitpakken** — `userID`, `name`, `username` uit het OAuth2-antwoord halen. `userID` is de unieke Smartschool-ID die we als `sub` in het JWT stoppen.

2. **OneRoster bellen** — `getUserByUsername(username)` om rol + sourcedId op te halen, dan `getKlasBySourcedId(...)` voor de klas. Als OneRoster faalt of de gebruiker niet kent, blijven rol en klas `"onbekend"`. **De login mislukt niet** — de gebruiker krijgt een token met rol `"onbekend"` en komt op de homepage terecht, niet op een dashboard.

3. **User upsert** — `userRepository.findBySmartschoolId(...)`. Bestaat al → klas en rol updaten. Nieuw → aanmaken. Géén naam, géén e-mail. We bewaren bewust het minimum.

4. **JWT bouwen + redirect** — `jwtService.generateToken(smartschoolId, rol, naam, klas)` en dan `response.sendRedirect(frontendBaseUrl + "/login-success?token=" + jwtToken)`. Het token zit in de URL omdat een redirect geen body kan meegeven. De browser landt op `/login-success` op de frontend, waar het component het token uit de URL plukt.

---

## 7. Wat de frontend doet bij `/login-success`

Drie regels die ertoe doen:

```ts
localStorage.setItem('jwtToken', token);          // bewaren over refresh heen
this.authService.setUserFromToken(token);         // currentUser signal vullen
// router.navigate(...) op basis van role
```

Vanaf nu is er **geen onderscheid** meer tussen "ingelogd via Smartschool" en "ingelogd via admin-login":
- Zelfde sleutel in `localStorage` (`jwtToken`).
- Zelfde interceptor plakt `Bearer ...` op alle calls.
- Zelfde `JwtAuthenticationFilter` op de backend leest het token.

Het enige verschil zit in de `role`-claim. Daarop beslissen de guards welke routes je mag zien.

---

## 8. Vergelijking met de admin-flow

| | Smartschool | Admin |
|---|---|---|
| Inlogscherm | `/login` | `/admin-login` |
| Wachtwoord ingetypt waar? | Bij Smartschool zelf | In jouw eigen formulier |
| Hoeveel HTTP-calls van de browser? | Eén (redirect-keten) | Eén (twee bij eerste login) |
| Hoeveel calls backend → buiten? | 2 naar Smartschool + 1–4 naar OneRoster | 0 |
| Hoe komt JWT bij de browser? | Querystring in redirect URL | JSON-body van POST-response |
| Frontend-eindpunt | `/login-success` | `/admin-login` blijft op zichzelf |
| Resulterend token | Zelfde `jwtService.generateToken` | Zelfde |
| Resulterende `localStorage`-key | `jwtToken` | `jwtToken` |

Beide flows leiden tot een identieke "ingelogde staat". Dat is bewust.

---

## 9. Waar je makkelijk de controle verliest

### Plek 1: de `redirect-uri` moet exact kloppen
Bij Smartschool is geconfigureerd waar je gebruikers naar mogen terugkomen. Staat in `application.properties`:
```
redirect-uri={baseUrl}/login/oauth2/code/smartschool
```
Als je `app.frontend.url` of de hosting wijzigt, moet die URI ook bij Smartschool aangepast worden — anders krijg je een `redirect_uri_mismatch`-fout en stopt de flow daar. Niet jouw code: een config-issue.

### Plek 2: OneRoster faalt stilletjes
Bij elke `catch (Exception e)` in `OneRosterService` wordt de fout naar `System.err` gelogd en valt rol/klas terug op `"onbekend"`. Symptoom in de app: gebruiker logt in, maar wordt naar `/` gestuurd in plaats van zijn dashboard, en niets in de UI verklaart waarom. **Diagnose:** kijk in de backend-console naar `[OneRoster]`-regels.

### Plek 3: het token in de URL
Bij `/login-success?token=...` staat het JWT letterlijk in de URL. Dat betekent:
- Het komt in de browser-history terecht.
- Het komt in server-logs van proxies tussen jou en de browser.
- Een ander tabblad op dezelfde history-stack kan het zien.

Dat is voor de schoolcontext meestal acceptabel, maar weet dat dit zo is. Mitigation: het component verwijdert het token uit de URL nadat het is opgeslagen (doe je nu niet — eventuele verbetering). Want het JWT in `localStorage` is je echte opslagplek; de URL is alleen transport.

### Plek 4: rol-mapping en `null`
`mapRol` zet alleen `"student"` en `"teacher"` om naar een DB-enum. Andere waarden (waaronder `"beheerder"` als OneRoster die ooit teruggeeft) worden `null` in de DB-kolom. In het **JWT** echter zit de rauwe OneRoster-string (`rol`), niet het enum. Dus de frontend kan iemand wel als `"beheerder"` zien, terwijl de DB-rij rol `null` heeft. Niet per se een bug, wel iets om in je hoofd te houden. Hoe die `"beheerder"`-string in OneRoster terechtkomt is een vraag voor de school-IT — buiten dit document.

### Plek 5: dev-modus omzeilt dit alles
In het `dev`-profiel is heel deze OAuth2-flow **uitgeschakeld** door `SecurityConfig`. `DevAuthFilter` zet een vaste fake-gebruiker. Dus als je lokaal test op `/login` en het werkt niet — kijk eerst of je in dev draait. De Smartschool-knop probeert tegen de productie-URL te praten en dat faalt CORS-gewijs.

---

## 10. Hoe je live controleert wat er gebeurt

1. **Network-tab, "preserve log" aanzetten.** Anders verlies je de redirect-keten zodra je naar Smartschool springt.
2. **De vier sleutelregels die je daar wil zien:**
   - `GET /oauth2/authorization/smartschool` → 302 naar `oauth.smartschool.be`
   - Smartschool 302 terug naar `/login/oauth2/code/smartschool?code=...`
   - 302 naar `{frontend}/login-success?token=eyJ...`
   - `GET /login-success?token=...` (200, frontend serveert Angular)
3. **Backend-log filteren op `[OneRoster]`** voor diagnose van rol/klas.
4. **`localStorage.getItem('jwtToken')` in de console** om te checken dat het JWT effectief geland is.
5. **JWT decoderen op [jwt.io](https://jwt.io)** om `sub`, `role`, `name`, `klas` te bekijken.

---

## 11. Mentale samenvatting in vier zinnen

1. De knop "Aanmelden met Smartschool" begint een OAuth2-redirect-keten die Spring Security volledig zelf afhandelt.
2. Op het einde van die keten roept Spring `OAuth2LoginSuccessHandler` aan; daar verrijk jij de gebruiker met OneRoster-data en maak je een JWT.
3. Je stuurt de browser naar `/login-success?token=...`; daar pakt de frontend het token uit de URL en slaat het op in `localStorage`.
4. Vanaf daar is de Smartschool-gebruiker volledig inwisselbaar met een admin-gebruiker — zelfde token-mechanisme, zelfde interceptor, zelfde filter.

Dat is de hele flow.
