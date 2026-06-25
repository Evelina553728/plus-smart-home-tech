package ru.yandex.practicum.commerce.warehouse.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.commerce.interactionapi.dto.cart.ShoppingCartDto;
import ru.yandex.practicum.commerce.interactionapi.dto.warehouse.AddProductToWarehouseRequest;
import ru.yandex.practicum.commerce.interactionapi.dto.warehouse.AddressDto;
import ru.yandex.practicum.commerce.interactionapi.dto.warehouse.AssemblyProductForOrderRequest;
import ru.yandex.practicum.commerce.interactionapi.dto.warehouse.BookedProductsDto;
import ru.yandex.practicum.commerce.interactionapi.dto.warehouse.NewProductInWarehouseRequest;
import ru.yandex.practicum.commerce.interactionapi.dto.warehouse.ReturnProductsRequest;
import ru.yandex.practicum.commerce.interactionapi.dto.warehouse.ShippedToDeliveryRequest;
import ru.yandex.practicum.commerce.warehouse.exception.ProductAlreadyExistsInWarehouseException;
import ru.yandex.practicum.commerce.warehouse.exception.ProductInShoppingCartLowQuantityInWarehouseException;
import ru.yandex.practicum.commerce.warehouse.exception.ProductNotFoundInWarehouseException;
import ru.yandex.practicum.commerce.warehouse.model.OrderBooking;
import ru.yandex.practicum.commerce.warehouse.model.WarehouseProduct;
import ru.yandex.practicum.commerce.warehouse.repository.OrderBookingRepository;
import ru.yandex.practicum.commerce.warehouse.repository.WarehouseProductRepository;

import java.security.SecureRandom;
import java.util.HashMap;
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
    private final OrderBookingRepository orderBookingRepository;

    public WarehouseServiceImpl(WarehouseProductRepository warehouseProductRepository,
                                OrderBookingRepository orderBookingRepository) {
        this.warehouseProductRepository = warehouseProductRepository;
        this.orderBookingRepository = orderBookingRepository;
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

        validateQuantity(request.getQuantity());

        product.setQuantity(product.getQuantity() + request.getQuantity());

        warehouseProductRepository.save(product);
    }

    @Override
    public BookedProductsDto checkProductQuantityEnoughForShoppingCart(ShoppingCartDto shoppingCartDto) {
        return calculateBookedProducts(shoppingCartDto.getProducts(), false);
    }

    @Override
    public AddressDto getWarehouseAddress() {
        AddressDto address = new AddressDto();

        address.setCountry(CURRENT_ADDRESS);
        address.setCity(CURRENT_ADDRESS);
        address.setStreet(CURRENT_ADDRESS);
        address.setHouse(CURRENT_ADDRESS);
        address.setFlat(CURRENT_ADDRESS);

        return address;
    }

    @Override
    @Transactional
    public BookedProductsDto assemblyProductForOrderFromShoppingCart(AssemblyProductForOrderRequest request) {
        UUID orderId = request.getOrderId();

        if (orderBookingRepository.existsByOrderId(orderId)) {
            throw new IllegalArgumentException("Order with id " + orderId + " is already booked");
        }

        BookedProductsDto bookedProductsDto = calculateBookedProducts(request.getProducts(), true);

        OrderBooking orderBooking = new OrderBooking();
        orderBooking.setOrderId(orderId);
        orderBooking.setProducts(new HashMap<>(request.getProducts()));

        orderBookingRepository.save(orderBooking);

        return bookedProductsDto;
    }

    @Override
    @Transactional
    public void shippedToDelivery(ShippedToDeliveryRequest request) {
        OrderBooking orderBooking = getOrderBookingOrThrow(request.getOrderId());

        orderBooking.setDeliveryId(request.getDeliveryId());

        orderBookingRepository.save(orderBooking);
    }

    @Override
    @Transactional
    public void returnProducts(ReturnProductsRequest request) {
        for (Map.Entry<UUID, Long> entry : request.getProducts().entrySet()) {
            UUID productId = entry.getKey();
            Long returnedQuantity = entry.getValue();

            validateQuantity(returnedQuantity);

            WarehouseProduct product = getProductOrThrow(productId);
            product.setQuantity(product.getQuantity() + returnedQuantity);

            warehouseProductRepository.save(product);
        }
    }

    private BookedProductsDto calculateBookedProducts(Map<UUID, Long> products, boolean decreaseQuantity) {
        double totalWeight = 0.0;
        double totalVolume = 0.0;
        boolean hasFragileProduct = false;

        for (Map.Entry<UUID, Long> entry : products.entrySet()) {
            UUID productId = entry.getKey();
            Long requestedQuantity = entry.getValue();

            validateQuantity(requestedQuantity);

            WarehouseProduct product = getProductOrThrow(productId);

            if (product.getQuantity() < requestedQuantity) {
                throw new ProductInShoppingCartLowQuantityInWarehouseException(productId);
            }

            totalWeight += product.getWeight() * requestedQuantity;
            totalVolume += product.getWidth() * product.getHeight() * product.getDepth() * requestedQuantity;

            if (Boolean.TRUE.equals(product.getFragile())) {
                hasFragileProduct = true;
            }

            if (decreaseQuantity) {
                product.setQuantity(product.getQuantity() - requestedQuantity);
                warehouseProductRepository.save(product);
            }
        }

        return new BookedProductsDto(totalWeight, totalVolume, hasFragileProduct);
    }

    private WarehouseProduct getProductOrThrow(UUID productId) {
        return warehouseProductRepository.findById(productId)
                .orElseThrow(() -> new ProductNotFoundInWarehouseException(productId));
    }

    private OrderBooking getOrderBookingOrThrow(UUID orderId) {
        return orderBookingRepository.findByOrderId(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Order booking with order id " + orderId + " not found"));
    }

    private void validateQuantity(Long quantity) {
        if (quantity == null || quantity <= 0) {
            throw new IllegalArgumentException("Product quantity must be positive");
        }
    }
}