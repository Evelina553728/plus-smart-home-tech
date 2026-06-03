package ru.yandex.practicum.commerce.warehouse.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.commerce.interactionapi.dto.cart.ShoppingCartDto;
import ru.yandex.practicum.commerce.interactionapi.dto.warehouse.AddProductToWarehouseRequest;
import ru.yandex.practicum.commerce.interactionapi.dto.warehouse.AddressDto;
import ru.yandex.practicum.commerce.interactionapi.dto.warehouse.BookedProductsDto;
import ru.yandex.practicum.commerce.interactionapi.dto.warehouse.NewProductInWarehouseRequest;
import ru.yandex.practicum.commerce.warehouse.exception.ProductAlreadyExistsInWarehouseException;
import ru.yandex.practicum.commerce.warehouse.exception.ProductInShoppingCartLowQuantityInWarehouseException;
import ru.yandex.practicum.commerce.warehouse.exception.ProductNotFoundInWarehouseException;
import ru.yandex.practicum.commerce.warehouse.model.WarehouseProduct;
import ru.yandex.practicum.commerce.warehouse.repository.WarehouseProductRepository;

import java.security.SecureRandom;
import java.util.Map;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class WarehouseServiceImpl implements WarehouseService {

    private static final String[] ADDRESSES = new String[]{"ADDRESS_1", "ADDRESS_2"};

    private static final String CURRENT_ADDRESS = ADDRESSES[
            new SecureRandom().nextInt(ADDRESSES.length)
            ];

    private final WarehouseProductRepository warehouseProductRepository;

    public WarehouseServiceImpl(WarehouseProductRepository warehouseProductRepository) {
        this.warehouseProductRepository = warehouseProductRepository;
    }

    @Override
    @Transactional
    public void addNewProduct(NewProductInWarehouseRequest request) {
        UUID productId = request.getProductId();

        if (warehouseProductRepository.existsById(productId)) {
            throw new ProductAlreadyExistsInWarehouseException(productId);
        }

        WarehouseProduct product = new WarehouseProduct();
        product.setProductId(productId);
        product.setQuantity(0L);
        product.setFragile(request.isFragile());
        product.setWidth(request.getDimension().getWidth());
        product.setHeight(request.getDimension().getHeight());
        product.setDepth(request.getDimension().getDepth());
        product.setWeight(request.getWeight());

        warehouseProductRepository.save(product);
    }

    @Override
    @Transactional
    public void addProductToWarehouse(AddProductToWarehouseRequest request) {
        WarehouseProduct product = getProductOrThrow(request.getProductId());

        product.setQuantity(product.getQuantity() + request.getQuantity());

        warehouseProductRepository.save(product);
    }

    @Override
    public BookedProductsDto checkProductQuantityEnoughForShoppingCart(ShoppingCartDto shoppingCartDto) {
        double totalWeight = 0.0;
        double totalVolume = 0.0;
        boolean hasFragileProduct = false;

        for (Map.Entry<UUID, Long> entry : shoppingCartDto.getProducts().entrySet()) {
            UUID productId = entry.getKey();
            Long requestedQuantity = entry.getValue();

            WarehouseProduct product = getProductOrThrow(productId);

            if (product.getQuantity() < requestedQuantity) {
                throw new ProductInShoppingCartLowQuantityInWarehouseException(productId);
            }

            totalWeight += product.getWeight() * requestedQuantity;
            totalVolume += product.getWidth() * product.getHeight() * product.getDepth() * requestedQuantity;

            if (Boolean.TRUE.equals(product.getFragile())) {
                hasFragileProduct = true;
            }
        }

        return new BookedProductsDto(totalWeight, totalVolume, hasFragileProduct);
    }

    @Override
    public AddressDto getWarehouseAddress() {
        return new AddressDto(
                CURRENT_ADDRESS,
                CURRENT_ADDRESS,
                CURRENT_ADDRESS,
                CURRENT_ADDRESS,
                CURRENT_ADDRESS
        );
    }

    private WarehouseProduct getProductOrThrow(UUID productId) {
        return warehouseProductRepository.findById(productId)
                .orElseThrow(() -> new ProductNotFoundInWarehouseException(productId));
    }
}