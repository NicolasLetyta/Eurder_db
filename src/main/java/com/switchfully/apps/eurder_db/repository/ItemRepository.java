package com.switchfully.apps.eurder_db.repository;

import com.switchfully.apps.eurder_db.domain.Item;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ItemRepository extends JpaRepository<Item, Long> {
    boolean existsByName(String name);
}
