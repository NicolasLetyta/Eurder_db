package com.switchfully.apps.eurder_db.webapi.controller;

import com.switchfully.apps.eurder_db.service.AuthenticationService;
import com.switchfully.apps.eurder_db.service.ItemService;
import com.switchfully.apps.eurder_db.webapi.dto.ItemDtoInput;
import com.switchfully.apps.eurder_db.webapi.dto.ItemDtoOutput;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/items")
public class ItemController {
    private final ItemService itemService;
    private final AuthenticationService authenticationService;
    public ItemController(ItemService itemService, AuthenticationService authenticationService) {
        this.itemService = itemService;
        this.authenticationService = authenticationService;
    }

    @PostMapping(consumes = "application/json", produces = "application/json")
    @ResponseStatus(HttpStatus.CREATED)
    public ItemDtoOutput addItem(@RequestBody ItemDtoInput itemDtoInput,
                                 @RequestHeader(value = "Authorization") String authToken) {
        authenticationService.authenticateAdmin(authToken);
        return itemService.addItem(itemDtoInput);
    }

    @PutMapping(path = "/{itemId}", consumes = "application/json", produces = "application/json")
    @ResponseStatus(HttpStatus.OK)
    public ItemDtoOutput updateItem(@PathVariable Long itemId,
                                    @RequestBody ItemDtoInput itemDtoInput,
                                    @RequestHeader(value = "Authorization") String authToken) {
        authenticationService.authenticateAdmin(authToken);
        return itemService.updateItem(itemDtoInput,itemId);
    }
}
