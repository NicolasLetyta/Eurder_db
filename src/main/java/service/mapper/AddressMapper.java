package service.mapper;

import domain.Address;
import org.springframework.stereotype.Component;
import webapi.dto.AddressDtoInput;
import webapi.dto.AddressDtoOutput;

@Component
public class AddressMapper {

    public Address inputToAddress(AddressDtoInput addressDtoInput) {
        return new Address(addressDtoInput.getStreet(),
                addressDtoInput.getStreetNumber(),
                addressDtoInput.getPostalCode(),
                addressDtoInput.getCity(),
                addressDtoInput.getCountry());
    }

    public AddressDtoOutput addressToOutput(Address address) {
        return new AddressDtoOutput(address.getId(),
                address.getStreet(),
                address.getStreetNumber(),
                address.getPostalCode(),
                address.getCity(),
                address.getCountry());
    }
}
