package ru.yandex.practicum.commerce.shoppingstore.controller;

import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.yandex.practicum.commerce.interactionapi.client.ShoppingStoreClient;
import ru.yandex.practicum.commerce.interactionapi.dto.store.ProductCategory;
import ru.yandex.practicum.commerce.interactionapi.dto.store.ProductDto;
import ru.yandex.practicum.commerce.interactionapi.dto.store.SetProductQuantityStateRequest;
import ru.yandex.practicum.commerce.shoppingstore.service.ShoppingStoreService;

import java.util.List;
import java.util.UUID;

@RestController
public class ShoppingStoreController implements ShoppingStoreClient {

    private final ShoppingStoreService shoppingStoreService;

    public ShoppingStoreController(ShoppingStoreService shoppingStoreService) {
        this.shoppingStoreService = shoppingStoreService;
    }

    @Override
    public List<ProductDto> getProducts(@RequestParam("category") ProductCategory category) {
        return shoppingStoreService.getProducts(category);
    }

    @Override
    public ProductDto getProduct(@PathVariable("productId") UUID productId) {
        return shoppingStoreService.getProduct(productId);
    }

    @Override
    public ProductDto createProduct(@Valid @RequestBody ProductDto productDto) {
        return shoppingStoreService.createProduct(productDto);
    }

    @Override
    public ProductDto updateProduct(@Valid @RequestBody ProductDto productDto) {
        return shoppingStoreService.updateProduct(productDto);
    }

    @Override
    public Boolean removeProductFromStore(@RequestBody UUID productId) {
        return shoppingStoreService.removeProductFromStore(productId);
    }

    @Override
    public Boolean setProductQuantityState(@Valid @RequestBody SetProductQuantityStateRequest request) {
        return shoppingStoreService.setProductQuantityState(request);
    }
}