package com.switchfully.apps.eurder_db.webapi.controller;

import com.switchfully.apps.eurder_db.domain.Member;
import com.switchfully.apps.eurder_db.service.AuthenticationService;
import com.switchfully.apps.eurder_db.service.EurderService;
import com.switchfully.apps.eurder_db.webapi.dto.EurderDtoList;
import com.switchfully.apps.eurder_db.webapi.dto.EurderDtoOutput;
import com.switchfully.apps.eurder_db.webapi.dto.EurderDtoReport;
import com.switchfully.apps.eurder_db.webapi.dto.ItemGroupDtoInput;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/orders")
public class EurderController {
    private final EurderService eurderService;
    private final AuthenticationService authenticationService;
    public EurderController(EurderService eurderService, AuthenticationService authenticationService) {
        this.eurderService = eurderService;
        this.authenticationService = authenticationService;
    }

    @PostMapping(consumes = "application/json", produces = "application/json")
    @ResponseStatus(HttpStatus.CREATED)
    public EurderDtoOutput addItemGroupToNewCart(@RequestHeader(value = "Authorization") String AuthToken,
                                              @RequestBody ItemGroupDtoInput itemGroupDtoInput) {

        Member customer = authenticationService.authenticateMember(AuthToken);
        return eurderService.addItemGroupToNewCart(itemGroupDtoInput,customer);
    }

    @PostMapping(path = "/{eurderId}", consumes = "application/json", produces = "application/json")
    @ResponseStatus(HttpStatus.CREATED)
    public EurderDtoOutput addItemGroupToExistingCart(@RequestHeader(value = "Authorization") String AuthToken,
                                                 @PathVariable Long eurderId,
                                                 @RequestBody ItemGroupDtoInput itemGroupDtoInput) {

        Member customer = authenticationService.authenticateMember(AuthToken);
        return eurderService.addItemGroupToExistingCart(itemGroupDtoInput,customer, eurderId);
    }

    @PutMapping(path = "/{eurderId}", produces = "application/json")
    @ResponseStatus(HttpStatus.OK)
    public EurderDtoOutput placeEurder(@RequestHeader(value = "Authorization") String AuthToken,
                                       @PathVariable Long eurderId) {
        Member customer = authenticationService.authenticateMember(AuthToken);
        return eurderService.placeEurder(eurderId,customer);
    }

    @GetMapping(path = "/carts",produces = "application/json")
    public List<EurderDtoList> getMemberCarts(@RequestHeader(value = "Authorization") String AuthToken) {
        Member customer = authenticationService.authenticateMember(AuthToken);
        return eurderService.getMemberCarts(customer);
    }

    @GetMapping(path = "/eurder-report", produces = "application/json")
    public EurderDtoReport createEurderReport(@RequestHeader(value = "Authorization") String AuthToken) {
        Member customer = authenticationService.authenticateMember(AuthToken);
        return eurderService.createEurderReport(customer);
    }
}