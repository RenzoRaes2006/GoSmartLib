package ap.be.backend;

import org.springframework.data.jpa.repository.JpaRepository;

import ap.be.backend.entity.TestEntity;

public interface TestRepository extends JpaRepository<TestEntity, Long> {

}