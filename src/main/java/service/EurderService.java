package service;

import domain.*;
import exception.InvalidInputException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import repository.EurderRepository;
import repository.ItemRepository;
import repository.MemberRepository;
import service.mapper.EurderMapper;
import service.mapper.ItemGroupMapper;
import webapi.dto.EurderDtoOutput;
import webapi.dto.EurderReport;
import webapi.dto.ItemGroupDtoInput;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
public class EurderService {
    private final EurderRepository eurderRepository;
    private final EurderMapper eurderMapper;
    private final ItemGroupMapper itemGroupMapper;
    private final MemberRepository memberRepository;
    private final ItemRepository itemRepository;
    public EurderService(EurderRepository eurderRepository,
                         EurderMapper eurderMapper,
                         ItemGroupMapper itemGroupMapper,
                         MemberRepository memberRepository,
                         ItemRepository itemRepository) {
        this.eurderRepository = eurderRepository;
        this.eurderMapper = eurderMapper;
        this.itemGroupMapper = itemGroupMapper;
        this.memberRepository = memberRepository;
        this.itemRepository = itemRepository;
    }

    public EurderDtoOutput addItemGroupToCart(ItemGroupDtoInput itemGroupDtoInput, Member member) {
        String fullName = member.getFullName();
        Item item = itemRepository.findById(itemGroupDtoInput.getItemId()).orElseThrow(()->new InvalidInputException("Item id not found in repository"));
        Eurder cart = createOrGetCart(member.getId());

        ItemGroup itemGroup = itemGroupMapper.inputToItemGroup(itemGroupDtoInput,
                item,
                cart);

        cart.addItemGroup(itemGroup);
        cart = eurderRepository.save(cart);
        return eurderMapper.EurderToOutputCart(cart,fullName);
    }

    public EurderDtoOutput finalizeEurder(Member member){
        Eurder eurder = createOrGetCart(member.getId());

        eurder.getItemGroups()
                .stream()// stream over all ItemGroups in the cart
                .collect(Collectors.groupingBy(ItemGroup::getItem))   // organize itemgroups in lists in a mpa with Item as key value -> Map<Item, List<ItemGroup>>
                .forEach((item, groups) -> {
                    int totalOrderQuantity = groups.stream()
                            .mapToInt(ItemGroup::getQuantity)
                            .sum();                      // total order quantity per item in this eurder

                    LocalDate shipDate = calculateShippingDate(item,totalOrderQuantity,LocalDate.now());
                    groups.forEach(g -> g.setTotalPriceAtEurderDate(g.getTotalPriceAtEurderDate()));//set the total itemgroup price at shipping date to todays prices
                    groups.forEach(g -> g.setShippingDate(shipDate));  // for each group set date according to total
                    //orderquantity for an item, not per itemGroup :)
                });

        eurder.setStatusFinalized();
        return eurderMapper.EurderToOutputFinalized(eurderRepository.save(eurder),member.getFullName());
    }

    public EurderReport getEurderReport(Long memberId) {
        List<EurderDtoOutput> eurderDtoList = eurderRepository.findAllByMemberIdAndStatus(memberId,EurderStatus.FINALIZED).stream()
                .map(eurderMapper::EurderToDtoReport)
                .toList();
        double totalPrice = eurderDtoList.stream()
                .mapToDouble(EurderDtoOutput::getTotalPrice)
                .sum();
        return new EurderReport(eurderDtoList,totalPrice);
    }

    private Eurder createOrGetCart(Long memberId) {
        Optional<Eurder> cart = eurderRepository.findByMemberIdAndStatus(memberId, EurderStatus.CART);
        return cart.orElseGet(() -> eurderRepository.save(new Eurder(memberId)));
    }

    private LocalDate calculateShippingDate(Item item, int orderQuantity, LocalDate dateOfFinalization) {
        if(item.getStock()-orderQuantity<0){
            return dateOfFinalization.plusDays(7);
        }else {
            return dateOfFinalization.plusDays(1);
        }
    }
}
