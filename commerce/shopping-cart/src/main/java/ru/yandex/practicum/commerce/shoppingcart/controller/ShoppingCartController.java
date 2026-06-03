package ru.yandex.practicum.commerce.shoppingcart.controller;

import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.yandex.practicum.commerce.interactionapi.client.ShoppingCartClient;
import ru.yandex.practicum.commerce.interactionapi.dto.cart.ChangeProductQuantityRequest;
import ru.yandex.practicum.commerce.interactionapi.dto.cart.ShoppingCartDto;
import ru.yandex.practicum.commerce.shoppingcart.service.ShoppingCartService;

import java.util.Map;
import java.util.UUID;

@RestController
public class ShoppingCartController implements ShoppingCartClient {

    private final ShoppingCartService shoppingCartService;

    public ShoppingCartController(ShoppingCartService shoppingCartService) {
        this.shoppingCartService = shoppingCartService;
    }

    @Override
    public ShoppingCartDto getShoppingCart(@RequestParam("username") String username) {
        return shoppingCartService.getShoppingCart(username);
    }

    @Override
    public ShoppingCartDto addProductToShoppingCart(@RequestParam("username") String username,
                                                    @RequestBody Map<UUID, Long> products) {
        return shoppingCartService.addProductToShoppingCart(username, products);
    }

    @Override
    public ShoppingCartDto changeProductQuantity(@RequestParam("username") String username,
                                                 @Valid @RequestBody ChangeProductQuantityRequest request) {
        return shoppingCartService.changeProductQuantity(username, request);
    }

    @Override
    public ShoppingCartDto removeProductFromShoppingCart(@RequestParam("username") String username,
                                                         @RequestBody Map<UUID, Long> products) {
        return shoppingCartService.removeProductFromShoppingCart(username, products);
    }

    @Override
    public void deactivateShoppingCart(@RequestParam("username") String username) {
        shoppingCartService.deactivateShoppingCart(username);
    }
}