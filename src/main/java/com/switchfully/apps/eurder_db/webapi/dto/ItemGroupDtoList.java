package com.switchfully.apps.eurder_db.webapi.dto;

public class ItemGroupDtoList {
    private String itemName;
    private int quantity;
    private double subtotalPrice;

    public ItemGroupDtoList(String itemName, int quantity, double subtotalPrice) {
        this.itemName = itemName;
        this.quantity = quantity;
        this.subtotalPrice = subtotalPrice;
    }

    public String getItemName() {
        return itemName;
    }

    public int getQuantity() {
        return quantity;
    }

    public double getSubtotalPrice() {
        return subtotalPrice;
    }

    @Override
    public String toString() {
        return this.itemName +" " + this.quantity + " " + this.subtotalPrice;
    }
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ItemGroupDtoList that = (ItemGroupDtoList) o;
        return this.itemName.equals(that.itemName) &&
                this.quantity==that.quantity &&
                this.subtotalPrice==that.subtotalPrice;
    }
}

