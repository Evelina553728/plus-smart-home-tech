package ru.yandex.practicum.commerce.shoppingcart.model;

import jakarta.persistence.*;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Entity
@Table(name = "shopping_carts")
public class ShoppingCart {

    @Id
    @Column(name = "username")
    private String username;

    @ElementCollection
    @CollectionTable(
            name = "shopping_cart_products",
            joinColumns = @JoinColumn(name = "username")
    )
    @MapKeyColumn(name = "product_id")
    @Column(name = "quantity")
    private Map<UUID, Long> products = new HashMap<>();

    @Column(name = "active", nullable = false)
    private Boolean active = true;

    public ShoppingCart() {
    }

    public ShoppingCart(String username) {
        this.username = username;
        this.active = true;
        this.products = new HashMap<>();
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

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }
}