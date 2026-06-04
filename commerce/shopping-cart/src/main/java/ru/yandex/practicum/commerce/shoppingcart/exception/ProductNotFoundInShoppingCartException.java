package ru.yandex.practicum.commerce.shoppingcart.exception;

import java.util.UUID;

public class ProductNotFoundInShoppingCartException extends RuntimeException {

    public ProductNotFoundInShoppingCartException(UUID productId) {
        super("Product with id " + productId + " not found in shopping cart");
    }
}