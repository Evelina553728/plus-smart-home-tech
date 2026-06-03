package ru.yandex.practicum.commerce.shoppingstore.service;

import ru.yandex.practicum.commerce.interactionapi.dto.store.ProductCategory;
import ru.yandex.practicum.commerce.interactionapi.dto.store.ProductDto;
import ru.yandex.practicum.commerce.interactionapi.dto.store.SetProductQuantityStateRequest;

import java.util.List;
import java.util.UUID;

public interface ShoppingStoreService {

    List<ProductDto> getProducts(ProductCategory category);

    ProductDto getProduct(UUID productId);

    ProductDto createProduct(ProductDto productDto);

    ProductDto updateProduct(ProductDto productDto);

    Boolean removeProductFromStore(UUID productId);

    Boolean setProductQuantityState(SetProductQuantityStateRequest request);
}