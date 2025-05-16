package com.switchfully.apps.eurder_db.service;

import com.switchfully.apps.eurder_db.domain.Address;
import com.switchfully.apps.eurder_db.domain.Member;
import com.switchfully.apps.eurder_db.domain.MemberRole;
import com.switchfully.apps.eurder_db.exception.InvalidInputException;
import org.apache.commons.validator.routines.EmailValidator;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.switchfully.apps.eurder_db.repository.AddressRepository;
import com.switchfully.apps.eurder_db.repository.MemberRepository;
import com.switchfully.apps.eurder_db.service.mapper.AddressMapper;
import com.switchfully.apps.eurder_db.service.mapper.MemberMapper;
import com.switchfully.apps.eurder_db.webapi.dto.AddressDtoInput;
import com.switchfully.apps.eurder_db.webapi.dto.MemberDtoInput;
import com.switchfully.apps.eurder_db.webapi.dto.MemberDtoOutput;

import java.util.List;
import java.util.Objects;

import static com.switchfully.apps.eurder_db.utility.Validation.*;

@Service
@Transactional
public class MemberService {
    private final MemberRepository memberRepository;
    private final MemberMapper memberMapper;
    private final AddressRepository addressRepository;
    private final AddressMapper addressMapper;

    private final String phoneRegex = "^\\+\\d{1,3}[-\\s()]*\\d{6,14}([-\\s()]*\\d{1,4})?$";


    public MemberService(MemberRepository memberRepository,
                         MemberMapper memberMapper,
                         AddressRepository addressRepository,
                         AddressMapper addressMapper) {
        this.memberRepository = memberRepository;
        this.memberMapper = memberMapper;
        this.addressRepository = addressRepository;
        this.addressMapper = addressMapper;
    }

    public MemberDtoOutput registerAsCustomer(MemberDtoInput memberDtoInput) {
        validateMemberInput(memberDtoInput);
        AddressDtoInput addressDtoInput = validateAddressInput(memberDtoInput.getAddressInput());

        Address address = addressRepository.save(addressMapper.inputToAddress(addressDtoInput));
        Member customer = memberRepository.save(memberMapper.inputToMember(memberDtoInput,address.getId()));
        return memberMapper.memberToOutput(customer, address);
    }

    public List<MemberDtoOutput> findAllCustomers() {
        List<Member> members = memberRepository.findByMemberRole(MemberRole.CUSTOMER);
        return members.stream()
                .map(m->memberMapper.memberToOutput(m,addressRepository.findById(m.getAddressId()).get()))
                .toList();
    }

    public MemberDtoOutput findCustomerById(Long id) {
        validateArgumentWithBooleanCondition(id,"MemberId not found in repo", !memberRepository.existsById(id), InvalidInputException::new);
        Member member = memberRepository.findById(id).get();
        Address address = addressRepository.findById(member.getAddressId()).get();
        //address id can never be null in member
        return memberMapper.memberToOutput(member,address);
    }

    private MemberDtoInput validateMemberInput(MemberDtoInput memberDtoInput) {
        validateNonBlank(memberDtoInput.getFirstName(),"First name cannot be empty",InvalidInputException::new);
        validateNonBlank(memberDtoInput.getLastName(),"Last name cannot be empty",InvalidInputException::new);
        validateNonBlank(memberDtoInput.getEmail(),"Email cannot be empty",InvalidInputException::new);
        validateNonBlank(memberDtoInput.getPassword(),"password be empty",InvalidInputException::new);
        validateNonBlank(memberDtoInput.getEmail(),"Email cannot be empty",InvalidInputException::new);
        validateArgument(memberDtoInput.getAddressInput(),"AddressInput cannot be null", Objects::isNull,InvalidInputException::new);

        validateArgument(memberDtoInput.getEmail(),"Invalid email format",e->!EmailValidator.getInstance().isValid(e),InvalidInputException::new);
        validateArgument(memberDtoInput.getEmail(),"Email already in repository",e->memberRepository.existsByEmail(e),InvalidInputException::new);
        validateArgument(memberDtoInput.getPhone(),"Invalid phoneNumber format",p->!p.matches(phoneRegex),InvalidInputException::new);

        return memberDtoInput;
    }

    private AddressDtoInput validateAddressInput(AddressDtoInput addressDtoInput) {
        validateNonBlank(addressDtoInput.getStreet(),"Street name cannot be empty",InvalidInputException::new);
        validateNonBlank(addressDtoInput.getPostalCode(),"Postal code cannot be empty",InvalidInputException::new);
        validateNonBlank(addressDtoInput.getCity(),"City cannot be empty",InvalidInputException::new);
        validateNonBlank(addressDtoInput.getCountry(),"Country cannot be empty",InvalidInputException::new);
        return addressDtoInput;
    }
}
