package com.switchfully.apps.eurder_db.service;

import com.switchfully.apps.eurder_db.domain.*;
import com.switchfully.apps.eurder_db.exception.InvalidInputException;
import com.switchfully.apps.eurder_db.repository.AddressRepository;
import com.switchfully.apps.eurder_db.repository.EurderRepository;
import com.switchfully.apps.eurder_db.repository.ItemRepository;
import com.switchfully.apps.eurder_db.repository.MemberRepository;
import com.switchfully.apps.eurder_db.service.mapper.*;
import com.switchfully.apps.eurder_db.webapi.dto.*;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.LocalDate;
import java.util.List;

@SpringBootTest
@AutoConfigureTestDatabase
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
public class EurderServiceTest {
    @Autowired
    private AddressRepository addressRepository;
    @Autowired
    private AddressMapper addressMapper;
    @Autowired
    private MemberRepository memberRepository;
    @Autowired
    private MemberMapper memberMapper;
    @Autowired
    private MemberService memberService;
    @Autowired
    private ItemRepository itemRepository;
    @Autowired
    private ItemMapper itemMapper;
    @Autowired
    private ItemService itemService;
    @Autowired
    private EurderRepository eurderRepository;
    @Autowired
    private EurderMapper eurderMapper;
    @Autowired
    private EurderService eurderService;
    @Autowired
    private ItemGroupMapper itemGroupMapper;

    @PersistenceContext
    private EntityManager entityManager;

    Address address;

    Item item1, item2, item3;

    Member customer1, customer2;
    Member admin;

    Eurder cart1, cart2, cart3;

    long lastItemGroupId;
    long lastEurderId;


    @BeforeAll
    void beforeAll() {
        addressRepository.deleteAll();
        memberRepository.deleteAll();
        itemRepository.deleteAll();
        eurderRepository.deleteAll();

        address = addressRepository.save(new Address("street", "city", "state", "zipcode", "country"));

        customer1 = memberRepository.save(new Member("name1","name1","name1@gmail.com","pass","+3212345678", MemberRole.CUSTOMER, address.getId()));
        customer2 = memberRepository.save(new Member("name2","name2","name2@gmail.com","pass","+3112345678", MemberRole.CUSTOMER, address.getId()));
        admin = memberRepository.save(new Member("admin","admin","admin@gmail.com","pass","+3012345678", MemberRole.ADMIN, address.getId()));

    }

    private void CreateValidFinalizedEurdersForCustomer(Long memberId) {
        cart2 = eurderRepository.saveAndFlush(new Eurder(memberId));
        cart3 = eurderRepository.saveAndFlush(new Eurder(memberId));

        ItemGroup itemGroup1 = new ItemGroup(3,item1,cart2);
        ItemGroup itemGroup2 = new ItemGroup(15,item2,cart2);
        itemGroup1.setShippingDate(LocalDate.now().plusDays(1));
        itemGroup2.setShippingDate(LocalDate.now().plusDays(1));
        itemGroup1.setTotalPriceAtEurderDate(itemGroup1.calculateCurrentSubtotalPrice());
        itemGroup2.setTotalPriceAtEurderDate(itemGroup2.calculateCurrentSubtotalPrice());
        cart2.addItemGroup(itemGroup1);
        cart2.addItemGroup(itemGroup2);
        cart2.setStatusFinalized();
        cart2 = eurderRepository.saveAndFlush(cart2);

        ItemGroup itemGroup3 = new ItemGroup(11,item3,cart3);
        ItemGroup itemGroup4 = new ItemGroup(3,item3,cart3);
        itemGroup3.setShippingDate(LocalDate.now().plusDays(1));
        itemGroup4.setShippingDate(LocalDate.now().plusDays(1));
        itemGroup3.setTotalPriceAtEurderDate(itemGroup3.calculateCurrentSubtotalPrice());
        itemGroup4.setTotalPriceAtEurderDate(itemGroup4.calculateCurrentSubtotalPrice());
        cart3.addItemGroup(itemGroup3);
        cart3.addItemGroup(itemGroup4);
        cart3.setStatusFinalized();
        cart3 = eurderRepository.saveAndFlush(cart3);

    }


    @BeforeEach
    void beforeEach() {
        eurderRepository.deleteAll();
        itemRepository.deleteAll();

        cart1 = eurderRepository.save(new Eurder(customer1.getId()));

        item1 = itemRepository.saveAndFlush(new Item("item1","this is item1",1.0,100));
        item2 = itemRepository.saveAndFlush(new Item("item2","this is item2",2.0,500));
        item3 = itemRepository.saveAndFlush(new Item("item3","this is item3",5.0,250));


        lastItemGroupId = ((Number) entityManager.createNativeQuery("SELECT nextval('item_group_seq')").getSingleResult()).longValue();
        lastEurderId = ((Number) entityManager.createNativeQuery("SELECT nextval('eurder_seq')").getSingleResult()).longValue();
    }

