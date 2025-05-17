package com.switchfully.apps.eurder_db.repository;

import com.switchfully.apps.eurder_db.domain.*;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.annotation.DirtiesContext;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@DataJpaTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class EurderRepositoryTest {
    @Autowired
    private AddressRepository addressRepository;
    @Autowired
    private ItemRepository itemRepository;
    @Autowired
    private MemberRepository memberRepository;
    @Autowired
    private EurderRepository eurderRepository;

    @PersistenceContext
    private EntityManager entityManager;

    Item item1, item2, item3;
    Address address;
    Member customer1, customer2;

    Eurder cart1, cart2;

    ItemGroup itemGroup1, itemGroup2, itemGroup3;

    long lastEurderId;

    @BeforeAll
    void beforeAll() {
        itemRepository.deleteAll();
        addressRepository.deleteAll();
        memberRepository.deleteAll();

        item1 = itemRepository.saveAndFlush(new Item("item1","this is item1",1.0,100));
        item2 = itemRepository.saveAndFlush(new Item("item2","this is item2",2.0,100));
        item3 = itemRepository.saveAndFlush(new Item("item3","this is item3",3.0,100));

        address = addressRepository.save(new Address("street","number","postalcode","city","country"));

        customer1 = memberRepository.saveAndFlush(new Member("name1","name1","email1","pass","phone1", MemberRole.CUSTOMER, address.getId()));
        customer2 = memberRepository.saveAndFlush(new Member("name2","name2","email2","pass","phone2", MemberRole.CUSTOMER, address.getId()));
    }

    @BeforeEach
    void beforeEach() {
        eurderRepository.deleteAll();

        cart1 = eurderRepository.saveAndFlush(new Eurder(customer1.getId()));
        cart2 = eurderRepository.saveAndFlush(new Eurder(customer2.getId()));

        lastEurderId = ((Number) entityManager.createNativeQuery("SELECT nextval('eurder_seq')").getSingleResult()).longValue();
    }

    @Test
    void givenCustomerExists_whenCreateEurderAndSave_thenReturnEurderFromDatabase() {
        Eurder result = eurderRepository.saveAndFlush(new Eurder(customer1.getId()));
        assertThat(result.getId()).isEqualTo(lastEurderId+1);
    }

    @Test
    void givenCustomerWithEurderWitHItemGroupsExists_whenfindById_thenReturnCorrectEurderFromDatabase() {
        //GIVEN
        itemGroup1 = new ItemGroup(10,item1,cart1);
        itemGroup1.setShippingDate(LocalDate.now());
        cart1.addItemGroup(itemGroup1);

        cart1 = eurderRepository.saveAndFlush(cart1);

        Eurder result = eurderRepository.findById(cart1.getId()).orElse(null);

        assertThat(result.getItemGroups()).hasSize(1);
        assertThat(result.getItemGroups().get(0).getItem()).isEqualTo(item1);
        assertThat(result.getItemGroups().get(0).getQuantity()).isEqualTo(itemGroup1.getQuantity());
        assertThat(result.getItemGroups().get(0).getShippingDate()).isEqualTo(itemGroup1.getShippingDate());
        assertThat(result.getItemGroups().get(0).calculateCurrentSubtotalPrice()).isEqualTo(itemGroup1.calculateCurrentSubtotalPrice());
        assertThat(result).isEqualTo(cart1);
    }

    @Test
    void givenCartExists_whenfindByMemberIdAndStatus_thenReturnCorrectEurderFromDatabase() {
        Eurder result = eurderRepository.findByMemberIdAndStatusAndId(customer1.getId(),EurderStatus.CART,cart1.getId()).orElse(null);

        assertThat(result).isEqualTo(cart1);
        assertThat(result.getStatus()).isEqualTo(cart2.getStatus());
        assertThat(result.getMemberId()).isEqualTo(customer1.getId());
    }

    @Test
    void givenMultipleEurdersWithStatusFinalizesOfMemberExists_whenfindAllByMemberIdAndStatus_thenReturnCorrectEurdersFromDatabase() {
        Eurder finalized1 = new Eurder(customer1.getId());
        Eurder finalized2 = new Eurder(customer1.getId());
        itemGroup1 = new ItemGroup(10,item1,finalized1);
        itemGroup2 = new ItemGroup(10,item2,finalized2);

        itemGroup1.setShippingDate(LocalDate.now());
        itemGroup2.setShippingDate(LocalDate.now());

        finalized1.addItemGroup(itemGroup1);
        finalized2.addItemGroup(itemGroup2);

        finalized1.setStatusFinalized();
        finalized2.setStatusFinalized();

        finalized1 = eurderRepository.save(finalized1);
        finalized2 = eurderRepository.save(finalized2);

        List<Eurder> result = eurderRepository.findAllByMemberIdAndStatus(customer1.getId(),EurderStatus.FINALIZED);

        assertThat(result).hasSize(2);
        assertThat(result.get(0)).isEqualTo(finalized1);
        assertThat(result.get(1)).isEqualTo(finalized2);
    }




}
