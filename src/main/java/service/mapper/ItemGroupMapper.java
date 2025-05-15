package service.mapper;

import domain.Eurder;
import domain.Item;
import domain.ItemGroup;
import org.springframework.stereotype.Component;
import webapi.dto.ItemGroupDtoInput;
import webapi.dto.ItemGroupDtoOutput;

@Component
public class ItemGroupMapper {

    public ItemGroup inputToItemGroup(ItemGroupDtoInput itemGroupDtoInput,
                                      Item item,
                                      Eurder eurder) {
        return new ItemGroup(itemGroupDtoInput.getQuantity(),
                item,
                eurder);
    }

    public ItemGroupDtoOutput itemGroupToOutput(ItemGroup itemGroup) {
        return new ItemGroupDtoOutput(itemGroup.getId(),
                itemGroup.getItem().getName(),
                itemGroup.getItem().getDescription(),
                itemGroup.getQuantity(),
                itemGroup.calculateCurrentSubtotalPrice(),
                itemGroup.getEurder().getId());
    }

    public ItemGroupDtoOutput itemGroupToDtoList(ItemGroup itemGroup) {
        return new ItemGroupDtoOutput(null,
                itemGroup.getItem().getName(),
                null,
                itemGroup.getQuantity(),
                itemGroup.calculateCurrentSubtotalPrice(),
                null);
    }
}
