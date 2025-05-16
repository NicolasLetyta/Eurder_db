package com.switchfully.apps.eurder_db.repository;

import com.switchfully.apps.eurder_db.domain.Item;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.annotation.DirtiesContext;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@DataJpaTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
public class ItemRepositroyTest {
    @Autowired
    private ItemRepository itemRepository;

    @PersistenceContext
    private EntityManager entityManager;

    private Item item1;
    private Item item2;
    private Item item3;

    long lastItemId;

    @BeforeEach
    void setUp() {
        itemRepository.deleteAll();

        item1 = new Item("item1","this is item1",1.0,100);
        item2 = new Item("item2","this is item2",2.0,100);
        item3 = new Item("item3","this is item3",3.0,100);

        lastItemId = ((Number) entityManager.createNativeQuery("SELECT nextval('item_seq')").getSingleResult()).longValue();
    }

    @Test
    void givenCorrectItem_whenSaveItem_thenReturnItemFromDatabase() {
        Item result1 = itemRepository.save(item1);
        Item result2 = itemRepository.save(item2);
        Item result3 = itemRepository.save(item3);

        assertThat(result1.getId()).isEqualTo(lastItemId+1);
        assertThat(result2.getId()).isEqualTo(lastItemId+2);
        assertThat(result3.getId()).isEqualTo(lastItemId+3);

        assertThat(result1).isEqualTo(item1);
        assertThat(result2).isEqualTo(item2);
        assertThat(result3).isEqualTo(item3);
    }

    @Test
    void givenIllegalArguments_whenSave_thenThrowException(){
        Item nameNull = new Item(null,"description",1.0,100);
        Item priceNegative = new Item("priceNegative","description",-1,100);
        Item stockNegative = new Item("stockNegative","description",10,-1);
        Item nameIdentical = new Item("stockNegative","description",10,100);


        assertThrows(RuntimeException.class, () -> itemRepository.saveAndFlush(nameNull));
        assertThrows(RuntimeException.class, () -> itemRepository.saveAndFlush(priceNegative));
        assertThrows(RuntimeException.class, () -> itemRepository.saveAndFlush(stockNegative));
        assertThrows(RuntimeException.class, () -> itemRepository.saveAndFlush(nameIdentical));
    }

    @Test
    void givenItemExistsInDatabase_whenFindItemById_thenReturnItemFromDatabase() {
        Item savedItem = itemRepository.saveAndFlush(item1);

        Item result = itemRepository.findById(savedItem.getId()).orElse(null);

        assertThat(result).isEqualTo(savedItem);
        assertThat(result.getDescription()).isEqualTo(savedItem.getDescription());
    }

    @Test
    void givenItemExistsInDatabase_whenExistsByName_thenReturnTrue() {
        Item savedItem = itemRepository.saveAndFlush(item3);

        boolean result = itemRepository.existsByName(savedItem.getName());

        assertThat(result).isTrue();
    }

    @Test
    void givenItemNotInDatabase_whenExistsByName_thenReturnTrue() {
        itemRepository.saveAndFlush(item1);
        itemRepository.saveAndFlush(item2);
        itemRepository.saveAndFlush(item3);

        boolean result = itemRepository.existsByName("wrongName");

        assertThat(result).isFalse();
    }


}
