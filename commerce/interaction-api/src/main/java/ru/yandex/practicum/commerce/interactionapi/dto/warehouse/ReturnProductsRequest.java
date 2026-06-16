package ru.yandex.practicum.commerce.interactionapi.dto.warehouse;

import jakarta.validation.constraints.NotEmpty;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ReturnProductsRequest {

    @NotEmpty
    private Map<UUID, Long> products = new HashMap<>();

    public ReturnProductsRequest() {
    }

    public ReturnProductsRequest(Map<UUID, Long> products) {
        this.products = products;
    }

    public Map<UUID, Long> getProducts() {
        return products;
    }

    public void setProducts(Map<UUID, Long> products) {
        this.products = products;
    }
}