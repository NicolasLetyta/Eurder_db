package com.switchfully.apps.eurder_db.webapi.controller;

import com.switchfully.apps.eurder_db.domain.*;
import com.switchfully.apps.eurder_db.repository.AddressRepository;
import com.switchfully.apps.eurder_db.repository.EurderRepository;
import com.switchfully.apps.eurder_db.repository.ItemRepository;
import com.switchfully.apps.eurder_db.repository.MemberRepository;
import com.switchfully.apps.eurder_db.service.AuthenticationService;
import com.switchfully.apps.eurder_db.service.EurderService;
import com.switchfully.apps.eurder_db.service.ItemService;
import com.switchfully.apps.eurder_db.service.MemberService;
import com.switchfully.apps.eurder_db.service.mapper.EurderMapper;
import com.switchfully.apps.eurder_db.service.mapper.ItemGroupMapper;
import com.switchfully.apps.eurder_db.service.mapper.MemberMapper;
import com.switchfully.apps.eurder_db.webapi.dto.*;
import io.restassured.RestAssured;
import io.restassured.common.mapper.TypeRef;
import io.restassured.http.ContentType;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;

@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@AutoConfigureTestDatabase
public class EurderControllerTest {

    @LocalServerPort
    private int port;

    @Autowired
    private EntityManager entityManager;

    @Autowired
    private MemberService memberService;
    @Autowired
    private MemberMapper memberMapper;
    @Autowired
    private MemberRepository memberRepository;
    @Autowired
    private AddressRepository addressRepository;
    @Autowired
    private AuthenticationService authenticationService;
    @Autowired
    private EurderService eurderService;
    @Autowired
    private EurderMapper eurderMapper;
    @Autowired
    private ItemService itemService;
    @Autowired
    private ItemRepository itemRepository;
    @Autowired
    private EurderRepository eurderRepository;
    @Autowired
    private ItemGroupMapper itemGroupMapper;

    Item itemRing;
    Item itemShoes;

    Address address;

    Member customer1;
    Member customer2;
    Member customer3;
    Member admin;

    Long lastItemId;
    Long lastEurderId;
    Long lastItemGroupId;

    @BeforeAll
    void beforeAll() {
        addressRepository.deleteAll();
        itemRepository.deleteAll();
        memberRepository.deleteAll();
        eurderRepository.deleteAll();

        address = addressRepository.save(new Address("street","number","city","state","country"));

        admin = memberRepository.save(new Member("admin","hanne","hanne@yahoo.com","pass","+321958331243", MemberRole.ADMIN,address.getId()));
        customer1 = memberRepository.save(new Member("customer","ally","ally@yahoo.com","pass","+32195833740", MemberRole.CUSTOMER,address.getId()));
        customer2 = memberRepository.save(new Member("customer","bert","bert@yahoo.com","pass","+32195812340", MemberRole.CUSTOMER,address.getId()));
        customer3 = memberRepository.save(new Member("customer","bea","bea@yahoo.com","pass","+32345812340", MemberRole.CUSTOMER,address.getId()));
    }

