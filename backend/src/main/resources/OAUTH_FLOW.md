# Documentatie: Smartschool OAuth2 & JWT Flow (gosmartbib)

Dit document beschrijft de volledige authenticatie-flow tussen de Angular frontend, de Spring Boot backend en de Smartschool OAuth2 provider.

## 1. Backend Configuratie (application.properties)

De backend fungeert als de "OAuth Client". De volgende eigenschappen zijn essentieel voor de communicatie met Smartschool:

* client-id: De publieke ID van onze app bij Smartschool.
* client-secret: Het geheime wachtwoord (nooit delen!).
* client-authentication-method: Gebruik `client_secret_post` voor veilige verzending van credentials in de body.
* authorization-grant-type: `authorization_code` (standaard veilige flow, wisselt code in voor token).
* redirect-uri: Het backend-punt waar Smartschool de code naartoe stuurt (`{baseUrl}/login/oauth2/code/smartschool`).
* authorization-uri: De externe inlogpagina van Smartschool.
* token-uri: API om de code in te wisselen voor een Access Token.
* user-info-uri: API om gebruikersgegevens op te halen.
* user-name-attribute: We gebruiken `id` als unieke identificatie van de gebruiker.

---

## 2. De 8-Staps Flow

De flow is ontworpen om de veiligheid te maximaliseren door de tokens voornamelijk in de backend te verwerken.

1. De start (Angular)
De gebruiker klikt op de login-knop. Angular verwijst de browser door naar de backend (bijvoorbeeld `https://api-sp09.cloud-ap.be/oauth2/authorization/smartschool`).

2. De doorverwijzing (Spring Boot)
De backend ontvangt dit verzoek en stuurt de browser direct door naar de inlogpagina van Smartschool. De backend geeft hierbij de Client ID mee.

3. De autorisatie (Smartschool)
De gebruiker vult zijn inloggegevens in op de beveiligde pagina van Smartschool.

4. De code (Smartschool -> Spring Boot)
Na succesvol inloggen stuurt Smartschool de browser terug naar de backend redirect URI met een tijdelijke autorisatiecode.

5. De inwisseling (Spring Boot)
De backend vangt de code op en stuurt deze op de achtergrond, samen met de Client ID en Client Secret, naar Smartschool om in te wisselen voor een Access Token.

6. De gegevens (Spring Boot)
De backend gebruikt de ontvangen Access Token om de gegevens van de gebruiker veilig op te vragen bij Smartschool.

7. De interne token (Spring Boot -> Angular)
* De `AuthenticationSuccessHandler` wordt geactiveerd.
* De Smartschool ID wordt gecontroleerd in de database (en opgeslagen indien nieuw).
* De `JwtService` genereert een eigen gosmartbib-token (JWT).
* De backend stuurt de browser terug naar de Angular frontend, met de JWT in de URL (`https://frontend-sp09.cloud-ap.be/login-success?token=JOUW_JWT`).

8. De afronding (Angular)
De `LoginSuccessComponent` leest de JWT uit de adresbalk, slaat deze op in `localStorage` en stuurt de gebruiker naar de homepage (en verwijdert zo de token uit de zichtbare URL).

---

## 3. Waarom een eigen JWT?

Hoewel Smartschool de identiteit bevestigt, gebruiken we een eigen JWT voor interne beveiliging:
* Onafhankelijkheid: We hoeven niet bij elk verzoek aan Smartschool te vragen of de gebruiker echt is.
* Veiligheid & Snelheid: Onze backend herkent zijn eigen pasje (de JWT) en weet direct of iemand de juiste rechten heeft.

## 4. Belangrijke Componenten (Backend)

* SecurityConfig.java: Beheert de toegang tot URL's en activeert de `.oauth2Login()` automatische flow.
* OAuth2LoginSuccessHandler.java: De "brug" die na succesvol inloggen de database controleert, de JWT maakt en de redirect naar Angular doet.
* JwtService.java: Bevat de logica voor het genereren van de tokens.
* UserRepository.java: Slaat enkel de unieke Smartschool ID op (GDPR-proof, geen extra gegevens).

---

## 5. Volgende stap (To-Do)

Om in de toekomst beveiligde data uit de backend op te halen, moet Angular de opgeslagen token meesturen bij elk request. Hiervoor moet later nog een Angular `HttpInterceptor` geschreven worden.

## 6. API Beveiliging: De JWT Validatie (De Portier)

Zodra de gebruiker is ingelogd en de Angular frontend de JWT meestuurt bij elk API-verzoek, moet de backend deze token controleren voordat er toegang wordt gegeven tot beveiligde data (zoals boeken of reviews). Dit gebeurt via twee belangrijke componenten:

