package com.switchfully.apps.eurder_db.webapi.controller;

import com.switchfully.apps.eurder_db.domain.Address;
import com.switchfully.apps.eurder_db.domain.Member;
import com.switchfully.apps.eurder_db.domain.MemberRole;
import com.switchfully.apps.eurder_db.repository.AddressRepository;
import com.switchfully.apps.eurder_db.repository.MemberRepository;
import com.switchfully.apps.eurder_db.service.AuthenticationService;
import com.switchfully.apps.eurder_db.service.MemberService;
import com.switchfully.apps.eurder_db.service.mapper.MemberMapper;
import com.switchfully.apps.eurder_db.webapi.dto.*;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.checkerframework.checker.units.qual.A;
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

import java.util.List;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;

@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@AutoConfigureTestDatabase
public class MemberControllerTest {

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

    Address address;

    Member customer;
    Member admin;

    Long lastMemberId;
    Long lastAddressId;

    @BeforeAll
    void beforeAll() {
        address = addressRepository.save(new Address("street","number","city","state","country"));

        admin = memberRepository.save(new Member("admin","hanne","hanne@yahoo.com","pass","+321958331243", MemberRole.ADMIN,address.getId()));
        customer = memberRepository.save(new Member("customer","ally","ally@yahoo.com","pass","+32195833740", MemberRole.CUSTOMER,address.getId()));
    }

    @BeforeEach
    void beforeEach() {
        RestAssured.baseURI = "http://localhost";
        RestAssured.port = port;

        lastMemberId= ((Number) entityManager.createNativeQuery("SELECT nextval('member_seq')").getSingleResult()).longValue();
        lastAddressId= ((Number) entityManager.createNativeQuery("SELECT nextval('address_seq')").getSingleResult()).longValue();
    }

    @Test
    void givenValidMemberDtoInput_whenRegisterAsCustomer_thenResturnRegistereMemberDto() {
        //GIVEN
        AddressDtoInput addressPayload = new AddressDtoInput("street","number","city","state","country");
        MemberDtoInput body = new MemberDtoInput("member","memebr","member@yahoo.com","pass","+329175022837",addressPayload);
        //EXPECTED RESULT
        AddressDtoOutput addressDto = new AddressDtoOutput(lastAddressId+1,addressPayload.getStreet(),addressPayload.getStreetNumber(),addressPayload.getPostalCode(),addressPayload.getCity(),addressPayload.getCountry());
        MemberDtoOutput expectedResult = new MemberDtoOutput(lastMemberId+1,body.getFirstName()+" "+body.getLastName(),
                body.getEmail(),MemberRole.CUSTOMER,addressDto);

        MemberDtoOutput result = given()
                .contentType(ContentType.JSON)
                .body(body)
                .when()
                .post("/members")
                .then()
                .statusCode(HttpStatus.CREATED.value())
                .extract()
                .as(MemberDtoOutput.class);

        assertThat(result).isEqualTo(expectedResult);
    }

    @Test
    void givenInvalidMemberDtoInput_whenRegisterAsCustomer_thenReturnStatus400() {
        //GIVEN
        AddressDtoInput addressPayload = new AddressDtoInput("street","number","city","state","country");

        MemberDtoInput emptyFirstName = new MemberDtoInput("  ","member", "member@yahoo.com", "pass","+329175022837",addressPayload);
        MemberDtoInput emptyLastName = new MemberDtoInput("member","  ", "member@yahoo.com", "pass","+329175022837",addressPayload);
        MemberDtoInput duplicateEmail = new MemberDtoInput("member","member", customer.getEmail(), "pass","+329175022837",addressPayload);
        MemberDtoInput invalidEmail = new MemberDtoInput("member","member", "invalidformat", "pass","+329175022837",addressPayload);
        MemberDtoInput emptyEmail = new MemberDtoInput("member","member", "   ", "pass","+329175022837",addressPayload);
        MemberDtoInput emptyPassword = new MemberDtoInput("member","member", "member@yahoo.com", "  ","+329175022837",addressPayload);
        MemberDtoInput duplicatePhone = new MemberDtoInput("member","member", "member@yahoo.com", "pass",customer.getPhone(),addressPayload);
        MemberDtoInput invalidPhone = new MemberDtoInput("member","member", "member@yahoo.com", "pass","invalid format",addressPayload);
        MemberDtoInput emptyAddressDto = new MemberDtoInput("member","member", "member@yahoo.com", "pass","329175022837",null);

        List<MemberDtoInput> bodyList = List.of(emptyFirstName,emptyLastName,duplicateEmail,invalidEmail,emptyEmail,emptyPassword,duplicatePhone,invalidPhone,emptyAddressDto);

        for(MemberDtoInput body : bodyList) {
            given()
                    .contentType(ContentType.JSON)
                    .body(body)
                    .when()
                    .post("/members")
                    .then()
                    .statusCode(HttpStatus.BAD_REQUEST.value());
        }
    }

    @Test
    void givenValidAdminCreds_whenFindAllCustomers_thenReturnListOfCustomersDtoList() {
        //GIVEN
        String token = authenticationService.encode(admin.getEmail(), admin.getPassword());
        Address newAddress = addressRepository.save(new Address("street","number","city","state","country"));
        Member newCustomer = memberRepository.save(new Member("newcustomer","newcustomer","newcustomer@yahoo.com","pass","+32195833502", MemberRole.CUSTOMER,newAddress.getId()));
        //EXPECTED RESULT
        List<MemberDtoOutput> expectedResult = memberService.findAllCustomers();

        MemberDtoOutput[] result = given()
                .header("Authorization", token)
                .when()
                .get("/members")
                .then()
                .statusCode(HttpStatus.OK.value())
                .extract()
                .as(MemberDtoOutput[].class);

        assertThat(List.of(result)).isEqualTo(expectedResult);
    }

    @Test
    void givenValidAdminCreds_whenFindCustomerById_thenReturnCustomerDto() {
        //GIVEN
        String token = authenticationService.encode(admin.getEmail(), admin.getPassword());
        //EXPECTED RESULT
        MemberDtoOutput expectedResult = memberMapper.memberToOutput(customer,address);

        MemberDtoOutput result = given()
                .header("Authorization", token)
                .when()
                .get("/members/"+customer.getId())
                .then()
                .statusCode(HttpStatus.OK.value())
                .extract()
                .as(MemberDtoOutput.class);

        assertThat(result).isEqualTo(expectedResult);
    }

    @Test
    void givenInvalidAdminCreds_whenFindCustomerById_thenReturnStatus401() {
        //GIVEN
        String token = "invalid";

        given()
                .header("Authorization", token)
                .when()
                .get("/members/"+customer.getId())
                .then()
                .statusCode(HttpStatus.UNAUTHORIZED.value());
    }

    @Test
    void givenInvalidAdminCreds_whenFindAllCustomers_thenReturnStatus401() {
        //GIVEN
        String token = "invalid";

        given()
                .header("Authorization", token)
                .when()
                .get("/members")
                .then()
                .statusCode(HttpStatus.UNAUTHORIZED.value());
    }
}
