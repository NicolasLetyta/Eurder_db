package service.mapper;

import domain.Address;
import domain.Member;
import domain.MemberRole;
import org.springframework.stereotype.Component;
import webapi.dto.AddressDtoOutput;
import webapi.dto.MemberDtoInput;
import webapi.dto.MemberDtoOutput;

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
