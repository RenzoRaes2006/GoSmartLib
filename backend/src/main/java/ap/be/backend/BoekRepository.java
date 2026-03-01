package ap.be.backend;

import org.springframework.data.jpa.repository.JpaRepository;

import ap.be.backend.entity.Boek;

public interface BoekRepository extends JpaRepository<Boek,Long>{
    
}