    @Test
    void givenCustomerHasEurderCart_whenAddItemGroupToCart_thenReturnUpdatedEurderCartDto() {
        ItemGroupDtoInput input = new ItemGroupDtoInput(item1.getId(),99);
        ItemGroup itemGroupDomain = itemGroupMapper.inputToItemGroup(input,item1,cart1);
        itemGroupDomain.setShippingDate(LocalDate.now().minusDays(1));
        ItemGroupDtoOutput outputData = itemGroupMapper.itemGroupToOutputCart(itemGroupDomain);

        EurderDtoOutput expectedResult = getEurderDtoOutput(outputData, lastItemGroupId, EurderStatus.CART, customer1,cart1);

        EurderDtoOutput result = eurderService.addItemGroupToCart(input,customer1,cart1.getId());

        assertThat(result).isEqualTo(expectedResult);
    }

    @Test
    void givenCustomerHasFilledEurderCart_whenPlaceEurder_thenReturnUpdatedEurderCartDtoFinalized() {
        ItemGroupDtoInput input = new ItemGroupDtoInput(item1.getId(),10);
        ItemGroup itemGroupDomain = itemGroupMapper.inputToItemGroup(input,item1,cart1);
        itemGroupDomain.setShippingDate(LocalDate.now().plusDays(1));
        ItemGroupDtoOutput outputData = itemGroupMapper.itemGroupToOutputCart(itemGroupDomain);

        EurderDtoOutput expectedResult = getEurderDtoOutput(outputData, lastItemGroupId, EurderStatus.FINALIZED, customer1,cart1);

        cart1.addItemGroup(itemGroupDomain);
        eurderRepository.save(cart1);
        EurderDtoOutput result = eurderService.placeEurder(cart1.getId(),customer1);

        assertThat(result).isEqualTo(expectedResult);
        assertThat(itemRepository.findById(item1.getId()).get().getStock()).isEqualTo(item1.getStock()-input.getQuantity());
        assertThat(cart1.getItemGroups().get(0).getShippingDate()).isEqualTo(LocalDate.now().plusDays(1));
    }

    @Test
    void givenCustomerHasFilledCartMoreThanItemStock_whenPlaceEurder_thenReturnUpdatedEurderCartDtoFinalizedAndCorrectShippingDate() {
        ItemGroupDtoInput input1 = new ItemGroupDtoInput(item1.getId(),99);
        ItemGroupDtoInput input2 = new ItemGroupDtoInput(item1.getId(),100);

        ItemGroup itemGroupDomain1 = itemGroupMapper.inputToItemGroup(input1,item1,cart1);
        ItemGroup itemGroupDomain2 = itemGroupMapper.inputToItemGroup(input2,item1,cart1);


        itemGroupDomain1.setShippingDate(LocalDate.now().plusDays(7));
        itemGroupDomain2.setShippingDate(LocalDate.now().plusDays(7));

        ItemGroupDtoOutput outputData1 = itemGroupMapper.itemGroupToOutputFinalized(itemGroupDomain1);
        ItemGroupDtoOutput outputData2 = itemGroupMapper.itemGroupToOutputFinalized(itemGroupDomain2);

        ItemGroupDtoOutput itemGroupOutput1 = new ItemGroupDtoOutput(lastItemGroupId +1,
                outputData1.getItemName(),
                outputData1.getItemDescription(),
                outputData1.getQuantity(),
                itemGroupDomain1.calculateCurrentSubtotalPrice(),
                outputData1.getEurderId());

        ItemGroupDtoOutput itemGroupOutput2 = new ItemGroupDtoOutput(lastItemGroupId +2,
                outputData2.getItemName(),
                outputData2.getItemDescription(),
                outputData2.getQuantity(),
                itemGroupDomain2.calculateCurrentSubtotalPrice(),
                outputData2.getEurderId());

        EurderDtoOutput expectedResult = new EurderDtoOutput(cart1.getId(), customer1.getFullName(), customer1.getId(), EurderStatus.FINALIZED, List.of(itemGroupOutput1,itemGroupOutput2),itemGroupOutput1.getSubtotalPrice()+itemGroupOutput2.getSubtotalPrice());

        cart1.addItemGroup(itemGroupDomain1);
        cart1.addItemGroup(itemGroupDomain2);
        eurderRepository.save(cart1);
        EurderDtoOutput result = eurderService.placeEurder(cart1.getId(),customer1);

        assertThat(result).isEqualTo(expectedResult);
        assertThat(itemRepository.findById(item1.getId()).get().getStock()).isEqualTo(0);
        assertThat(cart1.getItemGroups().get(0).getShippingDate()).isEqualTo(LocalDate.now().plusDays(7));
        assertThat(cart1.getItemGroups().get(1).getShippingDate()).isEqualTo(LocalDate.now().plusDays(7));

    }

