package com.switchfully.apps.eurder_db.service.mapper;

import com.switchfully.apps.eurder_db.domain.Item;
import org.springframework.stereotype.Component;
import com.switchfully.apps.eurder_db.webapi.dto.ItemDtoInput;
import com.switchfully.apps.eurder_db.webapi.dto.ItemDtoOutput;

@Component
public class ItemMapper {

    public Item inputToItem(ItemDtoInput itemDtoInput) {
        return new Item(itemDtoInput.getName(),
                itemDtoInput.getDescription(),
                itemDtoInput.getPrice(),
                itemDtoInput.getStock());
    }

    public ItemDtoOutput itemToOutput(Item item) {
        return new ItemDtoOutput(item.getId(),
                item.getName(),
                item.getDescription(),
                item.getPrice(),
                item.getStock());
    }
}
