package com.switchfully.apps.eurder_db.webapi.controller;

import com.switchfully.apps.eurder_db.domain.Address;
import com.switchfully.apps.eurder_db.domain.Item;
import com.switchfully.apps.eurder_db.domain.Member;
import com.switchfully.apps.eurder_db.domain.MemberRole;
import com.switchfully.apps.eurder_db.repository.AddressRepository;
import com.switchfully.apps.eurder_db.repository.ItemRepository;
import com.switchfully.apps.eurder_db.repository.MemberRepository;
import com.switchfully.apps.eurder_db.service.AuthenticationService;
import com.switchfully.apps.eurder_db.service.ItemService;
import com.switchfully.apps.eurder_db.webapi.dto.ItemDtoInput;
import com.switchfully.apps.eurder_db.webapi.dto.ItemDtoOutput;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
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

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;

@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@AutoConfigureTestDatabase
public class ItemControllerTest {

    @LocalServerPort
    private int port;

    @Autowired
    private ItemService itemService;
    @Autowired
    private ItemRepository itemRepository;
    @Autowired
    private MemberRepository memberRepository;
    @Autowired
    private AddressRepository addressRepository;
    @Autowired
    private AuthenticationService authenticationService;

    @PersistenceContext
    private EntityManager entityManager;

    Item itemRing;
    Item itemShoes;
    Item itemPants;

    Address address;

    Member customer;
    Member admin;

    Long lastItemId;

    @BeforeAll
    void beforeAll() {
        itemRepository.deleteAll();
        memberRepository.deleteAll();
        addressRepository.deleteAll();

        itemRing = itemRepository.save(new Item("silver ring","A sterling silver ring",11.5,100));
        itemShoes = itemRepository.save(new Item("prada shoes","A pair of vintage prada shoes",40,100));
        itemPants = itemRepository.save(new Item("Jil Sander trousers","A pair of Jil Sander trousers",40,100));

        address = addressRepository.save(new Address("street","number","postalcode","city","country"));

        customer = memberRepository.save(new Member("customer","Loes","loes@yahoo.com","password","+32495033927", MemberRole.CUSTOMER,address.getId()));
        admin = memberRepository.save(new Member("admin","Roos","Roos@yahoo.com","password","+32495033123", MemberRole.ADMIN,address.getId()));
    }

    @BeforeEach
    void beforeEach() {
        RestAssured.baseURI = "http://localhost";
        RestAssured.port = port;
        lastItemId= ((Number) entityManager.createNativeQuery("SELECT nextval('item_seq')").getSingleResult()).longValue();
    }

    @Test
    void givenValidAdminCredsAndItemDtoInput_whenAddItem_thenReturnNewItemDtoOutput() {
        //GIVEN
        String token = authenticationService.encode(admin.getEmail(), admin.getPassword());
        ItemDtoInput itemDtoInput = new ItemDtoInput("newItem","this is newItem",13.0,100);

        //EXPECTED RESULT
        ItemDtoOutput expectedResult = new ItemDtoOutput(lastItemId+1,itemDtoInput.getName(),itemDtoInput.getDescription(),itemDtoInput.getPrice(),itemDtoInput.getStock());

        ItemDtoOutput result = given()
                .contentType(ContentType.JSON)
                .body(itemDtoInput)
                .header("Authorization", token)
                .when()
                .post("/items")
                .then()
                .statusCode(HttpStatus.CREATED.value())
                .extract()
                .as(ItemDtoOutput.class);

        assertThat(result).isEqualTo(expectedResult);
    }

    @Test
    void givenIndValidAdminCreds_whenAddItem_thenReturnStatusUnauthorized() {
        //GIVEN
        String token = authenticationService.encode(admin.getEmail(), admin.getPassword());
        ItemDtoInput itemDtoInput = new ItemDtoInput("","this is newItem",-10.0,-9);

        given()
                .contentType(ContentType.JSON)
                .body(itemDtoInput)
                .header("Authorization", token)
                .when()
                .post("/items")
                .then()
                .statusCode(HttpStatus.BAD_REQUEST.value());
    }

    @Test
    void givenIndValidItemDtoInput_whenAddItem_thenReturnStatusBadRequest() {
        //GIVEN
        String token = "invalid";
        ItemDtoInput itemDtoInput = new ItemDtoInput("newItem","this is newItem",13.0,100);

        given()
                .contentType(ContentType.JSON)
                .body(itemDtoInput)
                .header("Authorization", token)
                .when()
                .post("/items")
                .then()
                .statusCode(HttpStatus.UNAUTHORIZED.value());
    }

    @Test
    void givenValidAdminCredsAndItemDtoInput_whenUpdateItem_thenReturnUpdatedItemDtoOutput() {
        //GIVEN
        String token = authenticationService.encode(admin.getEmail(), admin.getPassword());
        ItemDtoInput itemDtoInput = new ItemDtoInput("NEW NAME","NEW DESCRIPTION",100.0,500);

        //EXPECTED RESULT
        ItemDtoOutput expectedResult = new ItemDtoOutput(itemShoes.getId(),itemDtoInput.getName(),itemDtoInput.getDescription(),itemDtoInput.getPrice(),itemDtoInput.getStock());

        ItemDtoOutput result = given()
                .contentType(ContentType.JSON)
                .body(itemDtoInput)
                .header("Authorization", token)
                .when()
                .put("/items/" + itemShoes.getId())
                .then()
                .statusCode(HttpStatus.OK.value())
                .extract()
                .as(ItemDtoOutput.class);

        assertThat(result).isEqualTo(expectedResult);
    }




}
