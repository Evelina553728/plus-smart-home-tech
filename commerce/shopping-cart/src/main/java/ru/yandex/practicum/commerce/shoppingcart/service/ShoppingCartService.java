package ru.yandex.practicum.commerce.shoppingcart.service;

import ru.yandex.practicum.commerce.interactionapi.dto.cart.ChangeProductQuantityRequest;
import ru.yandex.practicum.commerce.interactionapi.dto.cart.ShoppingCartDto;

import java.util.Map;
import java.util.UUID;

public interface ShoppingCartService {

    ShoppingCartDto getShoppingCart(String username);

    ShoppingCartDto addProductToShoppingCart(String username, Map<UUID, Long> products);

    ShoppingCartDto changeProductQuantity(String username, ChangeProductQuantityRequest request);

    ShoppingCartDto removeProductFromShoppingCart(String username, Map<UUID, Long> products);

    void deactivateShoppingCart(String username);
}