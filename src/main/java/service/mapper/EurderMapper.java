package service.mapper;

import domain.Eurder;
import webapi.dto.EurderDtoOutput;
import webapi.dto.ItemGroupDtoOutput;

import java.util.List;
import java.util.stream.Collectors;

public class EurderMapper {
    private ItemGroupMapper itemGroupMapper;
    public EurderMapper(ItemGroupMapper itemGroupMapper) {
        this.itemGroupMapper = itemGroupMapper;
    }

    public EurderDtoOutput EurderToOutput(Eurder eurder, String memberName) {
        List<ItemGroupDtoOutput> itemGroupDtoOutputList = eurder.getItemGroups().stream()
                .map(i->itemGroupMapper.itemGroupToOutput(i))
                .toList();
        return new EurderDtoOutput(eurder.getId(),
                memberName,
                eurder.getMemberId(),
                eurder.getStatus().name(),
                itemGroupDtoOutputList,
                eurder.getEurderPrice());
    }
}