    @Test
    void givenCustomerHasEmptyEurderCart_whenPlaceEurder_thenThrowsException() {
        assertThrows(InvalidInputException.class, () -> eurderService.placeEurder(cart1.getId(),customer1));
    }

    @Test
    void givenCustomerHasfinalizedEurder_whenPlaceEurder_thenThrowsException() {
        cart1.setStatusFinalized();
        assertThrows(InvalidInputException.class, () -> eurderService.placeEurder(cart1.getId(),customer1));
    }

    @Test
    void givenInvalidEurderId_whenPlaceEurder_thenThrowsException() {
        assertThrows(InvalidInputException.class, () -> eurderService.placeEurder(cart1.getId(),customer2));
    }

    @Test
    void givenCustomerHasfinalizedEurder_whenPlaceReEurder_thenReturnNewEurderDtoFinalized() {
        ItemGroupDtoInput input = new ItemGroupDtoInput(item1.getId(),1);
        ItemGroup itemGroupDomain = itemGroupMapper.inputToItemGroup(input,item1,cart1);
        itemGroupDomain.setShippingDate(LocalDate.now().plusDays(1));
        ItemGroupDtoOutput outputData = itemGroupMapper.itemGroupToOutputFinalized(itemGroupDomain);

        ItemGroupDtoOutput itemGroupOutput = new ItemGroupDtoOutput(lastItemGroupId +2,
                outputData.getItemName(),
                outputData.getItemDescription(),
                outputData.getQuantity(),
                itemGroupDomain.calculateCurrentSubtotalPrice(),
                lastEurderId+1);

        EurderDtoOutput expectedResult = new EurderDtoOutput(lastEurderId+1,
                customer1.getFullName(),
                customer1.getId(),
                EurderStatus.FINALIZED,
                List.of(itemGroupOutput),
                itemGroupDomain.calculateCurrentSubtotalPrice());

        cart1.addItemGroup(itemGroupDomain);
        cart1.setStatusFinalized();
        eurderRepository.save(cart1);
        EurderDtoOutput result = eurderService.placeReEurder(cart1.getId(),customer1);

        assertThat(result.getEurderStatus()).isEqualTo(EurderStatus.FINALIZED.toString());
        assertThat(result).isEqualTo(expectedResult);
    }

    @Test
    void givenCustomerHasfinalizedEurders_whenCreateEurderReport_thenReturnEurderReport() {
        ItemGroupDtoList itemGroup1 = new ItemGroupDtoList(item1.getName(), 3,item1.getPrice()*3);
        ItemGroupDtoList itemGroup2 = new ItemGroupDtoList(item2.getName(), 15,item2.getPrice()*15);
        List<ItemGroupDtoList> list1 = List.of(itemGroup1,itemGroup2);

        ItemGroupDtoList itemGroup3 = new ItemGroupDtoList(item3.getName(), 11,item3.getPrice()*11);
        ItemGroupDtoList itemGroup4 = new ItemGroupDtoList(item3.getName(), 3,item3.getPrice()*3);
        List<ItemGroupDtoList> list2 = List.of(itemGroup3,itemGroup4);

        EurderDtoList eurder1 = new EurderDtoList(lastEurderId+1,list1,list1.stream()
                .mapToDouble(ItemGroupDtoList::getSubtotalPrice)
                .sum());
        EurderDtoList eurder2 = new EurderDtoList(lastEurderId+2,list2,list2.stream()
                .mapToDouble(ItemGroupDtoList::getSubtotalPrice)
                .sum());
        List<EurderDtoList> eurders = List.of(eurder1,eurder2);

        EurderDtoReport expectedResult = new EurderDtoReport(eurders,eurders.stream().mapToDouble(EurderDtoList::getTotalPrice).sum());

        CreateValidFinalizedEurdersForCustomer(customer2.getId());
        eurderRepository.saveAndFlush(cart2);
        eurderRepository.saveAndFlush(cart3);

        EurderDtoReport result = eurderService.createEurderReport(customer2);

        assertThat(result).isEqualTo(expectedResult);
    }


    private static EurderDtoOutput getEurderDtoOutput(ItemGroupDtoOutput outputData, long lastItemGroupId, EurderStatus status, Member customer1, Eurder cart) {
        ItemGroupDtoOutput itemGroupOutput = new ItemGroupDtoOutput(lastItemGroupId +1,
                outputData.getItemName(),
                outputData.getItemDescription(),
                outputData.getQuantity(),
                outputData.getSubtotalPrice(),
                outputData.getEurderId());


        return new EurderDtoOutput(cart.getId(), customer1.getFullName(), customer1.getId(), status, List.of(itemGroupOutput),itemGroupOutput.getSubtotalPrice());
    }


}
