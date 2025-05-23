package com.switchfully.apps.eurder_db.repository;

import com.switchfully.apps.eurder_db.domain.Eurder;
import com.switchfully.apps.eurder_db.domain.EurderStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EurderRepository extends JpaRepository<Eurder, Long> {
    Optional<Eurder> findByMemberIdAndStatusAndId(Long memberId, EurderStatus status, Long id);

    List<Eurder> findAllByMemberIdAndStatus(Long memberId, EurderStatus status);


    boolean existsById(Long id);

}
