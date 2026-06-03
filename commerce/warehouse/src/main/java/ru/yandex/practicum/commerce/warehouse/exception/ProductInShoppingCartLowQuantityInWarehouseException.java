package ru.yandex.practicum.commerce.warehouse.exception;

import java.util.UUID;

public class ProductInShoppingCartLowQuantityInWarehouseException extends RuntimeException {

    public ProductInShoppingCartLowQuantityInWarehouseException(UUID productId) {
        super("Not enough product with id " + productId + " in warehouse");
    }
}