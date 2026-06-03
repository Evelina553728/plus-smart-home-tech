package ru.yandex.practicum.commerce.shoppingstore.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.yandex.practicum.commerce.interactionapi.dto.store.ProductCategory;
import ru.yandex.practicum.commerce.interactionapi.dto.store.ProductState;
import ru.yandex.practicum.commerce.shoppingstore.model.Product;

import java.util.List;
import java.util.UUID;

public interface ProductRepository extends JpaRepository<Product, UUID> {

    List<Product> findAllByProductCategoryAndProductState(ProductCategory productCategory, ProductState productState);
}