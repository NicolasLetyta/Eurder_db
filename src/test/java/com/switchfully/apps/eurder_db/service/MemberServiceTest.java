package com.switchfully.apps.eurder_db.service;

import com.switchfully.apps.eurder_db.domain.Address;
import com.switchfully.apps.eurder_db.domain.Member;
import com.switchfully.apps.eurder_db.domain.MemberRole;
import com.switchfully.apps.eurder_db.exception.InvalidInputException;
import com.switchfully.apps.eurder_db.repository.AddressRepository;
import com.switchfully.apps.eurder_db.repository.MemberRepository;
import com.switchfully.apps.eurder_db.service.mapper.AddressMapper;
import com.switchfully.apps.eurder_db.service.mapper.MemberMapper;
import com.switchfully.apps.eurder_db.webapi.dto.AddressDtoInput;
import com.switchfully.apps.eurder_db.webapi.dto.AddressDtoOutput;
import com.switchfully.apps.eurder_db.webapi.dto.MemberDtoInput;
import com.switchfully.apps.eurder_db.webapi.dto.MemberDtoOutput;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;


@SpringBootTest
@AutoConfigureTestDatabase
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
public class MemberServiceTest {
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

    Address address;

    Member member1,member2,member3;

    long lastMemberId;
    long lastAddressId;

    @BeforeAll
    void beforeAll() {
        addressRepository.deleteAll();
        memberRepository.deleteAll();
        address = addressRepository.save(new Address("street","number","city","state","country"));
    }

    @BeforeEach
    void beforeEach() {
        memberRepository.deleteAll();
        member1 = memberRepository.save(new Member("name1","name1","name1@gmail.com","pass","+3212345678", MemberRole.CUSTOMER, address.getId()));
        member2 = memberRepository.save(new Member("name2","name2","name2@gmail.com","pass","+3112345678", MemberRole.CUSTOMER, address.getId()));
        member3 = memberRepository.save(new Member("admin","admin","admin@gmail.com","pass","+3012345678", MemberRole.ADMIN, address.getId()));

        lastMemberId = member3.getId();
        lastAddressId = address.getId();
    }

    @Test
    void givenValidMemberDtoInput_whenRegisterAsCustomer_thenReturnCorrectMemberDto() {
        AddressDtoInput addressInput = new AddressDtoInput("street","number","city","state","country");
        MemberDtoInput memberInput = new MemberDtoInput("name3","name3","name3@gmail.com","pass",null,addressInput);

        AddressDtoOutput addressOutput = new AddressDtoOutput(lastAddressId+1,"street","number","city","state","country");
        MemberDtoOutput expectedResult = new MemberDtoOutput(lastMemberId+1,"name3 name3","name3@gmail.com",MemberRole.CUSTOMER,addressOutput);

        MemberDtoOutput result = memberService.registerAsCustomer(memberInput);

        assertThat(result).isEqualTo(expectedResult);
    }

    @Test
    void givenInvalidMemberDtoInput_whenRegisterAsCustomer_thenThrowsException() {
        AddressDtoInput addressInputCorrect = new AddressDtoInput("street","number","city","state","country");
        AddressDtoInput addressInputInvalid = new AddressDtoInput(null,"wrong",null,"state",null);

        MemberDtoInput nullFirstName = new MemberDtoInput(null,"name3","name3@gmail.com","pass","+3312345678",addressInputCorrect);
        MemberDtoInput nullLastName = new MemberDtoInput("name3",null,"name3@gmail.com","pass","+3312345678",addressInputCorrect);
        MemberDtoInput nullEmail = new MemberDtoInput("name3","name3",null,"pass","+3312345678",addressInputCorrect);
        MemberDtoInput nullPass = new MemberDtoInput("name3","name3","name3@gmail.com",null,"+3312345678",addressInputCorrect);
        MemberDtoInput nullAddress = new MemberDtoInput("name3","name3","name3@gmail.com","pass","+3312345678",null);
        MemberDtoInput invalidAddress = new MemberDtoInput("name3","name3","name3@gmail.com","pass","+3312345678",addressInputInvalid);
        MemberDtoInput duplicateEmail = new MemberDtoInput("name3","name3",member1.getEmail(),"pass","+3312345678",addressInputInvalid);
        MemberDtoInput duplicatePhone = new MemberDtoInput("name3","name3","name3@gmail.com","pass",member1.getPhone(),addressInputInvalid);

        assertThrows(InvalidInputException.class,()->memberService.registerAsCustomer(nullFirstName));
        assertThrows(InvalidInputException.class,()->memberService.registerAsCustomer(nullLastName));
        assertThrows(InvalidInputException.class,()->memberService.registerAsCustomer(nullEmail));
        assertThrows(InvalidInputException.class,()->memberService.registerAsCustomer(nullPass));
        assertThrows(InvalidInputException.class,()->memberService.registerAsCustomer(nullAddress));
        assertThrows(InvalidInputException.class,()->memberService.registerAsCustomer(invalidAddress));
        assertThrows(InvalidInputException.class,()->memberService.registerAsCustomer(duplicateEmail));
        assertThrows(InvalidInputException.class,()->memberService.registerAsCustomer(duplicatePhone));
    }

    @Test
    void givenTwoCustomersExistsInDatabase_whenFindAllCustomers_thenReturnTwoCustomersDto() {
        List<MemberDtoOutput> result = memberService.findAllCustomers();

        assertThat(result.size()).isEqualTo(2);
        assertThat(result).contains(memberMapper.memberToOutput(member1,address),memberMapper.memberToOutput(member2,address));
    }

    @Test
    void givenMemberIdIsFoundInDatabase_whenFindById_thenReturnMemberDto() {
        MemberDtoOutput expectedResult = memberMapper.memberToOutput(member2,address);

        MemberDtoOutput result = memberService.findCustomerById(member2.getId());

        assertThat(result).isEqualTo(expectedResult);
    }

    @Test
    void givenMemberIdIsNotFoundInDatabase_whenFindById_thenThrowsException() {
        assertThrows(InvalidInputException.class,()->memberService.findCustomerById(600L));
    }


}
