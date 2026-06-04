package ru.yandex.practicum.commerce.shoppingstore.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.commerce.interactionapi.dto.store.ProductCategory;
import ru.yandex.practicum.commerce.interactionapi.dto.store.ProductDto;
import ru.yandex.practicum.commerce.interactionapi.dto.store.ProductState;
import ru.yandex.practicum.commerce.interactionapi.dto.store.SetProductQuantityStateRequest;
import ru.yandex.practicum.commerce.shoppingstore.exception.ProductNotFoundException;
import ru.yandex.practicum.commerce.shoppingstore.mapper.ProductMapper;
import ru.yandex.practicum.commerce.shoppingstore.model.Product;
import ru.yandex.practicum.commerce.shoppingstore.repository.ProductRepository;

import java.util.List;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class ShoppingStoreServiceImpl implements ShoppingStoreService {

    private final ProductRepository productRepository;
    private final ProductMapper productMapper;

    public ShoppingStoreServiceImpl(ProductRepository productRepository,
                                    ProductMapper productMapper) {
        this.productRepository = productRepository;
        this.productMapper = productMapper;
    }

    @Override
    public List<ProductDto> getProducts(ProductCategory category) {
        return productRepository.findAllByProductCategoryAndProductState(category, ProductState.ACTIVE)
                .stream()
                .map(productMapper::toDto)
                .toList();
    }

    @Override
    public ProductDto getProduct(UUID productId) {
        Product product = getProductOrThrow(productId);
        return productMapper.toDto(product);
    }

    @Override
    @Transactional
    public ProductDto createProduct(ProductDto productDto) {
        Product product = productMapper.toEntity(productDto);
        product.setProductId(null);
        product.setProductState(ProductState.ACTIVE);

        Product savedProduct = productRepository.save(product);

        return productMapper.toDto(savedProduct);
    }

    @Override
    @Transactional
    public ProductDto updateProduct(ProductDto productDto) {
        UUID productId = productDto.getProductId();

        if (productId == null) {
            throw new IllegalArgumentException("Product id must not be null");
        }

        Product product = getProductOrThrow(productId);
        productMapper.updateEntity(product, productDto);

        Product savedProduct = productRepository.save(product);

        return productMapper.toDto(savedProduct);
    }

    @Override
    @Transactional
    public Boolean removeProductFromStore(UUID productId) {
        Product product = getProductOrThrow(productId);
        product.setProductState(ProductState.DEACTIVATE);
        productRepository.save(product);

        return true;
    }

    @Override
    @Transactional
    public Boolean setProductQuantityState(SetProductQuantityStateRequest request) {
        Product product = getProductOrThrow(request.getProductId());
        product.setQuantityState(request.getQuantityState());
        productRepository.save(product);

        return true;
    }

    private Product getProductOrThrow(UUID productId) {
        return productRepository.findById(productId)
                .orElseThrow(() -> new ProductNotFoundException(productId));
    }
}