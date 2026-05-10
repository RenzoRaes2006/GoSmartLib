# GoSmartLib

GoSmartLib is een digitaal bibliotheekbeheersysteem voor scholen, gebouwd voor AP Hogeschool Antwerpen. Leerlingen kunnen boeken opzoeken, reserveren en hun leengeschiedenis raadplegen. Leerkrachten beheren klasleeslijsten en volgen het leengedrag van hun klas op. Bibliotheekbeheerders verwerken uitleningen, retours en rapporteren ongepaste reviews. Authenticatie verloopt via Smartschool OAuth2.

---

## Technische stack

| Laag | Technologie |
|---|---|
| Backend | Java 21, Spring Boot 3.5 |
| Frontend | Angular 20 |
| Database | MySQL 8.0 |
| Auth | Smartschool OAuth2 + JWT |
| Containerisatie | Docker + Docker Compose + Traefik |

---

## Vereisten

- Java 21
- Maven 3.9+
- Node.js 20+ en npm
- MySQL 8.0 (of Docker)
- Een Smartschool-omgeving met OAuth2-client (voor inloggen)
- Een OneRoster API-toegang (voor klas- en gebruikersdata)

---

## Setup — lokale ontwikkeling

### 1. Repository klonen

```bash
git clone https://gitlab.apstudent.be/bachelor-it/software-project/25-26/team-09/gosmartlib.git
cd gosmartlib
```

### 2. Database aanmaken

Zorg dat MySQL draait en maak een database aan:

```sql
CREATE DATABASE schooldb;
```

Of start via Docker (enkel de database):

```bash
docker run --name gosmartlib-mysql -e MYSQL_ROOT_PASSWORD=root -e MYSQL_DATABASE=schooldb -p 3306:3306 -d mysql:8.0
```

### 3. Backend configureren

Bewerk `backend/src/main/resources/application.properties` en vul de volgende variabelen in:

```properties
# Database
spring.datasource.url=jdbc:mysql://localhost:3306/schooldb?createDatabaseIfNotExist=true&serverTimezone=UTC
spring.datasource.username=root
spring.datasource.password=root

# JWT (gebruik een lange, willekeurige string in productie)
app.jwt.secret=JOUW_GEHEIME_SLEUTEL_MIN_32_TEKENS

# Frontend URL (voor redirects na login)
app.frontend.url=http://localhost:4200/

# CORS
app.cors.allowed-origins=http://localhost:4200

# Smartschool OAuth2 (zie sectie hieronder)
spring.security.oauth2.client.registration.smartschool.client-id=JOUW_CLIENT_ID
spring.security.oauth2.client.registration.smartschool.client-secret=JOUW_CLIENT_SECRET

# OneRoster API (klas- en gebruikersdata, opvragen bij Smartschool-beheerder)
oneroster.base-url=https://JOUWSCHOOL.smartschool.be/ims/oneroster/v1p1
oneroster.token-url=https://JOUWSCHOOL.smartschool.be/ims/oneroster/token
oneroster.username=ONEROSTER_USERNAME
oneroster.password=ONEROSTER_PASSWORD
```

### 4. Backend starten

```bash
cd backend
mvn spring-boot:run
```

De backend draait op `http://localhost:8081`.

### 5. Frontend configureren

In `frontend/src/environments/` kan je de API-URL instellen. Standaard wijst de frontend naar `http://localhost:8081`.

### 6. Frontend starten

```bash
cd frontend
npm install
ng serve
```

De frontend is bereikbaar op `http://localhost:4200`.

---

## Setup — Docker (volledig)

Start backend, frontend en database in één keer:

```bash
docker compose up --build
```

> Let op: de Docker-setup gebruikt Traefik als reverse proxy en is bedoeld voor productie op `frontend-sp09.cloud-ap.be`. Voor lokale ontwikkeling is de losse setup (stappen 3–6) eenvoudiger.

---

## Smartschool OAuth2 configureren

1. Log in op het Smartschool-beheerderspaneel van jouw school.
2. Ga naar **Instellingen → API / OAuth2** en maak een nieuwe OAuth2-client aan.
3. Vul als redirect URI in:
   - Lokaal: `http://localhost:8081/login/oauth2/code/smartschool`
   - Productie: `https://api-sp09.cloud-ap.be/login/oauth2/code/smartschool`
4. Kopieer de `client-id` en `client-secret` naar `application.properties`.
5. Stel de scopes in op `userinfo`.

De gebruikersinformatie (smartschoolId, rol, klas) wordt opgehaald via de OneRoster API. Vraag de `username` en `password` hiervoor op bij de Smartschool-beheerder van de school.

---

## Tests uitvoeren

### Backend

```bash
cd backend
mvn test
```

Gebruikt H2 in-memory database — geen MySQL vereist voor tests.

### Frontend

```bash
cd frontend
ng test --watch=false --browsers=ChromeHeadless
```

---

## Rollen

| Rol | Toegang |
|---|---|
| `LEERLING` | Catalogus, leeslijst, reviews, eigen leengeschiedenis |
| `LEERKRACHT` | Klasleeslijsten beheren, leengedrag klas opvolgen |
| `BEHEERDER` | Boekbeheer, uitleningen verwerken, reviews modereren, statistieken |

---

## Projectstructuur

```
gosmartlib/
├── backend/          # Spring Boot applicatie
│   └── src/
│       ├── main/java/ap/be/backend/
│       │   ├── controller/   # REST endpoints
│       │   ├── service/      # Business logica
│       │   ├── entity/       # JPA entiteiten
│       │   ├── repository/   # Spring Data repositories
│       │   ├── security/     # JWT + OAuth2 configuratie
│       │   └── seeds/        # Testdata bij opstarten
│       └── test/             # Unit- en integratietests
├── frontend/         # Angular applicatie
│   └── src/app/
│       ├── services/ # HTTP-services naar backend
│       └── ...       # Components per feature
├── docker-compose.yaml
└── traefik/          # Reverse proxy configuratie
```
