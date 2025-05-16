package com.switchfully.apps.eurder_db.repository;

import com.switchfully.apps.eurder_db.domain.Address;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AddressRepository extends JpaRepository<Address, Long> {

}
