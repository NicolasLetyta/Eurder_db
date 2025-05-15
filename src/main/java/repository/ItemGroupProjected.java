package repository;

import domain.Item;
import webapi.dto.ItemGroupDtoInput;

public interface ItemGroupProjected {
    String getName();
    int getQuantity();

    default double getSubtotalPrice(double price) {
        return price * getQuantity();
    }
}
