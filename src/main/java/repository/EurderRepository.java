package repository;

import domain.Eurder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EurderRepository extends JpaRepository<Eurder, Long> {
}
