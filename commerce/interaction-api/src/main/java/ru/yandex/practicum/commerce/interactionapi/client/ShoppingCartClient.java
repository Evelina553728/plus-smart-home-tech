package ru.yandex.practicum.commerce.interactionapi.client;

import jakarta.validation.Valid;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.commerce.interactionapi.dto.cart.ChangeProductQuantityRequest;
import ru.yandex.practicum.commerce.interactionapi.dto.cart.ShoppingCartDto;

import java.util.Map;
import java.util.UUID;

@FeignClient(name = "shopping-cart")
public interface ShoppingCartClient {

    @GetMapping("/api/v1/shopping-cart")
    ShoppingCartDto getShoppingCart(@RequestParam("username") String username);

    @PutMapping("/api/v1/shopping-cart")
    ShoppingCartDto addProductToShoppingCart(@RequestParam("username") String username,
                                             @RequestBody Map<UUID, Long> products);

    @PostMapping("/api/v1/shopping-cart")
    ShoppingCartDto changeProductQuantity(@RequestParam("username") String username,
                                          @Valid @RequestBody ChangeProductQuantityRequest request);

    @PostMapping("/api/v1/shopping-cart/remove")
    ShoppingCartDto removeProductFromShoppingCart(@RequestParam("username") String username,
                                                  @RequestBody Map<UUID, Long> products);

    @PostMapping("/api/v1/shopping-cart/deactivate")
    void deactivateShoppingCart(@RequestParam("username") String username);
}