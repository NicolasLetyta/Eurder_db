package com.switchfully.apps.eurder_db.service.mapper;

import com.switchfully.apps.eurder_db.domain.Eurder;
import com.switchfully.apps.eurder_db.webapi.dto.*;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class EurderMapper {
    private ItemGroupMapper itemGroupMapper;
    public EurderMapper(ItemGroupMapper itemGroupMapper) {
        this.itemGroupMapper = itemGroupMapper;
    }

    public EurderDtoOutput eurderToOutputCart(Eurder eurder, String memberName) {
        List<ItemGroupDtoOutput> itemGroupDtoOutputList = eurder.getItemGroups().stream()
                .map(i->itemGroupMapper.itemGroupToOutputCart(i))
                .toList();
        return new EurderDtoOutput(eurder.getId(),
                memberName,
                eurder.getMemberId(),
                eurder.getStatus(),
                itemGroupDtoOutputList,
                eurder.calculateEurderPrice());
    }

    public EurderDtoOutput eurderToOutputFinalized(Eurder eurder, String memberName) {
        List<ItemGroupDtoOutput> itemGroupDtoOutputList = eurder.getItemGroups().stream()
                .map(i->itemGroupMapper.itemGroupToOutputFinalized(i))
                .toList();

        return new EurderDtoOutput(eurder.getId(),
                memberName,
                eurder.getMemberId(),
                eurder.getStatus(),
                itemGroupDtoOutputList,
                eurder.calculateEurderPriceFinalized());
    }

    public EurderDtoList eurderToDtoListFinalized(Eurder eurder) {
        List<ItemGroupDtoList> itemGroupsDtoList = eurder.getItemGroups().stream()
                .map(i->itemGroupMapper.itemGroupToDtoListFinalized(i))
                .toList();
        double totalEurderPriceFinalized = itemGroupsDtoList.stream()
                .mapToDouble(ItemGroupDtoList::getSubtotalPrice)
                .sum();

        return new EurderDtoList(eurder.getId(),
                itemGroupsDtoList,
                totalEurderPriceFinalized);
    }

    public EurderDtoList eurderToDtoListCart(Eurder eurder) {
        List<ItemGroupDtoList> itemGroupsDtoList = eurder.getItemGroups().stream()
                .map(itemGroupMapper::itemGroupToDtoListCart)
                .toList();

        double totalEurderPriceCart = itemGroupsDtoList.stream()
                .mapToDouble(ItemGroupDtoList::getSubtotalPrice)
                .sum();

        return new EurderDtoList(eurder.getId(),
                itemGroupsDtoList,
                totalEurderPriceCart);
    }

    public EurderDtoReport eurdersToDtoReport(List<Eurder> eurders) {
        List<EurderDtoList> eurdersDtoList = eurders.stream()
                .map(this::eurderToDtoListFinalized)
                .toList();

        double totalReportPrice = eurdersDtoList.stream()
                .mapToDouble(EurderDtoList::getTotalPrice)
                .sum();

        return new EurderDtoReport(eurdersDtoList, totalReportPrice);
    }

}
