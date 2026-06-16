package ru.yandex.practicum.commerce.delivery.controller;

import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import ru.yandex.practicum.commerce.delivery.service.DeliveryService;
import ru.yandex.practicum.commerce.interactionapi.client.DeliveryClient;
import ru.yandex.practicum.commerce.interactionapi.dto.delivery.DeliveryDto;

import java.util.UUID;

@RestController
public class DeliveryController implements DeliveryClient {

    private final DeliveryService deliveryService;

    public DeliveryController(DeliveryService deliveryService) {
        this.deliveryService = deliveryService;
    }

    @Override
    public DeliveryDto planDelivery(@RequestBody DeliveryDto deliveryDto) {
        return deliveryService.planDelivery(deliveryDto);
    }

    @Override
    public Double deliveryCost(@RequestBody DeliveryDto deliveryDto) {
        return deliveryService.deliveryCost(deliveryDto);
    }

    @Override
    public DeliveryDto picked(@RequestBody UUID deliveryId) {
        return deliveryService.picked(deliveryId);
    }

    @Override
    public DeliveryDto successfulDelivery(@RequestBody UUID deliveryId) {
        return deliveryService.successfulDelivery(deliveryId);
    }

    @Override
    public DeliveryDto failedDelivery(@RequestBody UUID deliveryId) {
        return deliveryService.failedDelivery(deliveryId);
    }
}