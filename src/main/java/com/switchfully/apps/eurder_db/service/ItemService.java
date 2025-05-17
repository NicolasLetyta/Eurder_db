package com.switchfully.apps.eurder_db.service;

import com.switchfully.apps.eurder_db.domain.Item;
import com.switchfully.apps.eurder_db.exception.InvalidInputException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.switchfully.apps.eurder_db.repository.ItemRepository;
import com.switchfully.apps.eurder_db.service.mapper.ItemMapper;
import com.switchfully.apps.eurder_db.webapi.dto.ItemDtoInput;
import com.switchfully.apps.eurder_db.webapi.dto.ItemDtoOutput;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static com.switchfully.apps.eurder_db.utility.Validation.*;

@Service
@Transactional
public class ItemService {
    private final ItemRepository itemRepository;
    private final ItemMapper itemMapper;
    public ItemService(ItemRepository itemRepository,
                       ItemMapper itemMapper) {
        this.itemRepository = itemRepository;
        this.itemMapper = itemMapper;
    }

    public ItemDtoOutput addItem(ItemDtoInput itemDtoInput) {
        Item item = itemRepository.save(itemMapper.inputToItem(validateItemInputCreate(itemDtoInput)));
        return itemMapper.itemToOutput(item);
    }

    public ItemDtoOutput updateItem(ItemDtoInput itemDtoInput, Long itemId) {
        validateItemInputUpdate(itemDtoInput, itemId);
        Item item = itemRepository.findById(itemId).orElseThrow(() -> new InvalidInputException("Item id not found"));

        Optional.ofNullable(itemDtoInput.getName()).ifPresent(item::setName);
        Optional.ofNullable(itemDtoInput.getDescription()).ifPresent(item::setDescription);
        Optional.ofNullable(itemDtoInput.getPrice()).ifPresent(item::setPrice);
        Optional.ofNullable(itemDtoInput.getStock()).ifPresent(item::setStock);

        return itemMapper.itemToOutput(itemRepository.save(item));
    }

//    public List<ItemDtoOutput> getItemOverView(String filter, String order) {
//        return null;
//    }

    private void validateItemInputUpdate(ItemDtoInput itemDtoInput, Long itemId) {
        validateArgumentWithBooleanCondition(itemId,"Item id cannot be null",
                itemId==null, InvalidInputException::new);

        if(itemDtoInput.getName()==null&&
                itemDtoInput.getDescription()==null&&
                itemDtoInput.getStock()==null&&
                itemDtoInput.getPrice()==null){
            throw new InvalidInputException("ItemInput must contain at least one field");
        }
        if(itemDtoInput.getName() != null) {
            validateNonBlank(itemDtoInput.getName(),"Item name cannot be blank",InvalidInputException::new);
            validateArgument(itemDtoInput.getName(),"Item with exact name match already exists", itemRepository::existsByName, InvalidInputException::new);
        }
        if(itemDtoInput.getStock() != null) {
            validateArgument(itemDtoInput.getStock(),"Item stock cannot be negative",s->s<0,InvalidInputException::new);
        }
        if(itemDtoInput.getPrice() != null) {
            validateArgument(itemDtoInput.getPrice(),"Item price cannot be negative or 0",s->s<=0,InvalidInputException::new);
        }
    }

    private ItemDtoInput validateItemInputCreate(ItemDtoInput itemDtoInput) {
        validateNonBlank(itemDtoInput.getName(),"Item name cannot be blank",InvalidInputException::new);
        validateArgument(itemDtoInput.getName(),"Item with exact name match already exists", itemRepository::existsByName, InvalidInputException::new);
        validateArgument(itemDtoInput.getPrice(),"Item price cannot be null", Objects::isNull,InvalidInputException::new);
        validateArgument(itemDtoInput.getPrice(),"Item price cannot be negative or 0",s->s<=0,InvalidInputException::new);
        validateArgument(itemDtoInput.getStock(),"Item stock cannot be null", Objects::isNull,InvalidInputException::new);
        validateArgument(itemDtoInput.getStock(),"Item stock cannot be negative",s->s<0,InvalidInputException::new);
        return itemDtoInput;
    }


}
