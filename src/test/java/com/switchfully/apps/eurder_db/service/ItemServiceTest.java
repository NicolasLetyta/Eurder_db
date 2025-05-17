package com.switchfully.apps.eurder_db.service;

import com.switchfully.apps.eurder_db.domain.Item;
import com.switchfully.apps.eurder_db.exception.InvalidInputException;
import com.switchfully.apps.eurder_db.repository.ItemRepository;
import com.switchfully.apps.eurder_db.service.mapper.ItemMapper;
import com.switchfully.apps.eurder_db.webapi.dto.ItemDtoInput;
import com.switchfully.apps.eurder_db.webapi.dto.ItemDtoOutput;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@AutoConfigureTestDatabase
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
public class ItemServiceTest {
    @Autowired
    private ItemRepository itemRepository;
    @Autowired
    private ItemMapper itemMapper;
    @Autowired
    private ItemService itemService;

    Item item1, item2, item3;

    long lastItemId;

    @BeforeAll
    void beforeAll() {
        itemRepository.deleteAll();
    }

    @BeforeEach
    void beforeEach() {
        itemRepository.deleteAll();
        item1 = itemRepository.save(new Item("item1","this is item1",1.0,100));
        item2 = itemRepository.save(new Item("item2","this is item2",2.0,500));
        item3 = itemRepository.save(new Item("item3","this is item3",5.0,250));

        lastItemId = item3.getId();
    }

    @Test
    void givenCorrectItemDtoInput_whenAddItem_thenReturnCorrectItemDto() {
        ItemDtoInput itemDtoInput = new ItemDtoInput("new item","this is new item",1.0,100);
        ItemDtoOutput expectedResult = new ItemDtoOutput(lastItemId+1,"new item","this is new item",1.0,100);

        ItemDtoOutput result = itemService.addItem(itemDtoInput);

        assertThat(result).isEqualTo(expectedResult);
    }

    @Test
    void givenInvalidItemDtoInput_whenAddItem_thenThrowException() {
        ItemDtoInput nullName = new ItemDtoInput(null,"this is a new item",1.0,100);
        ItemDtoInput negativePrice = new ItemDtoInput("new item1","this is a new item",-1.0,100);
        ItemDtoInput negativeStock = new ItemDtoInput("new item2","this is a new item",1.0,-100);
        ItemDtoInput duplicateName = new ItemDtoInput(item1.getName(),"this is a new item",1.0,100);

        assertThrows(InvalidInputException.class, () -> itemService.addItem(nullName));
        assertThrows(InvalidInputException.class, () -> itemService.addItem(negativePrice));
        assertThrows(InvalidInputException.class, () -> itemService.addItem(negativeStock));
        assertThrows(InvalidInputException.class, () -> itemService.addItem(duplicateName));
    }

    @Test
    void givenValidItemDtoInput_whenUpdateItem_thenReturnCorrectItemDto() {
        ItemDtoInput updateDtoInput = new ItemDtoInput(item1.getName()+" updated",null,500.0,5000);
        ItemDtoOutput expectedResult = new ItemDtoOutput(item1.getId(),item1.getName()+" updated",item1.getDescription(),500.0,5000);

        ItemDtoOutput result = itemService.updateItem(updateDtoInput,item1.getId());

        assertThat(result).isEqualTo(expectedResult);
    }

    @Test
    void givenInvalidItemDtoInput_whenUpdateItem_thenThrowException() {
        ItemDtoInput emptyInput = new ItemDtoInput(null,null,null,null);
        ItemDtoInput duplicateName = new ItemDtoInput(item1.getName(),null,null,null);
        ItemDtoInput negativePrice = new ItemDtoInput(null,null,-10.0,null);
        ItemDtoInput negativeStock = new ItemDtoInput(null,null,null,-1);

        assertThrows(InvalidInputException.class, () -> itemService.updateItem(emptyInput,item1.getId()));
        assertThrows(InvalidInputException.class, () -> itemService.updateItem(duplicateName,item1.getId()));
        assertThrows(InvalidInputException.class, () -> itemService.updateItem(negativePrice,item1.getId()));
        assertThrows(InvalidInputException.class, () -> itemService.updateItem(negativeStock,item1.getId()));
    }
}
