# Planning: OneRoster Integratie

## Doel
Na het inloggen via Smartschool weten we wie de gebruiker is (naam, smartschoolId),
maar we weten NIET in welke klas hij zit of wat zijn rol is (leerling / leerkracht).
OneRoster is een externe API van de school die die info wel heeft.
We gaan die koppeling bouwen zodat de JWT-token klas en rol bevat.

---

## Overzicht van de nieuwe flow (na jouw werk)

```
1. Gebruiker klikt "Inloggen"
         ↓
2. Browser gaat naar Smartschool (OAuth2 login)
         ↓
3. Smartschool stuurt gebruiker terug naar backend met een tijdelijke code
         ↓
4. Spring ruilt die code in -> krijgt: smartschoolId, username, naam
         ↓
5. [NIEUW] Backend roept OneRoster aan met de username
          -> OneRoster geeft terug: rol (student/teacher) + sourcedId
         ↓
6. [NIEUW] Backend vraagt klas op via sourcedId
          -> OneRoster geeft terug: klasnaam bv. "2SOF"
         ↓
7. JWT-token aanmaken MET klas en rol erin
         ↓
8. Frontend ontvangt token, slaat op in localStorage
         ↓
9. Elke API-call stuurt de token mee -> backend weet wie je bent
```

---

## Stap 1 — OneRosterService.java aanmaken

**Locatie:** `backend/src/main/java/ap/be/backend/service/OneRosterService.java`

**Wat doet dit bestand:**
Een nieuwe service die twee dingen kan:
1. Gebruiker opzoeken op basis van username → geeft terug: rol + sourcedId
2. Klas opzoeken op basis van sourcedId → geeft terug: klasnaam

**Hoe werkt OneRoster:**
OneRoster is een REST API. Je stuurt een HTTP-request met een `Authorization` header
(Basic Auth: username:password in base64). Je krijgt JSON terug.

**Endpoint 1 – Gebruiker ophalen:**
```
GET https://jouw-oneroster-url/users?filter=username='sof2.haitam.samire'
Authorization: Basic <base64(username:password)>
```
Geeft terug:
```json
{
  "users": [
    {
      "sourcedId": "abc123",
      "role": "student",
      "username": "sof2.haitam.samire"
    }
  ]
}
```

**Endpoint 2 – Klas ophalen:**
```
GET https://jouw-oneroster-url/users/abc123/classes
Authorization: Basic <base64(username:password)>
```
Geeft terug:
```json
{
  "classes": [
    {
      "title": "2SOF"
    }
  ]
}
```

**Wat je schrijft in de service:**
```java
@Service
public class OneRosterService {

    // HTTP client om requests te versturen
    private final RestTemplate restTemplate;

    // Haalt gebruikersinfo op (rol + sourcedId) via username
    public OneRosterUser getUserByUsername(String username) { ... }

    // Haalt de klas op via sourcedId
    public String getKlasBySourcedId(String sourcedId) { ... }
}
```

---

## Stap 2 — OAuth2LoginSuccessHandler.java uitbreiden

**Locatie:** `backend/src/main/java/ap/be/backend/config/OAuth2LoginSuccessHandler.java`

**Wat verandert:**
Na het ontvangen van de Smartschool data roep je OneRosterService aan.

```java
// HUIDIGE code (vereenvoudigd):
String smartschoolId = oAuth2User.getAttribute("userID");
String jwtToken = jwtService.generateToken(smartschoolId, rol, naam);

// NIEUWE code:
String username = oAuth2User.getAttribute("username"); // username uit Smartschool
OneRosterUser orUser = oneRosterService.getUserByUsername(username);
String klas = oneRosterService.getKlasBySourcedId(orUser.getSourcedId());
String jwtToken = jwtService.generateToken(smartschoolId, orUser.getRol(), naam, klas);
```

---

## Stap 3 — JwtService.java uitbreiden

**Locatie:** `backend/src/main/java/ap/be/backend/config/JwtService.java`

**Wat verandert:**
`klas` toevoegen als extra claim in de token.

```java
// HUIDIGE handtekening:
public String generateToken(String smartschoolId, String rol, String naam)

// NIEUWE handtekening:
public String generateToken(String smartschoolId, String rol, String naam, String klas)

// Extra regel in de builder:
.claim("klas", klas)
```

---

## Stap 4 — application.properties uitbreiden

**Locatie:** `backend/src/main/resources/application.properties`

Voeg toe:
```properties
# OneRoster API configuratie
oneroster.base-url=https://jouw-oneroster-url/ims/oneroster/v1p1
oneroster.username=jouw-api-gebruikersnaam
oneroster.password=jouw-api-wachtwoord
```

---

## Stap 5 — AuthService.ts uitbreiden (frontend)

**Locatie:** `frontend/src/app/services/auth.service.ts`

**Wat verandert:**
De frontend leest nu ook `klas` uit de JWT-token.

```typescript
// HUIDIGE code leest: smartschoolId, rol, naam
// NIEUWE code leest ook: klas

getKlas(): string {
  const token = localStorage.getItem('jwtToken');
  if (!token) return '';
  const payload = JSON.parse(atob(token.split('.')[1]));
  return payload.klas ?? '';
}
```

---

## Checklist (werk je af van boven naar onder)

- [ ] Stap 1: `OneRosterService.java` aanmaken
- [ ] Stap 2: `OAuth2LoginSuccessHandler.java` aanpassen
- [ ] Stap 3: `JwtService.java` aanpassen (klas toevoegen)
- [ ] Stap 4: `application.properties` aanpassen (OneRoster credentials)
- [ ] Stap 5: `AuthService.ts` aanpassen (klas uitlezen)
- [ ] Testen: inloggen en kijken of de token klas + rol bevat
  - Gebruik jwt.io om de token te decoderen en te controleren

---

## Nuttige info

- **JWT decoderen (om te testen):** Ga naar https://jwt.io, plak je token, en je ziet de inhoud
- **OneRoster docs:** https://www.imsglobal.org/oneroster-v11-final-specification
- **Basic Auth uitleggen:** username:password -> in base64 zetten -> meesturen als header
  Voorbeeld: `Authorization: Basic c2Nob29sOnBhc3M=`
