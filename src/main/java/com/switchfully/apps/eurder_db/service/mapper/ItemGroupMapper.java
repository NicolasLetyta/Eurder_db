package com.switchfully.apps.eurder_db.service.mapper;

import com.switchfully.apps.eurder_db.domain.Eurder;
import com.switchfully.apps.eurder_db.domain.Item;
import com.switchfully.apps.eurder_db.domain.ItemGroup;
import com.switchfully.apps.eurder_db.webapi.dto.ItemGroupDtoList;
import org.springframework.stereotype.Component;
import com.switchfully.apps.eurder_db.webapi.dto.ItemGroupDtoInput;
import com.switchfully.apps.eurder_db.webapi.dto.ItemGroupDtoOutput;

@Component
public class ItemGroupMapper {

    public ItemGroup inputToItemGroup(ItemGroupDtoInput itemGroupDtoInput,
                                      Item item,
                                      Eurder eurder) {
        return new ItemGroup(itemGroupDtoInput.getQuantity(),
                item,
                eurder);
    }

    public ItemGroupDtoOutput itemGroupToOutputCart(ItemGroup itemGroup) {
        return new ItemGroupDtoOutput(itemGroup.getId(),
                itemGroup.getItem().getName(),
                itemGroup.getItem().getDescription(),
                itemGroup.getQuantity(),
                itemGroup.calculateCurrentSubtotalPrice(),
                itemGroup.getEurder().getId());
    }

    public ItemGroupDtoOutput itemGroupToOutputFinalized(ItemGroup itemGroup) {
        return new ItemGroupDtoOutput(itemGroup.getId(),
                itemGroup.getItem().getName(),
                itemGroup.getItem().getDescription(),
                itemGroup.getQuantity(),
                itemGroup.getTotalPriceAtEurderDate(),
                itemGroup.getEurder().getId());
    }

    public ItemGroupDtoList itemGroupToDtoListFinalized(ItemGroup itemGroup) {
        return new ItemGroupDtoList(itemGroup.getItem().getName(),
                itemGroup.getQuantity(),
                itemGroup.getTotalPriceAtEurderDate());
    }

    public ItemGroupDtoList itemGroupToDtoListCart(ItemGroup itemGroup) {
        return new ItemGroupDtoList(itemGroup.getItem().getName(),
                itemGroup.getQuantity(),
                itemGroup.calculateCurrentSubtotalPrice());
    }
}
