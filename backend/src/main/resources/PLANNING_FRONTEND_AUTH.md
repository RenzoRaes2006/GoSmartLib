# Frontend Auth — Wat opgeruimd is en wat morgen nog moet

## Wat er opgeruimd is (al gedaan)

### AuthService.ts
- `currentUserDummy` verwijderd (was een nep test-gebruiker voor dev, niet meer nodig)
- `logout()` methode toegevoegd (verwijdert token uit localStorage en reset currentUser)
- TODO MORGEN: `klas` toevoegen aan het type en aan `setUserFromToken()`

### auth.guard.ts
- Alle logica stond in commentaar -> guard blokkeerde altijd iedereen
- Nu werkend: controleert of gebruiker ingelogd is, anders doorsturen naar /login

### admin-guard.ts
- Alle logica stond in commentaar -> guard blokkeerde altijd iedereen
- Nu werkend: controleert op rol 'beheerder'
- TODO MORGEN: rolnaam aanpassen zodra je weet welke waarden OneRoster teruggeeft

### login-success.ts
- `setUserFromToken()` stond in commentaar
- Nu actief: gebruiker wordt meteen ingesteld na login (anders werkte de app pas correct na een refresh)

---

## Wat morgen nog moet (frontend kant van OneRoster)

### Stap 1 — AuthService.ts: klas toevoegen

```typescript
// Type uitbreiden:
public currentUser = signal<{ id: string; name: string; role: string; klas: string } | null>(null);

// setUserFromToken uitbreiden:
setUserFromToken(token: string) {
  const decoded: any = jwtDecode(token);
  this.currentUser.set({
    id: decoded.sub,
    name: decoded.name,
    role: decoded.role,
    klas: decoded.klas   // <- dit toevoegen
  });
}
```

### Stap 2 — admin-guard.ts: rolnaam controleren

Zodra je weet welke rolnamen OneRoster teruggeeft, de check aanpassen:
```typescript
// Huidige check:
if (user && user.role === 'beheerder') { ... }

// Mogelijke aanpassing (afhankelijk van OneRoster):
if (user && user.role === 'administrator') { ... }
```

### Stap 3 — app.routes.ts: guards activeren op routes

De guards zijn aangemaakt maar nog NERGENS gekoppeld aan routes.
Voeg `canActivate` toe aan de routes die beschermd moeten zijn:

```typescript
// Voorbeeld:
{ path: 'beheerder', component: BeheerderDashboard, canActivate: [adminGuard] },
{ path: 'leeslijsten', component: KlassLeeslijstenOverzichtComponent, canActivate: [authGuard] },
```

Huidige routes in app.routes.ts die waarschijnlijk beschermd moeten worden:
- /beheerder          -> adminGuard
- /leerkracht-dashboard -> authGuard (leerkracht)
- /klassenleeslijsten -> authGuard (leerling)
- /leeslijsten        -> authGuard (leerkracht)
- /beheer/aanvragen   -> adminGuard
- /beheer/scan        -> adminGuard
- /beheer/actieve-leningen -> adminGuard

---

## Hoe de volledige auth flow werkt (overzicht)

```
Login via Smartschool
      ↓
OAuth2LoginSuccessHandler (backend)
      ↓
JWT-token aanmaken (bevat: id, name, role, [morgen: klas])
      ↓
Redirect naar /login-success?token=...
      ↓
LoginSuccessComponent
  -> token opslaan in localStorage
  -> setUserFromToken() aanroepen (currentUser signal instellen)
  -> doorsturen naar /
      ↓
Bij elke API-call: token meesturen via interceptor (Authorization: Bearer ...)
      ↓
Bij elke beschermde route: guard controleert currentUser signal
```
