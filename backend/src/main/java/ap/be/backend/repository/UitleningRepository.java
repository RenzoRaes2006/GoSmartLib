package ap.be.backend.repository;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import ap.be.backend.entity.Uitlening;

@Repository
public interface UitleningRepository extends JpaRepository<Uitlening, Long> {

    // active leningnen van een gebruiker
    List<Uitlening> findByGebruiker_GebruikerIDAndIsIngeleverdFalse(Long gebruikerId);

    // alle leningen van een gebruiker inlverdatum in verleden
    boolean existsByGebruiker_GebruikerIDAndIsIngeleverdFalseAndInleverDatumBefore(Long gebruikerId, LocalDate vandaag);

    // heeft dit boek al
    boolean existsByGebruiker_GebruikerIDAndBoek_IdAndIsIngeleverdFalse(Long gebruikerId, Long boekId);

    // alle aanvragen ophalen
    List<Uitlening> findByStatus(String status);

    boolean existsByGebruiker_GebruikerIDAndStatus(Long gebruikerId, String status);
}
