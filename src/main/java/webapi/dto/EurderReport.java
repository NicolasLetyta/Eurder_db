package webapi.dto;

import java.util.List;

public class EurderReport {
    private List<EurderDtoOutput> eurderDtoList;
    private double totalPrice;

    public EurderReport(List<EurderDtoOutput> eurderDtoList, double totalPrice) {
        this.eurderDtoList = eurderDtoList;
        this.totalPrice = totalPrice;
    }

    public List<EurderDtoOutput> getEurderDtoList() {
        return eurderDtoList;
    }

    public double getTotalPrice() {
        return totalPrice;
    }
}
