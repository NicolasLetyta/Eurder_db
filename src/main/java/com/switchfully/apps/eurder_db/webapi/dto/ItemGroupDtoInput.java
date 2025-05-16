package com.switchfully.apps.eurder_db.webapi.dto;

public class ItemGroupDtoInput {
    private Long itemId;
    private int quantity;

    public ItemGroupDtoInput(Long itemId, int quantity) {
        this.itemId = itemId;
        this.quantity = quantity;
    }

    public Long getItemId() {
        return itemId;
    }

    public int getQuantity() {
        return quantity;
    }
}
