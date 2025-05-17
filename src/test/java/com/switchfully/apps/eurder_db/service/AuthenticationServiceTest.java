package com.switchfully.apps.eurder_db.service;


import com.switchfully.apps.eurder_db.domain.Address;
import com.switchfully.apps.eurder_db.domain.Member;
import com.switchfully.apps.eurder_db.domain.MemberRole;
import com.switchfully.apps.eurder_db.exception.InvalidHeaderException;
import com.switchfully.apps.eurder_db.repository.AddressRepository;
import com.switchfully.apps.eurder_db.repository.MemberRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.annotation.DirtiesContext;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.Base64;

@DataJpaTest
@AutoConfigureTestDatabase
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
public class AuthenticationServiceTest {

    @Autowired
    private MemberRepository memberRepository;
    @Autowired
    private AddressRepository addressRepository;

    private AuthenticationService authenticationService;

    Member member1;
    Member member2;
    Member member3;

    Address address;

    @BeforeEach
    public void init() {
        memberRepository.deleteAll();
        addressRepository.deleteAll();
        authenticationService = new AuthenticationService(memberRepository);
        address = addressRepository.save(new Address("street","number","city","state","country"));
        member1 = memberRepository.save(new Member("name1","name1","name1@gmail.com","pass","+3212345678", MemberRole.CUSTOMER, address.getId()));
        member2 = memberRepository.save(new Member("name2","name2","name2@gmail.com","pass","+3112345678", MemberRole.CUSTOMER, address.getId()));
        member3 = memberRepository.save(new Member("admin","admin","admin@gmail.com","pass","+3012345678", MemberRole.ADMIN, address.getId()));
    }

    @Test
    void whenEncode_thenReturnToken() {
        String valueToEncode = member1.getEmail() + ":" + member1.getPassword();
        String expectedResult = "Basic " + Base64.getEncoder().encodeToString(valueToEncode.getBytes());

        String result = authenticationService.encode(member1.getEmail(), member1.getPassword());
        assertThat(result).isEqualTo(expectedResult);
    }

    @Test
    void givenValidToken_whenAuthenticateMember_thenReturnMember() {
        Member result = authenticationService.authenticateMember(authenticationService.encode(member1.getEmail(), member1.getPassword()));
        assertThat(result).isEqualTo(member1);
    }

    @Test
    void givenInvalidToken_whenAuthenticateMember_thenThrowException() {
        String invalidToken = "invalidToken";
        assertThrows(InvalidHeaderException.class, () -> authenticationService.authenticateMember(invalidToken));
    }

    @Test
    void givenValidToken_whenAuthenticateAdmin_thenReturnMember() {
        Member result = authenticationService.authenticateAdmin(authenticationService.encode(member3.getEmail(), member3.getPassword()));
        assertThat(result).isEqualTo(member3);
    }

    @Test
    void givenInvalidToken_whenAuthenticateAdmin_thenThrowException() {
        String invalidToken = authenticationService.encode(member1.getEmail(), member1.getPassword());
        assertThrows(InvalidHeaderException.class, () -> authenticationService.authenticateAdmin(invalidToken));
    }
}