    @BeforeEach
    void beforeEach() {
        RestAssured.baseURI = "http://localhost";
        RestAssured.port = port;

        eurderRepository.deleteAll();
        itemRepository.deleteAll();

        itemRing = itemRepository.save(new Item("silver ring","A sterling silver ring",11.5,100));
        itemShoes = itemRepository.save(new Item("prada shoes","A pair of vintage prada shoes",40,100));

        //setup multiple carts customer 2
        Eurder cart1 = eurderRepository.saveAndFlush(new Eurder(customer2.getId()));
        Eurder cart2 = eurderRepository.saveAndFlush(new Eurder(customer2.getId()));
        Eurder cart3 = eurderRepository.saveAndFlush(new Eurder(customer2.getId()));
        ItemGroupDtoInput itemGroup1 = new ItemGroupDtoInput(itemRing.getId(),10);
        ItemGroupDtoInput itemGroup2 = new ItemGroupDtoInput(itemRing.getId(),15);
        ItemGroupDtoInput itemGroup3 = new ItemGroupDtoInput(itemShoes.getId(),9);
        eurderService.addItemGroupToExistingCart(itemGroup1, customer2,cart1.getId());
        eurderService.addItemGroupToExistingCart(itemGroup2, customer2,cart2.getId());
        eurderService.addItemGroupToExistingCart(itemGroup3, customer2,cart2.getId());

        //setup multiple finalized orders customer 3
        Eurder eurder1 = eurderRepository.saveAndFlush(new Eurder(customer3.getId()));
        eurderService.addItemGroupToExistingCart(new ItemGroupDtoInput(itemRing.getId(),1), customer3,eurder1.getId());
        eurderService.addItemGroupToExistingCart(new ItemGroupDtoInput(itemShoes.getId(),3), customer3,eurder1.getId());
        eurderService.placeEurder(eurder1.getId(),customer3);

        Eurder eurder2 = eurderRepository.saveAndFlush(new Eurder(customer3.getId()));
        eurderService.addItemGroupToExistingCart(new ItemGroupDtoInput(itemShoes.getId(),2), customer3,eurder2.getId());
        eurderService.placeEurder(eurder2.getId(),customer3);


        lastItemId= ((Number) entityManager.createNativeQuery("SELECT nextval('item_seq')").getSingleResult()).longValue();
        lastItemGroupId= ((Number) entityManager.createNativeQuery("SELECT nextval('item_group_seq')").getSingleResult()).longValue();
        lastEurderId= ((Number) entityManager.createNativeQuery("SELECT nextval('eurder_seq')").getSingleResult()).longValue();

    }

    @Test
    void givenValidItemGroupDtoInput_whenAddItemGroupToNewCart_thenReturnCorrectEurderDto() {
        //GIVEN
        String token = authenticationService.encode(customer1.getEmail(), customer1.getPassword());
        ItemGroupDtoInput body = new ItemGroupDtoInput(itemRing.getId(),10);

        EurderDtoOutput result = given()
                .contentType(ContentType.JSON)
                .body(body)
                .header("Authorization", token)
                .when()
                .post("/orders")
                .then()
                .statusCode(HttpStatus.CREATED.value())
                .extract()
                .as(EurderDtoOutput.class);

        assertThat(result.getTotalPrice()).isEqualTo(body.getQuantity()*itemRing.getPrice());
        assertThat(result.getId()).isEqualTo(lastEurderId+1);
        assertThat(result.getMemberName()).isEqualTo(customer1.getFullName());
    }

    @Test
    void givenValidItemGroupDtoInputAndCustomerCartExists_whenAddItemGroupToExistingCart_thenReturnCorrectEurderDto() {
        //GIVEN
        String token = authenticationService.encode(customer1.getEmail(), customer1.getPassword());
        Eurder cart = eurderRepository.save(new Eurder(customer1.getId()));
        ItemGroupDtoInput body = new ItemGroupDtoInput(itemRing.getId(),10);
        ItemGroupDtoOutput itemGroupDto = new ItemGroupDtoOutput(lastItemGroupId+1,itemRing.getName(),itemRing.getDescription(),
                body.getQuantity(),body.getQuantity()*itemRing.getPrice(),cart.getId());
        //EXPECTED RESULT
        EurderDtoOutput expectedResult = new EurderDtoOutput(cart.getId(), customer1.getFullName(), customer1.getId(),EurderStatus.CART,
                List.of(itemGroupDto),itemGroupDto.getSubtotalPrice());


        EurderDtoOutput result = given()
                .contentType(ContentType.JSON)
                .body(body)
                .header("Authorization", token)
                .when()
                .post("/orders/"+cart.getId())
                .then()
                .statusCode(HttpStatus.CREATED.value())
                .extract()
                .as(EurderDtoOutput.class);

        assertThat(result).isEqualTo(expectedResult);
    }

