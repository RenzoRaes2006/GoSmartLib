package ap.be.backend.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/***
 * BESTAND: User.java
 *
 * WAT DOET DIT BESTAND?
 * Dit is de "User" entiteit — een Java-klasse die één rij in de "users" tabel
 * van de database voorstelt. Elke gebruiker die ooit inlogt via Smartschool
 * krijgt hier een record.
 *
 * LET OP: Dit is NIET de "Gebruiker" entiteit (die is voor leerlingen/leerkrachten
 * in het bibliotheeksysteem). "User" is puur voor het bijhouden WIE er ooit
 * ingelogd heeft via Smartschool.
 *
 * HUIDIG DATAMODEL (wat er nu in de tabel staat):
 *   id            -> automatisch gegenereerd getal (1, 2, 3, ...)
 *   smartschoolId -> de unieke ID van Smartschool (bv. "E4LD6Wfpo22xSAjmOUHj2g==")
 *
 * TODO MORGEN: Na de OneRoster-koppeling wil je hier ook opslaan:
 *   rol           -> "student" of "teacher"
 *   klas          -> bv. "2SOF"
 *
 * LOMBOK ANNOTATIES (automatisch gegenereerde code):
 *   @Getter      -> maakt automatisch getters aan (getId(), getSmartschoolId(), ...)
 *   @Setter      -> maakt automatisch setters aan (setSmartschoolId(...), ...)
 *   @AllArgsConstructor -> constructor met ALLE velden als parameters
 *   @NoArgsConstructor  -> lege constructor (vereist door JPA/Hibernate)
 ***/
@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "users")
public class User {

    /***
     * Primaire sleutel in de database.
     * IDENTITY = de database genereert automatisch een oplopend getal (1, 2, 3, ...).
     ***/
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /***
     * De unieke identifier van Smartschool voor deze gebruiker.
     * unique = true  -> twee gebruikers kunnen niet dezelfde smartschoolId hebben
     * nullable = false -> dit veld is verplicht, nooit leeg
     ***/
    @Column(unique = true, nullable = false)
    private String smartschoolId;

    /***
     * Handige constructor om snel een nieuwe User aan te maken met enkel de smartschoolId.
     * Gebruikt in OAuth2LoginSuccessHandler.java:
     *   userRepository.save(new User(smartschoolId))
     ***/
    public User(String smartschoolId) {
        this.smartschoolId = smartschoolId;
    }
}