### A. JwtService (Tokens Lezen & Valideren)
Naast het *maken* van tokens bij het inloggen, bevat deze klasse nu ook methoden om inkomende tokens uit te pakken en te controleren.

* `isTokenValid(String token)`: **De Zegelcontrole.** Deze methode controleert de wiskundige handtekening van de token met onze geheime sleutel. Het bevestigt dat de token écht door onze backend is gemaakt, niet is vervalst door een hacker, en nog niet is verlopen.
* `extractSmartschoolId(String token)`: **De Envelop Openen.** Zodra we weten dat de token geldig is, leest deze methode het `Subject` (het onderwerp) uit. Omdat we bij het maken van de token de Smartschool ID als subject hebben ingesteld, krijgen we hier exact de ID van de aanvrager terug.

### B. JwtAuthenticationFilter (De Portier)
Dit is een custom Spring Security Filter dat vóór elke API-call wordt uitgevoerd. Het onderschept inkomende HTTP-verzoeken (zoals een POST naar `/reviews/report`) en fungeert als de portier van de applicatie.

**De flow in het filter:**
1.  **Header Zoeken:** Het filter kijkt of het inkomende verzoek een `Authorization` header heeft.
2.  **Token Isoleren:** Als de header begint met `Bearer `, wordt de eigenlijke JWT tekst eruit geknipt.
3.  **Validatie:** Het filter roept `jwtService.isTokenValid()` aan.
4.  **Identificatie:** Als de token klopt, roept het `jwtService.extractSmartschoolId()` aan om te zien *wie* dit verzoek doet.
5.  **Beveiligingscontext Updaten:** Het filter plaatst de Smartschool ID in de `SecurityContextHolder`. Hiermee wordt Spring Security officieel verteld: *"Dit verzoek is veilig en wordt uitgevoerd door gebruiker X"*. Vanaf dat moment mag de code door naar de betreffende Controller.


## 7. Gebruikersrollen & Frontend Status (RBAC & Signals)

Om functionaliteiten af te schermen op basis van de rol van de gebruiker (Role-Based Access Control), slaan we de rol NIET op in de database (i.v.m. privacy-eisen), maar verpakken we deze in de JWT.

**A. Backend: Claims toevoegen**
In de `OAuth2LoginSuccessHandler` lezen we extra gegevens uit het Smartschool-profiel en sturen we deze mee naar de `JwtService`:
1. Smartschool ID (verplicht)
2. Rol (bijv. leerling, leerkracht, beheerder)
3. Naam van de ingelogde gebruiker

In `JwtService.java` worden deze toegevoegd als extra "claims" (datavelden) in de token:
`.claim("role", rol)`
`.claim("name", naam)`

**B. Frontend: Token Decoderen & Signals**
In Angular gebruiken we de library `jwt-decode` om de Base64-gecodeerde token uit te pakken tot leesbare JSON. We doen dit centraal in een `AuthService`.
We gebruiken Angular **Signals** om deze data globaal en reactief beschikbaar te maken voor de hele applicatie:

```typescript
// Voorbeeld AuthService state
public currentUser = signal<{ id: string, name: string, role: string } | null>(null);

## 8. Route Beveiliging (Auth Guards)

Om te voorkomen dat onbevoegden via de URL afgeschermde pagina's openen, gebruiken we Angular Auth Guards. 

* Werking: Een Guard controleert een voorwaarde vóór het laden van de route.
* Implementatie: De `authGuard` checkt of `authService.currentUser()` is ingevuld.
* Toepassing: In `app.routes.ts` voegen we `canActivate: [authGuard]` toe aan beveiligde routes. Publieke pagina's krijgen deze beveiliging niet.

---

## 9. Architectuurkeuzes (Voor verdediging)

Tijdens de ontwikkeling zijn deze bewuste keuzes gemaakt:

A. Geen Refresh Tokens
De JWT is 24 uur geldig. Daarna logt de gebruiker opnieuw in. Een complexe automatische refresh-flow is bewust buiten scope gelaten; de focus ligt op de kernfunctionaliteit van de bibliotheek.

B. Privacy (Data van anderen)
We slaan enkel anonieme Smartschool ID's op in de database, nooit namen. 
* Eigen naam: Zit in de JWT voor snelle weergave.
* Namen van anderen (bijv. bij reviews): De Spring Boot backend haalt deze *live* op bij Smartschool via een achtergrond-API-call en stuurt ze gecombineerd met de review door naar Angular.