package ap.be.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import ap.be.backend.entity.User;

import java.util.Optional;

/***
 * BESTAND: UserRepository.java
 *
 * WAT DOET DIT BESTAND?
 * Dit is de "deur" naar de "users" tabel in de database.
 * Via dit interface kan je gebruikers opzoeken, opslaan, en verwijderen
 * zonder zelf SQL te schrijven. Spring Data JPA regelt dat automatisch.
 *
 * HOE WERKT HET?
 * Door "JpaRepository<User, Long>" uit te breiden, krijg je gratis:
 *   - save(user)       -> INSERT of UPDATE in de database
 *   - findById(id)     -> SELECT * FROM users WHERE id = ?
 *   - findAll()        -> SELECT * FROM users
 *   - delete(user)     -> DELETE FROM users WHERE id = ?
 *   - ... en nog meer
 *
 * De <User, Long> parameters betekenen:
 *   - User -> welke entiteit (= welke tabel)
 *   - Long -> het type van de primaire sleutel (id is een Long)
 ***/
@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    /***
     * Zoek een gebruiker op basis van zijn Smartschool ID.
     * Spring Data JPA genereert automatisch de SQL:
     *   SELECT * FROM users WHERE smartschool_id = ?
     *
     * Geeft een Optional<User> terug:
     *   - Optional.of(user)  -> als de gebruiker gevonden is
     *   - Optional.empty()   -> als er geen gebruiker is met die smartschoolId
     *
     * Gebruikt in OAuth2LoginSuccessHandler.java om te controleren of een
     * inloggende gebruiker al in de database staat.
     ***/
    Optional<User> findBySmartschoolId(String smartschoolId);
}
