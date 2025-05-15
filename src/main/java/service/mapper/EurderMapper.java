package service.mapper;

import domain.Eurder;
import org.springframework.stereotype.Component;
import webapi.dto.EurderDtoOutput;
import webapi.dto.ItemGroupDtoOutput;

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

    public EurderDtoOutput EurderToOutputFinalized(Eurder eurder, String memberName) {
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

    public EurderDtoOutput EurderToDtoReport(Eurder eurder) {
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
