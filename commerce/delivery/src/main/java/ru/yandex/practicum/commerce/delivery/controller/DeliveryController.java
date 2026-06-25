package ru.yandex.practicum.commerce.delivery.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.commerce.delivery.service.DeliveryService;
import ru.yandex.practicum.commerce.interactionapi.dto.delivery.DeliveryDto;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/delivery")
public class DeliveryController {

    private final DeliveryService deliveryService;

    public DeliveryController(DeliveryService deliveryService) {
        this.deliveryService = deliveryService;
    }

    @PutMapping
    public ResponseEntity<DeliveryDto> planDelivery(@RequestBody DeliveryDto deliveryDto) {
        return ResponseEntity.ok(deliveryService.planDelivery(deliveryDto));
    }

    @PostMapping("/cost")
    public ResponseEntity<Double> deliveryCost(@RequestBody DeliveryDto deliveryDto) {
        return ResponseEntity.ok(deliveryService.deliveryCost(deliveryDto));
    }

    @PostMapping("/picked")
    public ResponseEntity<DeliveryDto> picked(@RequestBody UUID deliveryId) {
        return ResponseEntity.ok(deliveryService.picked(deliveryId));
    }

    @PostMapping("/successful")
    public ResponseEntity<DeliveryDto> successfulDelivery(@RequestBody UUID deliveryId) {
        return ResponseEntity.ok(deliveryService.successfulDelivery(deliveryId));
    }

    @PostMapping("/failed")
    public ResponseEntity<DeliveryDto> failedDelivery(@RequestBody UUID deliveryId) {
        return ResponseEntity.ok(deliveryService.failedDelivery(deliveryId));
    }
}