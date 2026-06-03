package ru.yandex.practicum.commerce.interactionapi.client;

import jakarta.validation.Valid;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.commerce.interactionapi.dto.store.ProductCategory;
import ru.yandex.practicum.commerce.interactionapi.dto.store.ProductDto;
import ru.yandex.practicum.commerce.interactionapi.dto.store.SetProductQuantityStateRequest;

import java.util.List;
import java.util.UUID;

@FeignClient(name = "shopping-store")
@RequestMapping("/api/v1/shopping-store")
public interface ShoppingStoreClient {

    @GetMapping
    List<ProductDto> getProducts(@RequestParam("category") ProductCategory category);

    @GetMapping("/{productId}")
    ProductDto getProduct(@PathVariable("productId") UUID productId);

    @PutMapping
    ProductDto createProduct(@Valid @RequestBody ProductDto productDto);

    @PostMapping
    ProductDto updateProduct(@Valid @RequestBody ProductDto productDto);

    @PostMapping("/removeProductFromStore")
    Boolean removeProductFromStore(@RequestBody UUID productId);

    @PostMapping("/quantityState")
    Boolean setProductQuantityState(@Valid @RequestBody SetProductQuantityStateRequest request);
}