package ap.be.backend.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import ap.be.backend.entity.Gebruiker;

@Repository
public interface GebruikerRepository extends JpaRepository<Gebruiker, Long> {
    @Query("select g from Gebruiker g where g.SchoolId = :schoolId")
    Optional<Gebruiker> findBySchoolId(@Param("schoolId") long schoolId);
}
