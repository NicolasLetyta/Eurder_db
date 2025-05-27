package com.switchfully.apps.eurder_db.webapi.controller;

import com.switchfully.apps.eurder_db.service.AuthenticationService;
import com.switchfully.apps.eurder_db.service.MemberService;
import com.switchfully.apps.eurder_db.webapi.dto.ItemDtoInput;
import com.switchfully.apps.eurder_db.webapi.dto.ItemDtoOutput;
import com.switchfully.apps.eurder_db.webapi.dto.MemberDtoInput;
import com.switchfully.apps.eurder_db.webapi.dto.MemberDtoOutput;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/members")
public class MemberController {
    private final MemberService memberService;
    private final AuthenticationService authenticationService;
    public MemberController(MemberService memberService, AuthenticationService authenticationService) {
        this.memberService = memberService;
        this.authenticationService = authenticationService;
    }

    @PostMapping(consumes = "application/json", produces = "application/json")
    @ResponseStatus(HttpStatus.CREATED)
    public MemberDtoOutput registerAsCustomer(@RequestBody MemberDtoInput memberDtoInput) {
        return memberService.registerAsCustomer(memberDtoInput);
    }

    @GetMapping(produces = "application/json")
    @ResponseStatus(HttpStatus.OK)
    public List<MemberDtoOutput> findAllCustomers(@RequestHeader(value = "Authorization") String authToken) {
        authenticationService.authenticateAdmin(authToken);
        return memberService.findAllCustomers();
    }

    @GetMapping(path = "/{customerId}", produces = "application/json")
    @ResponseStatus(HttpStatus.OK)
    public MemberDtoOutput findCustomerById(@RequestHeader(value = "Authorization") String authToken,
                                                  @PathVariable Long customerId) {
        authenticationService.authenticateAdmin(authToken);
        return memberService.findCustomerById(customerId);
    }

}
