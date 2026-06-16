package ru.yandex.practicum.commerce.order.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.commerce.interactionapi.client.DeliveryClient;
import ru.yandex.practicum.commerce.interactionapi.client.PaymentClient;
import ru.yandex.practicum.commerce.interactionapi.client.WarehouseClient;
import ru.yandex.practicum.commerce.interactionapi.dto.cart.ShoppingCartDto;
import ru.yandex.practicum.commerce.interactionapi.dto.delivery.DeliveryDto;
import ru.yandex.practicum.commerce.interactionapi.dto.order.OrderDto;
import ru.yandex.practicum.commerce.interactionapi.dto.order.OrderState;
import ru.yandex.practicum.commerce.interactionapi.dto.payment.PaymentDto;
import ru.yandex.practicum.commerce.interactionapi.dto.warehouse.AddressDto;
import ru.yandex.practicum.commerce.interactionapi.dto.warehouse.AssemblyProductForOrderRequest;
import ru.yandex.practicum.commerce.interactionapi.dto.warehouse.BookedProductsDto;
import ru.yandex.practicum.commerce.interactionapi.dto.warehouse.ReturnProductsRequest;
import ru.yandex.practicum.commerce.order.exception.OrderNotFoundException;
import ru.yandex.practicum.commerce.order.mapper.OrderMapper;
import ru.yandex.practicum.commerce.order.model.OrderEntity;
import ru.yandex.practicum.commerce.order.repository.OrderRepository;

