package ru.yandex.practicum.commerce.shoppingcart.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.yandex.practicum.commerce.shoppingcart.model.ShoppingCart;

public interface ShoppingCartRepository extends JpaRepository<ShoppingCart, String> {
}