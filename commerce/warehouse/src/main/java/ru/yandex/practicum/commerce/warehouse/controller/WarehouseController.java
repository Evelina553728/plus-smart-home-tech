package ru.yandex.practicum.commerce.warehouse.controller;

import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import ru.yandex.practicum.commerce.interactionapi.client.WarehouseClient;
import ru.yandex.practicum.commerce.interactionapi.dto.cart.ShoppingCartDto;
import ru.yandex.practicum.commerce.interactionapi.dto.warehouse.AddProductToWarehouseRequest;
import ru.yandex.practicum.commerce.interactionapi.dto.warehouse.AddressDto;
import ru.yandex.practicum.commerce.interactionapi.dto.warehouse.BookedProductsDto;
import ru.yandex.practicum.commerce.interactionapi.dto.warehouse.NewProductInWarehouseRequest;
import ru.yandex.practicum.commerce.warehouse.service.WarehouseService;

@RestController
public class WarehouseController implements WarehouseClient {

    private final WarehouseService warehouseService;

    public WarehouseController(WarehouseService warehouseService) {
        this.warehouseService = warehouseService;
    }

    @Override
    public void addNewProduct(@Valid @RequestBody NewProductInWarehouseRequest request) {
        warehouseService.addNewProduct(request);
    }

    @Override
    public void addProductToWarehouse(@Valid @RequestBody AddProductToWarehouseRequest request) {
        warehouseService.addProductToWarehouse(request);
    }

    @Override
    public BookedProductsDto checkProductQuantityEnoughForShoppingCart(@RequestBody ShoppingCartDto shoppingCartDto) {
        return warehouseService.checkProductQuantityEnoughForShoppingCart(shoppingCartDto);
    }

    @Override
    public AddressDto getWarehouseAddress() {
        return warehouseService.getWarehouseAddress();
    }
}