package com.switchfully.apps.eurder_db.service.mapper;

import com.switchfully.apps.eurder_db.domain.Address;
import com.switchfully.apps.eurder_db.domain.Member;
import com.switchfully.apps.eurder_db.domain.MemberRole;
import org.springframework.stereotype.Component;
import com.switchfully.apps.eurder_db.webapi.dto.MemberDtoInput;
import com.switchfully.apps.eurder_db.webapi.dto.MemberDtoOutput;

@Component
public class MemberMapper {
    private AddressMapper addressMapper;
    public MemberMapper(AddressMapper addressMapper) {
        this.addressMapper = addressMapper;
    }

    public Member inputToMember(MemberDtoInput memberDtoInput, Long addressId) {
        return new Member(memberDtoInput.getFirstName(),
                memberDtoInput.getLastName(),
                memberDtoInput.getEmail(),
                memberDtoInput.getPassword(),
                memberDtoInput.getPhone(),
                MemberRole.CUSTOMER,
                addressId);
    }

    public MemberDtoOutput memberToOutput(Member member, Address address) {
        return new MemberDtoOutput(member.getId(),
                member.getFullName(),
                member.getEmail(),
                member.getMemberRole(),
                addressMapper.addressToOutput(address));
    }
}
