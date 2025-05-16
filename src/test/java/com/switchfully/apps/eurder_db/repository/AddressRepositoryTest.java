package com.switchfully.apps.eurder_db.repository;

import com.switchfully.apps.eurder_db.domain.Address;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.annotation.DirtiesContext;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@DataJpaTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
public class AddressRepositoryTest {
    @Autowired
    private AddressRepository addressRepository;
    private Address address;

    @BeforeEach
    void setUp() {
        addressRepository.deleteAll();
        address = new Address("street","number","2000","city","country");
    }

    @Test
        //checking if the persisted domain addres is the same when it returns from
    void givenCorrectAddress_whenSave_thenReturnAddressFromDatabase() {
        Address result = addressRepository.save(address);

        assertThat(result).isEqualTo(address);
        assertThat(result.getStreet()).isEqualTo(address.getStreet());
    }

    @Test
        //checking if an exception is thrown when providing incorrect data
    void givenNullAtNonNullable_whenSave_thenThrowException() {
        Address nullValueStreet = new Address(null,"number","2000","city","country");
        Address nullValuePostalCode = new Address("street","number",null,"city","country");
        Address nullValueCity = new Address("street","number","2000",null,"country");
        Address nullValueCountry = new Address("street","number","2000","city",null);

        assertThrows(Exception.class, () -> addressRepository.saveAndFlush(nullValueStreet));
        assertThrows(Exception.class, () -> addressRepository.saveAndFlush(nullValuePostalCode));
        assertThrows(Exception.class, () -> addressRepository.saveAndFlush(nullValueCity));
        assertThrows(Exception.class, () -> addressRepository.saveAndFlush(nullValueCountry));
    }

    @Test
        // an address with streetnumber null should be able to be saved without errors
    void givenNullForStreetNumber_whenSave_thenReturnAddressFromDatabase() {
        Address nullValueNumber = new Address("street",null,"2000","city","country");

        Address result = addressRepository.save(nullValueNumber);

        assertThat(result).isEqualTo(nullValueNumber);
        assertThat(result.getStreet()).isEqualTo("street");
    }

    @Test
        //checking if find by id works after saving a new address
    void givenAddressExistsInDatabase_whenFindById_thenReturnAddressFromDatabase() {
        Address savedAddress = addressRepository.save(address);
        Address result = addressRepository.findById(savedAddress.getId()).get();
        assertThat(result).isEqualTo(savedAddress);
    }
}
