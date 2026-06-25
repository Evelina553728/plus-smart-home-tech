package ru.yandex.practicum.commerce.interactionapi.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.commerce.interactionapi.dto.delivery.DeliveryDto;

import java.util.UUID;

@FeignClient(name = "delivery")
public interface DeliveryClient {

    @PutMapping("/api/v1/delivery")
    DeliveryDto planDelivery(@RequestBody DeliveryDto deliveryDto);

    @PostMapping("/api/v1/delivery/cost")
    Double deliveryCost(@RequestBody DeliveryDto deliveryDto);

    @PostMapping("/api/v1/delivery/picked")
    DeliveryDto picked(@RequestBody UUID deliveryId);

    @PostMapping("/api/v1/delivery/successful")
    DeliveryDto successfulDelivery(@RequestBody UUID deliveryId);

    @PostMapping("/api/v1/delivery/failed")
    DeliveryDto failedDelivery(@RequestBody UUID deliveryId);
}