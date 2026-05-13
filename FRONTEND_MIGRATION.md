# Frontend Migratie — Gebruiker → User

Backend-refactor: de oude `Gebruiker`-entity is vervangen door `User`. Hieronder de zaken die in de frontend bijgewerkt moeten worden.

---

## 1. Endpoint hernoemd

| Oud | Nieuw |
|-----|-------|
| `GET /api/gebruiker/schoolid/{schoolId}` (numeriek) | `GET /api/gebruiker/by-smartschool/{smartschoolId}` (string) |

De path-param is nu een **string** (smartschoolId, bv. `"E4LD6Wfpo22xSAjmOUHj2g=="` of voor seed-data `"12345"`), niet meer een numerieke schoolId.

---

## 2. `GebruikerDTO` heeft een nieuwe shape

**Oud:**
```ts
{ id, schoolId, voorNaam, achternaam, email, klas, rol }
```

**Nieuw:**
```ts
{ id, smartschoolId, klas, rol }
```

Verdwenen velden: `schoolId`, `voorNaam`, `achternaam`, `email`.

**Gevolg:** elke pagina die voor-/achternaam of email toonde uit deze DTO toont nu `undefined`. Voor de ingelogde gebruiker zelf staat de naam wél in de JWT (`name`-claim) — gebruik die in plaats van de DTO.

Voor andere leerlingen (bv. in admin-overzichten) is naam/email niet meer beschikbaar via dit endpoint. Als dit nodig is moet er een aparte OneRoster-call komen — vraag aan backend.

---

## 3. `UitleningSummaryDTO` heeft een nieuwe shape

**Oud:** veld `gebruikerSchoolId: long`
**Nieuw:** veld `smartschoolId: string`

Filteren/zoeken op nummer werkt niet meer; gebruik de string.

---

## 4. Zoekbalk leerlingen — beperkter

Endpoint `GET /api/leerlingen?zoekterm=...` zocht voorheen in voor- en achternaam. **Nu zoekt hij alleen in `smartschoolId`** (User heeft geen naam-veld in de DB). Pas de UI/copy van het zoekveld hierop aan ("Zoek op smartschool-ID") of verwijder de zoekbalk tot er een aparte naam-source is.

---

## 5. Geen functionele wijziging, maar opletten

- JWT-shape (`sub`, `role`, `name`, `klas`, `iat`, `exp`) is **ongewijzigd**.
- Login-flow (Smartschool OAuth + beheerder-login) is ongewijzigd.
- Beoordelingen/uitleningen/leeslijsten endpoints geven dezelfde JSON terug — alleen genummerde id's en smartschoolId zijn nu de identifiers, geen schoolId meer.

---

## TL;DR voor de frontend-dev

Globale zoekacties uit te voeren in de frontend-codebase:

1. `/api/gebruiker/schoolid` → `/api/gebruiker/by-smartschool`
2. Properties `voorNaam`, `achternaam`, `email`, `schoolId` op `Gebruiker`/`GebruikerDTO`-types — verwijderen of vervangen door JWT-claim (`name`).
3. Types/interfaces `Gebruiker` met de oude velden — vernieuwen naar `{ id, smartschoolId, klas, rol }`.
4. Zoekformulier voor leerlingen — label/placeholder bijwerken.
