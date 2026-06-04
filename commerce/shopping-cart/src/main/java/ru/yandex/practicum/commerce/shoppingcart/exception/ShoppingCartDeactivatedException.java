package ru.yandex.practicum.commerce.shoppingcart.exception;

public class ShoppingCartDeactivatedException extends RuntimeException {

    public ShoppingCartDeactivatedException(String username) {
        super("Shopping cart for user " + username + " is deactivated");
    }
}