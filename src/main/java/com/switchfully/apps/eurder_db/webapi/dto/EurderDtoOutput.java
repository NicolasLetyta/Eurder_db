package com.switchfully.apps.eurder_db.webapi.dto;

import com.switchfully.apps.eurder_db.domain.EurderStatus;

import java.util.List;

public class EurderDtoOutput {
    private Long id;
    private String memberName;
    private Long memberId;
    private String eurderStatus;
    private List<ItemGroupDtoOutput> itemGroups;
    private double totalPrice;

    public EurderDtoOutput(Long id, String memberName, Long memberId, EurderStatus eurderStatus, List<ItemGroupDtoOutput> itemGroups, double totalPrice) {
        this.id = id;
        this.memberName = memberName;
        this.memberId = memberId;
        this.eurderStatus = eurderStatus.toString();
        this.itemGroups = itemGroups;
        this.totalPrice = totalPrice;
    }

    public Long getId() {
        return id;
    }

    public String getMemberName() {
        return memberName;
    }

    public Long getMemberId() {
        return memberId;
    }

    public String getEurderStatus() {
        return eurderStatus;
    }

    public List<ItemGroupDtoOutput> getItemGroups() {
        return itemGroups;
    }

    public double getTotalPrice() {
        return totalPrice;
    }

    @Override
    public String toString() {
        return this.id + " " + this.memberName + " " + this.eurderStatus +" "+ this.itemGroups + " " + this.totalPrice;
    }
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        EurderDtoOutput that = (EurderDtoOutput) o;
        return this.id.equals(that.id) &&
                this.memberName.equals(that.memberName) &&
                this.eurderStatus.equals(that.eurderStatus) &&
                this.itemGroups.equals(that.itemGroups) &&
                this.totalPrice == that.totalPrice;
    }
}
