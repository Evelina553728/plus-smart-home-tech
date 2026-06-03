package ru.yandex.practicum.commerce.warehouse.exception;

import java.util.UUID;

public class ProductAlreadyExistsInWarehouseException extends RuntimeException {

    public ProductAlreadyExistsInWarehouseException(UUID productId) {
        super("Product with id " + productId + " already exists in warehouse");
    }
}