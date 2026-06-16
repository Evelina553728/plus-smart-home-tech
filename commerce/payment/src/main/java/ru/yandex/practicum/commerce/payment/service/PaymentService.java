package ru.yandex.practicum.commerce.payment.service;

import ru.yandex.practicum.commerce.interactionapi.dto.order.OrderDto;
import ru.yandex.practicum.commerce.interactionapi.dto.payment.PaymentDto;

import java.util.UUID;

public interface PaymentService {

    Double productCost(OrderDto orderDto);

    Double getTotalCost(OrderDto orderDto);

    PaymentDto payment(OrderDto orderDto);

    PaymentDto paymentSuccess(UUID paymentId);

    PaymentDto paymentFailed(UUID paymentId);
}