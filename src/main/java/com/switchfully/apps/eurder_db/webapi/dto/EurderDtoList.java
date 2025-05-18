package com.switchfully.apps.eurder_db.webapi.dto;

import com.switchfully.apps.eurder_db.domain.EurderStatus;

import java.util.List;

public class EurderDtoList {
    private Long id;
    private List<ItemGroupDtoList> itemGroups;
    private double totalPrice;

    public EurderDtoList(Long id, List<ItemGroupDtoList> itemGroups, double totalPrice) {
        this.id = id;
        this.itemGroups = itemGroups;
        this.totalPrice = totalPrice;
    }

    public Long getId() {
        return id;
    }

    public List<ItemGroupDtoList> getItemGroups() {
        return itemGroups;
    }

    public double getTotalPrice() {
        return totalPrice;
    }

    @Override
    public String toString() {
        return this.id+ " " +this.itemGroups + " " + this.totalPrice;
    }
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        EurderDtoList that = (EurderDtoList) o;
        return this.id.equals(that.id) &&
                this.itemGroups.equals(that.itemGroups) &&
                this.totalPrice == that.totalPrice;
    }
}

