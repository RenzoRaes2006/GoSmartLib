package ap.be.backend.repository;

import java.util.Collection;
import java.util.Set;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import ap.be.backend.entity.BoekSchade;

public interface BoekSchadeRepository extends JpaRepository<BoekSchade, Long> {

    @Query("SELECT DISTINCT s.uitlening.exemplaar.boek.id FROM BoekSchade s WHERE s.uitlening.exemplaar.boek.id IN :ids")
    Set<Long> findBoekIdsMetSchade(@Param("ids") Collection<Long> ids);
}
