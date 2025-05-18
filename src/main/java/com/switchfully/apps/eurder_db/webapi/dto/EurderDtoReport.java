package com.switchfully.apps.eurder_db.webapi.dto;

import java.util.List;

public class EurderDtoReport {
    private List<EurderDtoList> eurderDtoList;
    private double totalPrice;

    public EurderDtoReport(List<EurderDtoList> eurderDtoList, double totalPrice) {
        this.eurderDtoList = eurderDtoList;
        this.totalPrice = totalPrice;
    }

    public List<EurderDtoList> getEurderDtoList() {
        return eurderDtoList;
    }

    public double getTotalPrice() {
        return totalPrice;
    }
    @Override
    public String toString() {
        return this.eurderDtoList.toString() + " \n" + this.totalPrice;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        EurderDtoReport that = (EurderDtoReport) o;
        return this.eurderDtoList.equals(that.eurderDtoList) &&
                this.totalPrice == that.totalPrice;
    }

}
