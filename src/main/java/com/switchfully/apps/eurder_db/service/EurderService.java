package com.switchfully.apps.eurder_db.service;

import com.switchfully.apps.eurder_db.domain.*;
import com.switchfully.apps.eurder_db.exception.InvalidInputException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.switchfully.apps.eurder_db.repository.EurderRepository;
import com.switchfully.apps.eurder_db.repository.ItemRepository;
import com.switchfully.apps.eurder_db.repository.MemberRepository;
import com.switchfully.apps.eurder_db.service.mapper.EurderMapper;
import com.switchfully.apps.eurder_db.service.mapper.ItemGroupMapper;
import com.switchfully.apps.eurder_db.webapi.dto.EurderDtoOutput;
import com.switchfully.apps.eurder_db.webapi.dto.EurderReport;
import com.switchfully.apps.eurder_db.webapi.dto.ItemGroupDtoInput;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.switchfully.apps.eurder_db.utility.Validation.validateArgument;

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

    public EurderDtoOutput addItemGroupToCart(ItemGroupDtoInput itemGroupDtoInput, Member member, Long cartId) {
        validateItemGroupInput(itemGroupDtoInput);
        String fullName = member.getFullName();
        Item item = itemRepository.findById(itemGroupDtoInput.getItemId()).get();
        //either add itemgroup to existing cart eurder, or create a new eurder cart
        Eurder cart = createOrGetCart(member.getId(),cartId);

        ItemGroup itemGroup = itemGroupMapper.inputToItemGroup(itemGroupDtoInput,
                item,
                cart);

        //shows expected shipping date, will be recalculated at finalization of order
        itemGroup.setShippingDate(calculateShippingDate(item,itemGroupDtoInput.getQuantity(),LocalDate.now()));

        cart.addItemGroup(itemGroup);
        cart = eurderRepository.save(cart);
        return eurderMapper.eurderToOutputCart(cart,fullName);
    }

    public EurderDtoOutput finalizeEurder(Long eurderId, Member member) {
        Eurder eurder = validateEurderIdMember(eurderId,member);
        return finalizeEurderPRIVATE(eurder,member.getFullName());
    }

