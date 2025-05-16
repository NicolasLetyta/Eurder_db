package com.switchfully.apps.eurder_db.repository;

import com.switchfully.apps.eurder_db.domain.Address;
import com.switchfully.apps.eurder_db.domain.Member;
import com.switchfully.apps.eurder_db.domain.MemberRole;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.annotation.DirtiesContext;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@DataJpaTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
public class MemberRepositoryTest {
    @Autowired
    private MemberRepository memberRepository;
    @Autowired
    private AddressRepository addressRepository;

    @PersistenceContext
    private EntityManager entityManager;

    Member member1;
    Member member2;
    Member member3;

    Address address;

    @BeforeEach
    void setUp() {
        addressRepository.deleteAll();
        memberRepository.deleteAll();
        address = addressRepository.saveAndFlush(new Address("street","number","2000","city","country"));

        member1 = new Member("name1","name1","email1","pass","phone1", MemberRole.CUSTOMER, address.getId());
        member2 = new Member("name2","name2","email2","pass","phone2", MemberRole.CUSTOMER, address.getId());
        member3 = new Member("name3","name3","email3","pass","phone3", MemberRole.CUSTOMER, address.getId());
    }

    @Test
    void givenCorrectMember_whenSaveAndFlush_thenReturnMemberFromDatabase() {
        Member savedMember = memberRepository.saveAndFlush(member1);

        assertThat(savedMember).isEqualTo(member1);
    }

    @Test
    void givenIllegalArguments_whenSaveAndFlush_thenThrows() {
        Member nullName = new Member(null,null,"email4","pass","phone4", MemberRole.CUSTOMER, address.getId());
        Member nullEmail = new Member("name5","name5",null,"pass","phone5", MemberRole.CUSTOMER, address.getId());
        Member nullPass = new Member("name6","name6","email6",null,"phone6", MemberRole.CUSTOMER, address.getId());
        Member identicalPhone = new Member("name7","name7","email7","pass","phone1", MemberRole.CUSTOMER, address.getId());
        Member identicalEmail = new Member("name8","name8","email1","pass","phone8", MemberRole.CUSTOMER, address.getId());
        Member nullRole= new Member("name9","name9","email9","pass","phone9", null, address.getId());
        Member nullAddress = new Member("name10","name10","email10","pass","phone10", MemberRole.CUSTOMER, null);

        assertThrows(RuntimeException.class, () -> memberRepository.saveAndFlush(nullName));
        assertThrows(RuntimeException.class, () -> memberRepository.saveAndFlush(nullEmail));
        assertThrows(RuntimeException.class, () -> memberRepository.saveAndFlush(nullPass));
        assertThrows(RuntimeException.class, () -> memberRepository.saveAndFlush(nullRole));
        assertThrows(RuntimeException.class, () -> memberRepository.saveAndFlush(nullAddress));
        assertThrows(RuntimeException.class, () -> memberRepository.saveAndFlush(identicalPhone));
        assertThrows(RuntimeException.class, () -> memberRepository.saveAndFlush(identicalEmail));
    }

    @Test
    void givenMemberExistsInDatabase_whenFindMemberById_thenReturnMemberFromDatabase() {
        Member savedMember = memberRepository.saveAndFlush(member2);
        Member result = memberRepository.findById(savedMember.getId()).orElse(null);
        assertThat(result).isEqualTo(savedMember);
    }

    @Test
    void givenMemberNotInDatabase_whenFindByEmail_thenReturnNull() {
        memberRepository.saveAndFlush(member1);
        memberRepository.saveAndFlush(member2);
        memberRepository.saveAndFlush(member3);

        Member result = memberRepository.findByEmail("wrongEmail");

        assertThat(result).isNull();
    }

    @Test
    void givenThreeCustomersInDatabase_whenFindByMemberRole_thenReturnThreeCustomersFromDatabase() {
        memberRepository.saveAndFlush(member1);
        memberRepository.saveAndFlush(member2);
        memberRepository.saveAndFlush(member3);

        List<Member> result = memberRepository.findByMemberRole(MemberRole.CUSTOMER);
        assertThat(result).hasSize(3);
        assertThat(result.get(0)).isEqualTo(member1);
        assertThat(result.get(1)).isEqualTo(member2);
        assertThat(result.get(2)).isEqualTo(member3);
    }


}
