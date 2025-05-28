package com.switchfully.apps.eurder_db.service;

import com.switchfully.apps.eurder_db.domain.*;
import com.switchfully.apps.eurder_db.exception.InvalidInputException;
import com.switchfully.apps.eurder_db.webapi.dto.EurderDtoList;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.switchfully.apps.eurder_db.repository.EurderRepository;
import com.switchfully.apps.eurder_db.repository.ItemRepository;
import com.switchfully.apps.eurder_db.service.mapper.EurderMapper;
import com.switchfully.apps.eurder_db.service.mapper.ItemGroupMapper;
import com.switchfully.apps.eurder_db.webapi.dto.EurderDtoOutput;
import com.switchfully.apps.eurder_db.webapi.dto.EurderDtoReport;
import com.switchfully.apps.eurder_db.webapi.dto.ItemGroupDtoInput;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

import static com.switchfully.apps.eurder_db.utility.Validation.validateArgument;

@Service
@Transactional
public class EurderService {
    private final EurderRepository eurderRepository;
    private final EurderMapper eurderMapper;
    private final ItemGroupMapper itemGroupMapper;
    private final ItemRepository itemRepository;
    public EurderService(EurderRepository eurderRepository,
                         EurderMapper eurderMapper,
                         ItemGroupMapper itemGroupMapper,
                         ItemRepository itemRepository) {
        this.eurderRepository = eurderRepository;
        this.eurderMapper = eurderMapper;
        this.itemGroupMapper = itemGroupMapper;
        this.itemRepository = itemRepository;
    }

    //initialize an eurder by saving an eurder to the db with status CART
    // not sure is this one is neede, if i want a new cart, i can just use add item group to new cart and use that id, if i create a cart i probably want to immediately add an itemgroup

//    public EurderDtoOutput createCart(Member member) {
//        Eurder cart = eurderRepository.save(new Eurder(member.getId()));
//        return eurderMapper.eurderToOutputCart(cart, member.getFullName());
//    }

    public EurderDtoOutput addItemGroupToExistingCart(ItemGroupDtoInput itemGroupDtoInput, Member member, Long eurderId) {
        Eurder cart = findEurderAndValidateOwnership(eurderId,member.getId());
        validateArgument(cart,"The found order is not of type CART, cannot add itemgroup to order with status FINALIZED",c->c.getStatus()!=EurderStatus.CART,InvalidInputException::new);

        return addItemGroupToCart(itemGroupDtoInput,member,cart);
    }

    public EurderDtoOutput addItemGroupToNewCart(ItemGroupDtoInput itemGroupDtoInput, Member member) {
        //create a new cart and save to the repository
        Eurder cart = eurderRepository.save(new Eurder(member.getId()));

        return addItemGroupToCart(itemGroupDtoInput,member,cart);
    }

    public EurderDtoOutput placeEurder(Long eurderId, Member member) {
        Eurder eurder = findEurderAndValidateOwnership(eurderId,member.getId());
        return finalizeEurder(eurder,member.getFullName());
    }

    public EurderDtoReport createEurderReport(Member member) {
        List<Eurder> eurdersFinalized = eurderRepository.findAllByMemberIdAndStatus(member.getId(), EurderStatus.FINALIZED);
        if(eurdersFinalized.isEmpty()){
            return null;
        }else {
            return eurderMapper.eurdersToDtoReport(eurdersFinalized);
        }
    }

    //there has to be some way to see how many carts a user has...
    //currently the tot price of a cart is 0 -> eurdermapper -> eurderToDtoList uses itemGroupDtoList.getSubtotalPrice = itemGroup.getTotalPriceAtEurderDate -> total price at eurder date is not set yet and thus is null
    //this might also result in a nullPointerException... I will need another DTO -> CartOverViewDto :)
    public List<EurderDtoList> getMemberCarts(Member member) {
        List<Eurder> memberCartList = eurderRepository.findAllByMemberIdAndStatus(member.getId(), EurderStatus.CART);
        validateArgument(memberCartList,"No eurders with status CART found for member "+member, List::isEmpty,InvalidInputException::new);

        return memberCartList.stream()
                .map(eurderMapper::eurderToDtoListCart)
                .collect(Collectors.toList());
    }

    public EurderDtoOutput placeReEurder(Long eurderId, Member member) {
        Eurder oldEurder = findEurderAndValidateOwnership(eurderId,member.getId());
        validateArgument(oldEurder,"Eurder not finalized yet, cannot re-order until finalized",e->!e.getStatus().equals(EurderStatus.FINALIZED),InvalidInputException::new);
        Eurder newEurder = new Eurder(member.getId());

        oldEurder.getItemGroups().forEach(i->{
            ItemGroup newItemGroup = new ItemGroup(i.getQuantity(), i.getItem(), newEurder);
            newEurder.addItemGroup(newItemGroup);
        });
        return finalizeEurder(newEurder,member.getFullName());
    }

    private EurderDtoOutput addItemGroupToCart(ItemGroupDtoInput itemGroupDtoInput, Member member, Eurder cart) {
        validateItemGroupInput(itemGroupDtoInput);
        Item item = itemRepository.findById(itemGroupDtoInput.getItemId()).get();
        ItemGroup itemGroup = itemGroupMapper.inputToItemGroup(itemGroupDtoInput,
                item,
                cart);

        //shows expected shipping date, will be recalculated at finalization of order
        itemGroup.setShippingDate(calculateShippingDate(item,itemGroupDtoInput.getQuantity(),LocalDate.now()));

        cart.addItemGroup(itemGroup);
        cart = eurderRepository.save(cart);
        return eurderMapper.eurderToOutputCart(cart,member.getFullName());
    }

    private EurderDtoOutput finalizeEurder(Eurder eurder, String memberName) {
        validateArgument(eurder,"Cannot place empty eurder",e->e.getItemGroups().isEmpty(),InvalidInputException::new);
        validateArgument(eurder,"Cannot finalize eurder with status FINALIZED, please use reEurder endpoint",e->e.getStatus().equals(EurderStatus.FINALIZED),InvalidInputException::new);

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

    private void validateItemGroupInput(ItemGroupDtoInput itemGroupDtoInput) {
        validateArgument(itemGroupDtoInput.getItemId(),"Item id not found in repository", i->!itemRepository.existsById(i),InvalidInputException::new);
        validateArgument(itemGroupDtoInput.getQuantity(),"Order quantity must be larger than 0", q->q<=0,InvalidInputException::new);
    }

    private Eurder findEurderAndValidateOwnership(Long eurderId, Long memberId) {
        Eurder eurder = eurderRepository.findById(eurderId).orElseThrow(() -> new InvalidInputException("Order id not found, please enter a valid order id"));
        return validateArgument(eurder,"This order does not belong to the privided user id",e->!eurder.getMemberId().equals(memberId),InvalidInputException::new);
    }

    private LocalDate calculateShippingDate(Item item, int orderQuantity, LocalDate dateOfFinalization) {
        if(item.getStock()-orderQuantity<0){
            return dateOfFinalization.plusDays(7);
        }else {
            return dateOfFinalization.plusDays(1);
        }
    }
}
