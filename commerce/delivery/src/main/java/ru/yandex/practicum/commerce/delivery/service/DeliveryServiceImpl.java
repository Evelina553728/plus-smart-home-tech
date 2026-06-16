package ru.yandex.practicum.commerce.delivery.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.commerce.delivery.exception.DeliveryNotFoundException;
import ru.yandex.practicum.commerce.delivery.mapper.DeliveryMapper;
import ru.yandex.practicum.commerce.delivery.model.DeliveryEntity;
import ru.yandex.practicum.commerce.delivery.repository.DeliveryRepository;
import ru.yandex.practicum.commerce.interactionapi.client.OrderClient;
import ru.yandex.practicum.commerce.interactionapi.client.WarehouseClient;
import ru.yandex.practicum.commerce.interactionapi.dto.delivery.DeliveryDto;
import ru.yandex.practicum.commerce.interactionapi.dto.delivery.DeliveryState;
import ru.yandex.practicum.commerce.interactionapi.dto.warehouse.AddressDto;
import ru.yandex.practicum.commerce.interactionapi.dto.warehouse.ShippedToDeliveryRequest;

import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class DeliveryServiceImpl implements DeliveryService {

    private static final double BASE_COST = 5.0;
    private static final double FRAGILE_RATE = 0.2;
    private static final double WEIGHT_RATE = 0.3;
    private static final double VOLUME_RATE = 0.2;
    private static final double DIFFERENT_STREET_RATE = 0.2;

    private final DeliveryRepository deliveryRepository;
    private final DeliveryMapper deliveryMapper;
    private final WarehouseClient warehouseClient;
    private final OrderClient orderClient;

    public DeliveryServiceImpl(DeliveryRepository deliveryRepository,
                               DeliveryMapper deliveryMapper,
                               WarehouseClient warehouseClient,
                               OrderClient orderClient) {
        this.deliveryRepository = deliveryRepository;
        this.deliveryMapper = deliveryMapper;
        this.warehouseClient = warehouseClient;
        this.orderClient = orderClient;
    }

    @Override
    @Transactional
    public DeliveryDto planDelivery(DeliveryDto deliveryDto) {
        validateDelivery(deliveryDto);

        DeliveryEntity delivery = deliveryMapper.toEntity(deliveryDto);
        delivery.setDeliveryState(DeliveryState.CREATED);

        return deliveryMapper.toDto(deliveryRepository.save(delivery));
    }

    @Override
    public Double deliveryCost(DeliveryDto deliveryDto) {
        validateDelivery(deliveryDto);

        double result = BASE_COST;

        AddressDto fromAddress = deliveryDto.getFromAddress();

        if (addressContains(fromAddress, "ADDRESS_2")) {
            result += BASE_COST * 2;
        } else {
            result += BASE_COST;
        }

        if (Boolean.TRUE.equals(deliveryDto.getFragile())) {
            result += result * FRAGILE_RATE;
        }

        result += safeDouble(deliveryDto.getDeliveryWeight()) * WEIGHT_RATE;
        result += safeDouble(deliveryDto.getDeliveryVolume()) * VOLUME_RATE;

        if (!sameStreet(deliveryDto.getFromAddress(), deliveryDto.getToAddress())) {
            result += result * DIFFERENT_STREET_RATE;
        }

        return result;
    }

    @Override
    @Transactional
    public DeliveryDto picked(UUID deliveryId) {
        DeliveryEntity delivery = getDeliveryOrThrow(deliveryId);

        delivery.setDeliveryState(DeliveryState.IN_PROGRESS);

        warehouseClient.shippedToDelivery(
                new ShippedToDeliveryRequest(delivery.getOrderId(), delivery.getDeliveryId())
        );

        return deliveryMapper.toDto(deliveryRepository.save(delivery));
    }

    @Override
    @Transactional
    public DeliveryDto successfulDelivery(UUID deliveryId) {
        DeliveryEntity delivery = getDeliveryOrThrow(deliveryId);

        delivery.setDeliveryState(DeliveryState.DELIVERED);
        DeliveryEntity savedDelivery = deliveryRepository.save(delivery);

        orderClient.delivery(savedDelivery.getOrderId());

        return deliveryMapper.toDto(savedDelivery);
    }

    @Override
    @Transactional
    public DeliveryDto failedDelivery(UUID deliveryId) {
        DeliveryEntity delivery = getDeliveryOrThrow(deliveryId);

        delivery.setDeliveryState(DeliveryState.FAILED);
        DeliveryEntity savedDelivery = deliveryRepository.save(delivery);

        orderClient.deliveryFailed(savedDelivery.getOrderId());

        return deliveryMapper.toDto(savedDelivery);
    }

    private DeliveryEntity getDeliveryOrThrow(UUID deliveryId) {
        if (deliveryId == null) {
            throw new IllegalArgumentException("Delivery id must not be null");
        }

        return deliveryRepository.findById(deliveryId)
                .orElseThrow(() -> new DeliveryNotFoundException(deliveryId));
    }

    private void validateDelivery(DeliveryDto deliveryDto) {
        if (deliveryDto == null) {
            throw new IllegalArgumentException("Delivery must not be null");
        }

        if (deliveryDto.getOrderId() == null) {
            throw new IllegalArgumentException("Order id must not be null");
        }

        if (deliveryDto.getFromAddress() == null) {
            throw new IllegalArgumentException("From address must not be null");
        }

        if (deliveryDto.getToAddress() == null) {
            throw new IllegalArgumentException("To address must not be null");
        }
    }

    private boolean addressContains(AddressDto address, String value) {
        if (address == null || value == null) {
            return false;
        }

        return contains(address.getCountry(), value)
                || contains(address.getCity(), value)
                || contains(address.getStreet(), value)
                || contains(address.getHouse(), value)
                || contains(address.getFlat(), value);
    }

    private boolean contains(String source, String value) {
        return source != null && source.contains(value);
    }

    private boolean sameStreet(AddressDto first, AddressDto second) {
        if (first == null || second == null) {
            return false;
        }

        String firstStreet = first.getStreet();
        String secondStreet = second.getStreet();

        return firstStreet != null && firstStreet.equals(secondStreet);
    }

    private double safeDouble(Double value) {
        return value == null ? 0.0 : value;
    }
}