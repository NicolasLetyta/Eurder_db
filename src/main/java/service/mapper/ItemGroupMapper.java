package service.mapper;

import domain.Eurder;
import domain.Item;
import domain.ItemGroup;
import org.springframework.stereotype.Component;
import webapi.dto.ItemGroupDtoInput;
import webapi.dto.ItemGroupDtoOutput;

import java.time.LocalDate;

@Component
public class ItemGroupMapper {

    public ItemGroup inputToItemGroup(ItemGroupDtoInput itemGroupDtoInput,
                                      LocalDate shippingDate,
                                      Item item,
                                      Eurder eurder) {
        return new ItemGroup(shippingDate,
                itemGroupDtoInput.getQuantity(),
                item,
                eurder);
    }

    public ItemGroupDtoOutput itemGroupToOutput(ItemGroup itemGroup) {
        return new ItemGroupDtoOutput(itemGroup.getId(),
                itemGroup.getItem().getName(),
                itemGroup.getItem().getDescription(),
                itemGroup.getQuantity(),
                itemGroup.getSubtotalPrice(),
                itemGroup.getEurder().getId());
    }
}
