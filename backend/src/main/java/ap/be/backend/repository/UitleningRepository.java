package ap.be.backend.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import ap.be.backend.entity.Boek;
import ap.be.backend.entity.Gebruiker;
import ap.be.backend.entity.Uitlening;

@Repository
public interface UitleningRepository extends JpaRepository<Uitlening, Long> {

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
}