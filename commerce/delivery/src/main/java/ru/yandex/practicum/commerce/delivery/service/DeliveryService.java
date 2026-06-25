package ru.yandex.practicum.commerce.delivery.service;

import ru.yandex.practicum.commerce.interactionapi.dto.delivery.DeliveryDto;

import java.util.UUID;

public interface DeliveryService {

    DeliveryDto planDelivery(DeliveryDto deliveryDto);

    Double deliveryCost(DeliveryDto deliveryDto);

    DeliveryDto picked(UUID deliveryId);

    DeliveryDto successfulDelivery(UUID deliveryId);

    DeliveryDto failedDelivery(UUID deliveryId);
}