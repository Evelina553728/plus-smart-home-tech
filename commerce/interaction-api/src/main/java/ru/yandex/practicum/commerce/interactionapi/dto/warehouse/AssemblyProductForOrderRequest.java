package ru.yandex.practicum.commerce.interactionapi.dto.warehouse;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class AssemblyProductForOrderRequest {

    @NotNull
    private UUID orderId;

    @NotEmpty
    private Map<UUID, Long> products = new HashMap<>();

    public AssemblyProductForOrderRequest() {
    }

    public AssemblyProductForOrderRequest(UUID orderId, Map<UUID, Long> products) {
        this.orderId = orderId;
        this.products = products;
    }

    public UUID getOrderId() {
        return orderId;
    }

    public void setOrderId(UUID orderId) {
        this.orderId = orderId;
    }

    public Map<UUID, Long> getProducts() {
        return products;
    }

    public void setProducts(Map<UUID, Long> products) {
        this.products = products;
    }
}