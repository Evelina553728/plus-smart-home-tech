package ru.yandex.practicum.commerce.interactionapi.client;

import jakarta.validation.Valid;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.commerce.interactionapi.dto.cart.ShoppingCartDto;
import ru.yandex.practicum.commerce.interactionapi.dto.warehouse.AddProductToWarehouseRequest;
import ru.yandex.practicum.commerce.interactionapi.dto.warehouse.AddressDto;
import ru.yandex.practicum.commerce.interactionapi.dto.warehouse.BookedProductsDto;
import ru.yandex.practicum.commerce.interactionapi.dto.warehouse.NewProductInWarehouseRequest;

@FeignClient(name = "warehouse")
@RequestMapping("/api/v1/warehouse")
public interface WarehouseClient {

    @PutMapping
    void addNewProduct(@Valid @RequestBody NewProductInWarehouseRequest request);

    @PostMapping("/add")
    void addProductToWarehouse(@Valid @RequestBody AddProductToWarehouseRequest request);

    @PostMapping("/check")
    BookedProductsDto checkProductQuantityEnoughForShoppingCart(@RequestBody ShoppingCartDto shoppingCartDto);

    @GetMapping("/address")
    AddressDto getWarehouseAddress();
}