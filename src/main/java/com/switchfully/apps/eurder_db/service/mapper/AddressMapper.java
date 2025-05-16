package com.switchfully.apps.eurder_db.service.mapper;

import com.switchfully.apps.eurder_db.domain.Address;
import org.springframework.stereotype.Component;
import com.switchfully.apps.eurder_db.webapi.dto.AddressDtoInput;
import com.switchfully.apps.eurder_db.webapi.dto.AddressDtoOutput;

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
