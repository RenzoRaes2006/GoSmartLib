# Fix: Verkeerde naam bij reviews

## Het probleem

Bij het bekijken van reviews werd een verkeerde naam getoond ("Team01 ITSOF" of een naam van een andere leerling) in plaats van de echte naam van de persoon die de review schreef.

In de leerkrachtenomgeving (leeslijst toewijzen) werkten de namen wél correct.

---

## Hoe namen werken in dit systeem

Namen worden **nooit opgeslagen in de database** (privacy). In plaats daarvan worden ze live opgevraagd bij **OneRoster** — een externe Smartschool-API die leerling- en leerkrachtgegevens beheert.

Er zijn twee plekken in de code die namen ophalen:

| Context | Klasse | Methode |
|---|---|---|
| Leerkracht ziet leerlingenlijst | `LeerlingController` | `vulNamenIn()` → `OneRosterClient.haalLiveSchoolDataOp()` |
| Reviews, schaderapporten, ... | `OneRosterService` | `getNaamBySourcedId(smartschoolId)` |

---

## Waar de naam vandaan komt bij een review

```
[Leerling schrijft review]
        ↓
BeoordelingService.beoordelingenOpslaan()
  → SecurityUtils.getCurrentUser(userRepository)
    → haalt User op uit DB via smartschoolId (uit JWT-token)
  → review.user = die User

[Leerling bekijkt reviews]
        ↓
BeoordelingService.getBeoordelingenOpBoekId()
  → voor elke review: oneRosterService.getNaamBySourcedId(review.user.smartschoolId)
    → toont die naam boven de review
```

**Sleutelgegeven**: `User.smartschoolId` is de identifier die de brug vormt tussen onze database en OneRoster. Als die identifier verkeerd is, geeft OneRoster een verkeerde naam terug.

---

## Oorzaak 1: Verkeerde smartschoolId bij login

### Twee bronnen voor User-records

Leerlingen komen op twee manieren in de database terecht:

**Bron A — OneRoster bulk-import** (`OneRosterImportService` → `OneRosterLiveClient`):
```
GET /ims/oneroster/v1p1/students
→ voor elke student: sla sourcedId op als smartschoolId in DB
```

**Bron B — OAuth2-login** (`OAuth2LoginSuccessHandler`):
```
GET /users?filter=username='sof2.haitam.samire'
→ sla die sourcedId op als smartschoolId in DB
```

**Het probleem**: het `/users`-endpoint gaf soms een andere sourcedId terug dan het `/students`-endpoint voor dezelfde persoon. Bron B kon daardoor een ander (fout) User-record aanmaken dan het record dat Bron A al had aangemaakt.

### De fix

In `OAuth2LoginSuccessHandler` roepen we nu **eerst** `/students` en `/teachers` aan (dezelfde bron als de import), en pas als fallback `/users`:

```java
// Probeer eerst de getypeerde endpoints (/students, /teachers)
OneRosterUser orUser = oneRosterService.getUserByUsernameViaGetypeerdeEndpoint(username);
if (orUser == null) {
    orUser = oneRosterService.getUserByUsername(username); // fallback
}
```

**Nieuwe methode in `OneRosterService`**: `getUserByUsernameViaGetypeerdeEndpoint(username)` bevraagt `/students?filter=username='...'` en daarna `/teachers?filter=username='...'`. De `username`-filter werkt wél in Smartschool's OneRoster.

**Self-healing**: bij herlogin worden oude records met een verkeerde smartschoolId automatisch gecorrigeerd:

```java
User user = userRepository.findBySmartschoolId(smartschoolIdFinal)
    .or(() -> userRepository.findBySmartschoolId(fallbackId))  // zoek ook op oude foutieve ID
    .map(existing -> {
        existing.setSmartschoolId(smartschoolIdFinal);  // corrigeer naar juiste ID
        ...
    })
    ...
```

---

