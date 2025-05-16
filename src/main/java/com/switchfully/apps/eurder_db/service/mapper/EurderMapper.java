package com.switchfully.apps.eurder_db.service.mapper;

import com.switchfully.apps.eurder_db.domain.Eurder;
import org.springframework.stereotype.Component;
import com.switchfully.apps.eurder_db.webapi.dto.EurderDtoOutput;
import com.switchfully.apps.eurder_db.webapi.dto.ItemGroupDtoOutput;

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
                eurder.getStatus().name(),
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
                eurder.getStatus().name(),
                itemGroupDtoOutputList,
                eurder.calculateEurderPriceFinalized());
    }

    public EurderDtoOutput eurderToDtoReport(Eurder eurder) {
        List<ItemGroupDtoOutput> itemGroupsDtoFinalized = eurder.getItemGroups().stream()
                .map(i->itemGroupMapper.itemGroupToDtoReport(i))
                .toList();
        double totalEurderPriceFinalized = itemGroupsDtoFinalized.stream()
                .mapToDouble(ItemGroupDtoOutput::getSubtotalPrice)
                .sum();
        return new EurderDtoOutput(eurder.getId(),
                null,
                null,
                null,
                itemGroupsDtoFinalized,
                totalEurderPriceFinalized);
    }

}
