package service.mapper;

import domain.Item;
import org.springframework.stereotype.Component;
import webapi.dto.ItemDtoInput;
import webapi.dto.ItemDtoOutput;

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
