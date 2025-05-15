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

import static utility.Validation.validateArgument;

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
        validateItemGroupInput(itemGroupDtoInput);
        String fullName = member.getFullName();
        Item item = itemRepository.findById(itemGroupDtoInput.getItemId()).get();
        Eurder cart = createOrGetCart(member.getId());

        ItemGroup itemGroup = itemGroupMapper.inputToItemGroup(itemGroupDtoInput,
                item,
                cart);

        cart.addItemGroup(itemGroup);
        cart = eurderRepository.save(cart);
        return eurderMapper.eurderToOutputCart(cart,fullName);
    }

    public EurderDtoOutput finalizeEurder(Long eurderId, Member member) {
        Eurder eurder = eurderRepository.findByIdAndMemberId(eurderId,member.getId()).orElseThrow(()->new InvalidInputException("Eurder not found in repository"));
        validateArgument(eurder,"Cannot place empty eurder",e->e.getItemGroups().isEmpty(),InvalidInputException::new);

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
        return eurderMapper.eurderToOutputFinalized(eurderRepository.save(eurder),member.getFullName());
    }

    public EurderReport getEurderReport(Long memberId) {
        List<EurderDtoOutput> eurderDtoList = eurderRepository.findAllByMemberIdAndStatus(memberId,EurderStatus.FINALIZED).stream()
                .map(eurderMapper::eurderToDtoReport)
                .toList();
        double totalPrice = eurderDtoList.stream()
                .mapToDouble(EurderDtoOutput::getTotalPrice)
                .sum();
        return new EurderReport(eurderDtoList,totalPrice);
    }

    public EurderDtoOutput reEurder(Long eurderId, Member member) {
        Eurder newEurder = eurderRepository.save(new Eurder(member.getId()));
        Eurder oldEurder = eurderRepository.findByIdAndMemberId(eurderId,member.getId()).orElseThrow(()->new InvalidInputException("Eurder id not found in repository"));
        oldEurder.getItemGroups().forEach(i->newEurder.addItemGroup(new ItemGroup(i.getQuantity(),
                i.getItem(),
                newEurder)));
        return finalizeEurder(newEurder.getId(),member);
    }

    private void validateItemGroupInput(ItemGroupDtoInput itemGroupDtoInput) {
        validateArgument(itemGroupDtoInput.getItemId(),"Item id not found in repository", i->!itemRepository.existsById(i),InvalidInputException::new);
        validateArgument(itemGroupDtoInput.getQuantity(),"Order quantity must be larger than 0", q->q<=0,InvalidInputException::new);
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