## Oorzaak 2: Kapot filter in OneRoster

### Het probleem

`getNaamBySourcedId(sourcedId)` deed een API-call naar:
```
GET /students?filter=sourcedId='5905_8774_0'
```

**Smartschool's OneRoster ondersteunt het `sourcedId`-filter niet correct**: in plaats van de specifieke student te filteren, geeft het altijd de eerste student in de lijst terug — in dit geval "Wout Oris".

Dit was zichtbaar via debug-logging:
```
/students?filter=sourcedId='5905_8774_0' → geeft Wout Oris terug (fout!)
/students?filter=username='sof2.haitam.samire' → geeft 1 correct resultaat terug (werkt!)
```

### De fix

In plaats van één API-call per sourcedId met een kapot filter, laadt `getNaamBySourcedId` nu **alle leerlingen en leerkrachten** één keer op (met paginering), bouwt een `sourcedId → naam` map en cachet die 30 minuten:

```java
// In OneRosterService
private synchronized Map<String, String> haalAlleNamenOp() {
    // Controleer cache (30 min TTL)
    if (alleNamenCache != null && cache is nog geldig) {
        return alleNamenCache;
    }
    // Laad alle pagina's van /students en /teachers
    Map<String, String> resultaat = new HashMap<>();
    laadNamenViaEndpoint("/students", resultaat);  // haalt bv. 150 leerlingen op
    laadNamenViaEndpoint("/teachers", resultaat);
    alleNamenCache = resultaat;
    return resultaat;
}

// Paginering in laadNamenViaEndpoint:
int offset = 0;
while (true) {
    GET /students?limit=100&offset=offset
    → voeg alle (sourcedId, naam) paren toe aan de map
    if (minder dan 100 resultaten) break;
    offset += 100;
}
```

Daarna is `getNaamBySourcedId(sourcedId)` gewoon:
```java
return haalAlleNamenOp().get(sourcedId);  // snelle map-lookup, geen API-call
```

---

## Samenvatting van gewijzigde bestanden

| Bestand | Wat gewijzigd |
|---|---|
| `OneRosterService.java` | `getNaamBySourcedId` gebruikt nu een gepagineerde map-cache i.p.v. een kapotte filter; nieuwe methode `getUserByUsernameViaGetypeerdeEndpoint` bevraagt `/students`+`/teachers` |
| `OAuth2LoginSuccessHandler.java` | Login bevraagt nu eerst `/students`+`/teachers` voor de sourcedId; self-healing corrigeert oude verkeerde records bij herlogin |

---

## Visueel overzicht van de volledige flow (na fix)

```
Leerling logt in via Smartschool (OAuth2)
        ↓
OAuth2LoginSuccessHandler
  1. Haal username op uit OAuth2-response (bv. "sof2.haitam.samire")
  2. Zoek sourcedId op via /students?filter=username='...'  ← nieuw
     (fallback: /teachers, dan /users)
  3. Zoek User in DB op sourcedId
     → niet gevonden? Zoek op fallback Smartschool userID  ← nieuw (self-healing)
     → gevonden via fallback: update smartschoolId naar correcte sourcedId  ← nieuw
     → nog niet gevonden: maak nieuw User-record aan
  4. Genereer JWT-token met smartschoolId erin

Leerling schrijft review
        ↓
JWT-token → SecurityUtils.getCurrentSmartschoolId() → findBySmartschoolId()
→ User-record opgehaald → review.user = dit record

Leerling (of iemand anders) bekijkt reviews
        ↓
BeoordelingService.getBeoordelingenOpBoekId()
  → voor elke review: oneRosterService.getNaamBySourcedId(review.user.smartschoolId)
        ↓
OneRosterService.haalAlleNamenOp()  ← nieuw
  → Cache geldig? Return map
  → Cache verlopen: laad alle /students + /teachers (gepagineerd)
  → return map.get(sourcedId)  → "Haitam Samire"  ✓
```
