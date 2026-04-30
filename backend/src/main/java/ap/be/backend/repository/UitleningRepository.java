package ap.be.backend.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import ap.be.backend.entity.Boek;
import ap.be.backend.entity.Gebruiker;
import ap.be.backend.entity.Uitlening;

@Repository
public interface UitleningRepository extends JpaRepository<Uitlening, Long> {

    // Gebruikt exact de ID variabele zoals Spring Boot die verwacht
    long countByGebruiker_GebruikerID(Long gebruikerId);

    // Get active loans of a user
    List<Uitlening> findByGebruiker_GebruikerIDAndIsIngeleverdFalse(Long gebruikerId);

    // Check if user has overdue loans
    boolean existsByGebruiker_GebruikerIDAndIsIngeleverdFalseAndInleverDatumBefore(Long gebruikerId, LocalDate vandaag);

    // Check if user already has this book
    boolean existsByGebruiker_GebruikerIDAndBoek_IdAndIsIngeleverdFalse(Long gebruikerId, Long boekId);

    // Fetch all requests by status
    List<Uitlening> findByStatus(String status);

    boolean existsByGebruiker_GebruikerIDAndStatus(Long gebruikerId, String status);

    List<Uitlening> findByGebruiker_GebruikerIDOrderByUitleenDatumDesc(Long gebruikerId);

    Optional<Uitlening> findFirstByGebruikerAndBoekAndIsIngeleverdFalseOrderByUitleenDatumAsc(Gebruiker gebruiker, Boek boek);

    List<Uitlening> findByIsIngeleverdFalseOrderByUitleenDatumDesc();

    // Aangepaste query: Omdat we geen inleverdatum kunnen vergelijken in de entity,
    // tellen we hier simpelweg hoeveel boeken deze leerling al heeft teruggebracht.
    @Query("SELECT COUNT(u) FROM Uitlening u WHERE u.gebruiker.gebruikerID = :id AND u.isIngeleverd = true")
    long countOnTime(@Param("id") Long id);
}