//    public EurderDtoOutput finalizeEurder(Long eurderId, Member member) {
//
//        Eurder eurder = eurderRepository.findById(eurderId).orElseThrow(() -> new InvalidInputException("Eurder not found"));
//        validateArgument(eurder,"Cannot place empty eurder",e->e.getItemGroups().isEmpty(),InvalidInputException::new);
//        validateArgument(eurder,"Cannot finalize past eurder, please use reEurder endpoint",e->e.getStatus().equals(EurderStatus.FINALIZED),InvalidInputException::new);
//
//        eurder.getItemGroups()
//                .stream()// stream over all ItemGroups in the cart
//                .collect(Collectors.groupingBy(ItemGroup::getItem))   // organize itemgroups in lists in a mpa with Item as key value -> Map<Item, List<ItemGroup>>
//                .forEach((item, groups) -> {
//                    int totalOrderQuantity = groups.stream()
//                            .mapToInt(ItemGroup::getQuantity)
//                            .sum();                      // total order quantity per item in this eurder
//
//                    item.setStock(item.getStock()-totalOrderQuantity);
//                    itemRepository.save(item);
//                    LocalDate shipDate = calculateShippingDate(item,totalOrderQuantity,LocalDate.now());
//                    groups.forEach(g -> g.setTotalPriceAtEurderDate(g.calculateCurrentSubtotalPrice()));//set the total itemgroup price at shipping date to todays prices
//                    groups.forEach(g -> g.setShippingDate(shipDate));  // for each group set date according to total
//                    //orderquantity for an item, not per itemGroup :)
//                });
//
//
//        eurder.setStatusFinalized();
//        return eurderMapper.eurderToOutputFinalized(eurderRepository.save(eurder),member.getFullName());
//    }

    public EurderReport getEurderReport(Long memberId) {
        List<EurderDtoOutput> eurderDtoList = eurderRepository.findAllByMemberIdAndStatus(memberId, EurderStatus.FINALIZED).stream()
                .map(eurderMapper::eurderToDtoReport)
                .toList();
        double totalPrice = eurderDtoList.stream()
                .mapToDouble(EurderDtoOutput::getTotalPrice)
                .sum();
        return new EurderReport(eurderDtoList,totalPrice);
    }

    public EurderDtoOutput reEurder(Long eurderId, Member member) {
        Eurder oldEurder = validateEurderIdMember(eurderId,member);
        validateArgument(oldEurder,"Eurder not finalized yet, cannot re-order until finalized",e->!e.getStatus().equals(EurderStatus.FINALIZED),InvalidInputException::new);
        Eurder newEurder = eurderRepository.save(new Eurder(member.getId()));

        oldEurder.getItemGroups().forEach(i->{
            ItemGroup newItemGroup = new ItemGroup(i.getQuantity(), i.getItem(), newEurder);
            newItemGroup.setShippingDate(calculateShippingDate(i.getItem(),i.getQuantity(),LocalDate.now()));
            newEurder.addItemGroup(newItemGroup);
        });
        eurderRepository.save(newEurder);
        return finalizeEurderPRIVATE(newEurder,member.getFullName());
    }

    private EurderDtoOutput finalizeEurderPRIVATE(Eurder eurder, String memberName) {
        validateArgument(eurder,"Cannot place empty eurder",e->e.getItemGroups().isEmpty(),InvalidInputException::new);
        validateArgument(eurder,"Cannot finalize past eurder, please use reEurder endpoint",e->e.getStatus().equals(EurderStatus.FINALIZED),InvalidInputException::new);

        eurder.getItemGroups()
                .stream()// stream over all ItemGroups in the cart
                .collect(Collectors.groupingBy(ItemGroup::getItem))   // organize itemgroups in lists in a mpa with Item as key value -> Map<Item, List<ItemGroup>>
                .forEach((item, groups) -> {
                    int totalOrderQuantity = groups.stream()
                            .mapToInt(ItemGroup::getQuantity)
                            .sum();                      // total order quantity per item in this eurder

                    item.setStock(item.getStock()-totalOrderQuantity);
                    itemRepository.save(item);
                    LocalDate shipDate = calculateShippingDate(item,totalOrderQuantity,LocalDate.now());
                    groups.forEach(g -> g.setTotalPriceAtEurderDate(g.calculateCurrentSubtotalPrice()));//set the total itemgroup price at shipping date to todays prices
                    groups.forEach(g -> g.setShippingDate(shipDate));  // for each group set date according to total
                    //orderquantity for an item, not per itemGroup :)
                });
        eurder.setStatusFinalized();
        return eurderMapper.eurderToOutputFinalized(eurderRepository.save(eurder),memberName);
    }

    private Eurder validateEurderIdMember(Long eurderId, Member member) {
        Eurder eurder = eurderRepository.findById(eurderId).orElseThrow(() -> new InvalidInputException("Eurder not found in repository"));
        validateArgument(eurder,"Eurder does not belong to this member",e->!e.getMemberId().equals(member.getId()),InvalidInputException::new);
        return eurder;
    }

    private void validateItemGroupInput(ItemGroupDtoInput itemGroupDtoInput) {
        validateArgument(itemGroupDtoInput.getItemId(),"Item id not found in repository", i->!itemRepository.existsById(i),InvalidInputException::new);
        validateArgument(itemGroupDtoInput.getQuantity(),"Order quantity must be larger than 0", q->q<=0,InvalidInputException::new);
    }


    private Eurder createOrGetCart(Long memberId, Long cartId) {
        Optional<Eurder> cart = eurderRepository.findByMemberIdAndStatusAndId(memberId, EurderStatus.CART,cartId);
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
