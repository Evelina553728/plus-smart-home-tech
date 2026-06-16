package ru.yandex.practicum.commerce.order.service;

import ru.yandex.practicum.commerce.interactionapi.dto.cart.ShoppingCartDto;
import ru.yandex.practicum.commerce.interactionapi.dto.order.OrderDto;

import java.util.List;
import java.util.UUID;

public interface OrderService {

    OrderDto createNewOrder(ShoppingCartDto shoppingCartDto);

    List<OrderDto> getUserOrders(String username);

    OrderDto assembly(UUID orderId);

    OrderDto assemblyFailed(UUID orderId);

    OrderDto payment(UUID orderId);

    OrderDto paymentSuccess(UUID orderId);

    OrderDto paymentFailed(UUID orderId);

    OrderDto delivery(UUID orderId);

    OrderDto deliveryFailed(UUID orderId);

    OrderDto completed(UUID orderId);

    OrderDto calculateDelivery(UUID orderId);

    OrderDto calculateTotal(UUID orderId);

    OrderDto productReturn(UUID orderId);
}