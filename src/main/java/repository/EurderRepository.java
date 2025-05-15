package repository;

import domain.Eurder;
import domain.EurderStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EurderRepository extends JpaRepository<Eurder, Long> {
    Optional<Eurder> findByMemberIdAndStatus(Long memberId, EurderStatus status);

    Optional<Eurder> findByIdAndMemberId(Long eurderId, Long memberId);

    List<Eurder> findAllByMemberIdAndStatus(Long memberId, EurderStatus status);

}
