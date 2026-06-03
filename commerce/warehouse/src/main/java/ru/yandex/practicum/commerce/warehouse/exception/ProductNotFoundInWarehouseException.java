package ru.yandex.practicum.commerce.warehouse.exception;

import java.util.UUID;

public class ProductNotFoundInWarehouseException extends RuntimeException {

    public ProductNotFoundInWarehouseException(UUID productId) {
        super("Product with id " + productId + " not found in warehouse");
    }
}