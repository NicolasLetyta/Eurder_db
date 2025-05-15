package service.mapper;

import domain.Eurder;
import org.springframework.stereotype.Component;
import webapi.dto.EurderDtoOutput;
import webapi.dto.EurderReport;
import webapi.dto.ItemGroupDtoOutput;

import java.util.List;
import java.util.stream.Collectors;

@Component
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

    public EurderDtoOutput EurderToDtoList(Eurder eurder) {
        List<ItemGroupDtoOutput> itemGroupDtoList = eurder.getItemGroups().stream()
                .map(i->itemGroupMapper.itemGroupToDtoList(i))
                .toList();
        return new EurderDtoOutput(eurder.getId(),
                null,
                null,
                null,
                itemGroupDtoList,
                eurder.getEurderPrice());
    }

}