import java.util.HashMap;
import java.util.List;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class OrderServiceImpl implements OrderService {

    private static final AddressDto DEFAULT_DELIVERY_ADDRESS = new AddressDto(
            "ADDRESS_TO",
            "ADDRESS_TO",
            "ADDRESS_TO",
            "ADDRESS_TO",
            "ADDRESS_TO"
    );

    private final OrderRepository orderRepository;
    private final OrderMapper orderMapper;
    private final WarehouseClient warehouseClient;
    private final DeliveryClient deliveryClient;
    private final PaymentClient paymentClient;

    public OrderServiceImpl(OrderRepository orderRepository,
                            OrderMapper orderMapper,
                            WarehouseClient warehouseClient,
                            DeliveryClient deliveryClient,
                            PaymentClient paymentClient) {
        this.orderRepository = orderRepository;
        this.orderMapper = orderMapper;
        this.warehouseClient = warehouseClient;
        this.deliveryClient = deliveryClient;
        this.paymentClient = paymentClient;
    }

    @Override
    @Transactional
    public OrderDto createNewOrder(ShoppingCartDto shoppingCartDto) {
        validateShoppingCart(shoppingCartDto);

        BookedProductsDto bookedProducts = warehouseClient.checkProductQuantityEnoughForShoppingCart(shoppingCartDto);

        OrderEntity order = new OrderEntity();
        order.setUsername(shoppingCartDto.getUsername());
        order.setProducts(new HashMap<>(shoppingCartDto.getProducts()));
        order.setOrderState(OrderState.NEW);
        order.setDeliveryAddress(orderMapper.toAddress(DEFAULT_DELIVERY_ADDRESS));

        applyBookedProducts(order, bookedProducts);

        return orderMapper.toDto(orderRepository.save(order));
    }

    @Override
    public List<OrderDto> getUserOrders(String username) {
        if (username == null || username.isBlank()) {
            throw new IllegalArgumentException("Username must not be blank");
        }

        return orderRepository.findAllByUsername(username)
                .stream()
                .map(orderMapper::toDto)
                .toList();
    }

    @Override
    @Transactional
    public OrderDto assembly(UUID orderId) {
        OrderEntity order = getOrderOrThrow(orderId);

        BookedProductsDto bookedProducts = warehouseClient.assemblyProductForOrderFromShoppingCart(
                new AssemblyProductForOrderRequest(order.getOrderId(), order.getProducts())
        );

        applyBookedProducts(order, bookedProducts);
        order.setOrderState(OrderState.ASSEMBLED);

        return orderMapper.toDto(orderRepository.save(order));
    }

    @Override
    @Transactional
    public OrderDto assemblyFailed(UUID orderId) {
        return changeState(orderId, OrderState.ASSEMBLY_FAILED);
    }

    @Override
    @Transactional
    public OrderDto payment(UUID orderId) {
        OrderEntity order = getOrderOrThrow(orderId);

        PaymentDto payment = paymentClient.payment(orderMapper.toDto(order));

        order.setPaymentId(payment.getPaymentId());
        order.setProductPrice(payment.getProductPrice());
        order.setDeliveryPrice(payment.getDeliveryPrice());
        order.setTotalPrice(payment.getTotalPrice());
        order.setOrderState(OrderState.ON_PAYMENT);

        return orderMapper.toDto(orderRepository.save(order));
    }

    @Override
    @Transactional
    public OrderDto paymentSuccess(UUID orderId) {
        return changeState(orderId, OrderState.PAID);
    }

    @Override
    @Transactional
    public OrderDto paymentFailed(UUID orderId) {
        return changeState(orderId, OrderState.PAYMENT_FAILED);
    }

    @Override
    @Transactional
    public OrderDto delivery(UUID orderId) {
        return changeState(orderId, OrderState.DELIVERED);
    }

    @Override
    @Transactional
    public OrderDto deliveryFailed(UUID orderId) {
        return changeState(orderId, OrderState.DELIVERY_FAILED);
    }

    @Override
    @Transactional
    public OrderDto completed(UUID orderId) {
        return changeState(orderId, OrderState.COMPLETED);
    }

    @Override
    @Transactional
    public OrderDto calculateDelivery(UUID orderId) {
        OrderEntity order = getOrderOrThrow(orderId);

        DeliveryDto delivery = new DeliveryDto();
        delivery.setOrderId(order.getOrderId());
        delivery.setFromAddress(warehouseClient.getWarehouseAddress());
        delivery.setToAddress(DEFAULT_DELIVERY_ADDRESS);
        delivery.setDeliveryWeight(order.getDeliveryWeight());
        delivery.setDeliveryVolume(order.getDeliveryVolume());
        delivery.setFragile(order.getFragile());

        DeliveryDto plannedDelivery = deliveryClient.planDelivery(delivery);
        Double deliveryPrice = deliveryClient.deliveryCost(plannedDelivery);

        order.setDeliveryId(plannedDelivery.getDeliveryId());
        order.setDeliveryPrice(deliveryPrice);

        return orderMapper.toDto(orderRepository.save(order));
    }

    @Override
    @Transactional
    public OrderDto calculateTotal(UUID orderId) {
        OrderEntity order = getOrderOrThrow(orderId);

        OrderDto dto = orderMapper.toDto(order);

        Double productPrice = paymentClient.productCost(dto);
        dto.setProductPrice(productPrice);

        Double totalPrice = paymentClient.getTotalCost(dto);

        order.setProductPrice(productPrice);
        order.setTotalPrice(totalPrice);

        return orderMapper.toDto(orderRepository.save(order));
    }

    @Override
    @Transactional
    public OrderDto productReturn(UUID orderId) {
        OrderEntity order = getOrderOrThrow(orderId);

        warehouseClient.returnProducts(new ReturnProductsRequest(order.getProducts()));

        order.setOrderState(OrderState.PRODUCT_RETURNED);

        return orderMapper.toDto(orderRepository.save(order));
    }

    private OrderDto changeState(UUID orderId, OrderState orderState) {
        OrderEntity order = getOrderOrThrow(orderId);
        order.setOrderState(orderState);

        return orderMapper.toDto(orderRepository.save(order));
    }

    private OrderEntity getOrderOrThrow(UUID orderId) {
        if (orderId == null) {
            throw new IllegalArgumentException("Order id must not be null");
        }

        return orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException(orderId));
    }

    private void validateShoppingCart(ShoppingCartDto shoppingCartDto) {
        if (shoppingCartDto == null) {
            throw new IllegalArgumentException("Shopping cart must not be null");
        }

        if (shoppingCartDto.getUsername() == null || shoppingCartDto.getUsername().isBlank()) {
            throw new IllegalArgumentException("Username must not be blank");
        }

        if (shoppingCartDto.getProducts() == null || shoppingCartDto.getProducts().isEmpty()) {
            throw new IllegalArgumentException("Products must not be empty");
        }
    }

    private void applyBookedProducts(OrderEntity order, BookedProductsDto bookedProducts) {
        order.setDeliveryWeight(bookedProducts.getDeliveryWeight());
        order.setDeliveryVolume(bookedProducts.getDeliveryVolume());
        order.setFragile(bookedProducts.getFragile());
    }
}