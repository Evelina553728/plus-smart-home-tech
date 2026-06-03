package ru.yandex.practicum.commerce.shoppingstore.mapper;

import org.springframework.stereotype.Component;
import ru.yandex.practicum.commerce.interactionapi.dto.store.ProductDto;
import ru.yandex.practicum.commerce.interactionapi.dto.store.ProductState;
import ru.yandex.practicum.commerce.shoppingstore.model.Product;

@Component
public class ProductMapper {

    public ProductDto toDto(Product product) {
        ProductDto dto = new ProductDto();

        dto.setProductId(product.getProductId());
        dto.setProductName(product.getProductName());
        dto.setDescription(product.getDescription());
        dto.setImageSrc(product.getImageSrc());
        dto.setProductCategory(product.getProductCategory());
        dto.setQuantityState(product.getQuantityState());
        dto.setProductState(product.getProductState());

        return dto;
    }

    public Product toEntity(ProductDto dto) {
        Product product = new Product();

        product.setProductId(dto.getProductId());
        product.setProductName(dto.getProductName());
        product.setDescription(dto.getDescription());
        product.setImageSrc(dto.getImageSrc());
        product.setProductCategory(dto.getProductCategory());
        product.setQuantityState(dto.getQuantityState());

        if (dto.getProductState() == null) {
            product.setProductState(ProductState.ACTIVE);
        } else {
            product.setProductState(dto.getProductState());
        }

        return product;
    }

    public void updateEntity(Product product, ProductDto dto) {
        product.setProductName(dto.getProductName());
        product.setDescription(dto.getDescription());
        product.setImageSrc(dto.getImageSrc());
        product.setProductCategory(dto.getProductCategory());
        product.setQuantityState(dto.getQuantityState());

        if (dto.getProductState() != null) {
            product.setProductState(dto.getProductState());
        }
    }
}