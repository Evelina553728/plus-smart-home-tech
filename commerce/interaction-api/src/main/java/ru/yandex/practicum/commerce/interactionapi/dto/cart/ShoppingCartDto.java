package ru.yandex.practicum.commerce.interactionapi.dto.cart;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ShoppingCartDto {

    private String username;

    private Map<UUID, Long> products = new HashMap<>();

    public ShoppingCartDto() {
    }

    public ShoppingCartDto(String username, Map<UUID, Long> products) {
        this.username = username;
        this.products = products;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public Map<UUID, Long> getProducts() {
        return products;
    }

    public void setProducts(Map<UUID, Long> products) {
        this.products = products;
    }
}