    @Test
    void givenCustomerCartWithContentExists_whenPlaceEurder_thenReturnCorrectEurderDto() {
        //GIVEN
        String token = authenticationService.encode(customer1.getEmail(), customer1.getPassword());
        Eurder cart = eurderRepository.save(new Eurder(customer1.getId()));
        ItemGroupDtoInput itemGroup1 = new ItemGroupDtoInput(itemRing.getId(),10);
        ItemGroupDtoInput itemGroup2 = new ItemGroupDtoInput(itemRing.getId(),15);
        ItemGroupDtoInput itemGroup3 = new ItemGroupDtoInput(itemShoes.getId(),9);
        eurderService.addItemGroupToExistingCart(itemGroup1, customer1,cart.getId());
        eurderService.addItemGroupToExistingCart(itemGroup2, customer1,cart.getId());
        eurderService.addItemGroupToExistingCart(itemGroup3, customer1,cart.getId());
        //EXPECTED RESULT
        List<ItemGroupDtoOutput> itemGroupDtos = List.of(new ItemGroupDtoOutput(lastItemGroupId+1,itemRing.getName(),itemRing.getDescription(),
                itemGroup1.getQuantity(),itemGroup1.getQuantity()*itemRing.getPrice(),cart.getId()),
                new ItemGroupDtoOutput(lastItemGroupId+2,itemRing.getName(),itemRing.getDescription(),
                        itemGroup2.getQuantity(),itemGroup2.getQuantity()*itemRing.getPrice(),cart.getId()),
                new ItemGroupDtoOutput(lastItemGroupId+3,itemShoes.getName(),itemShoes.getDescription(),
                        itemGroup3.getQuantity(),itemGroup3.getQuantity()*itemShoes.getPrice(),cart.getId()));

        double finalTotalPrice = itemGroupDtos.stream()
                .mapToDouble(ItemGroupDtoOutput::getSubtotalPrice)
                .sum();

        EurderDtoOutput expectedResult = new EurderDtoOutput(cart.getId(), customer1.getFullName(), customer1.getId(),EurderStatus.FINALIZED,
                itemGroupDtos,finalTotalPrice);


        EurderDtoOutput result = given()
                .header("Authorization", token)
                .when()
                .put("/orders/"+cart.getId())
                .then()
                .statusCode(HttpStatus.OK.value())
                .extract()
                .as(EurderDtoOutput.class);

        assertThat(result).isEqualTo(expectedResult);
        assertThat(result.getItemGroups().size()).isEqualTo(3);
    }

    @Test
    void givenCustomerHasMultipleCarts_whenGetMemberCarts_thenReturnListOfMemberCarts() {
        //GIVEN
        String token = authenticationService.encode(customer2.getEmail(), customer2.getPassword());

        //EXPECTED RESULT
        List<EurderDtoList> expectedResult = eurderService.getMemberCarts(customer2);


        List<EurderDtoList> result = given()
                .header("Authorization", token)
                .when()
                .get("/orders/carts")
                .then()
                .statusCode(HttpStatus.OK.value())
                .extract()
                .as(new TypeRef<List<EurderDtoList>>() {});

        assertThat(result).isEqualTo(expectedResult);
    }

    @Test
    void givenCustomerHasMultipleFinalizedOrders_whenCreateEurderReport_thenReturnEurderReport() {
        //GIVEN
        String token = authenticationService.encode(customer3.getEmail(), customer3.getPassword());
        //EXPECTED RESULT
        EurderDtoReport expectedResult = eurderService.createEurderReport(customer3);

        EurderDtoReport result = given()
                .header("Authorization", token)
                .when()
                .get("/orders/eurder-report")
                .then()
                .statusCode(HttpStatus.OK.value())
                .extract()
                .as(EurderDtoReport.class);

        System.out.println(expectedResult);
        System.out.println(result);

        assertThat(result).isEqualTo(expectedResult);

    }
}
