package ru.yandex.practicum.commerce.delivery.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

import java.util.Objects;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class DeliveryServiceImpl implements DeliveryService {

    private static final Logger log = LoggerFactory.getLogger(DeliveryServiceImpl.class);

    private static final double BASE_COST = 5.0;
    private static final double FRAGILE_RATE = 0.2;
    private static final double WEIGHT_RATE = 0.3;
    private static final double VOLUME_RATE = 0.2;
    private static final double DIFFERENT_STREET_RATE = 0.2;

    private static final String ADDRESS_2 = "ADDRESS_2";

    private static final double ADDRESS_2_MULTIPLIER = 2.0;
    private static final double DEFAULT_ADDRESS_MULTIPLIER = 1.0;

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

        log.info("Planning delivery for orderId={}", deliveryDto.getOrderId());

        DeliveryEntity delivery = deliveryMapper.toEntity(deliveryDto);
        delivery.setDeliveryState(DeliveryState.CREATED);

        DeliveryEntity savedDelivery = deliveryRepository.save(delivery);

        log.info(
                "Delivery planned: deliveryId={}, orderId={}",
                savedDelivery.getDeliveryId(),
                savedDelivery.getOrderId()
        );

        return deliveryMapper.toDto(savedDelivery);
    }

    @Override
    public Double deliveryCost(DeliveryDto deliveryDto) {
        validateDelivery(deliveryDto);

        log.info("Calculating delivery cost for orderId={}", deliveryDto.getOrderId());

        double result = BASE_COST;

        double warehouseAddressMultiplier = getWarehouseAddressMultiplier(deliveryDto.getFromAddress());
        result += BASE_COST * warehouseAddressMultiplier;

        if (Boolean.TRUE.equals(deliveryDto.getFragile())) {
            result += result * FRAGILE_RATE;
        }

        double weightCost = safeDouble(deliveryDto.getDeliveryWeight()) * WEIGHT_RATE;
        result += weightCost;

        double volumeCost = safeDouble(deliveryDto.getDeliveryVolume()) * VOLUME_RATE;
        result += volumeCost;

        boolean sameStreet = sameStreet(deliveryDto.getFromAddress(), deliveryDto.getToAddress());

        if (!sameStreet) {
            result += result * DIFFERENT_STREET_RATE;
        }

        log.info(
                "Delivery cost calculated for orderId={}: cost={}, warehouseAddressMultiplier={}, weightCost={}, volumeCost={}, fragile={}, sameStreet={}",
                deliveryDto.getOrderId(),
                result,
                warehouseAddressMultiplier,
                weightCost,
                volumeCost,
                deliveryDto.getFragile(),
                sameStreet
        );

        return result;
    }

    @Override
    @Transactional
    public DeliveryDto picked(UUID deliveryId) {
        DeliveryEntity delivery = getDeliveryOrThrow(deliveryId);

        log.info("Picking delivery: deliveryId={}, orderId={}", deliveryId, delivery.getOrderId());

        delivery.setDeliveryState(DeliveryState.IN_PROGRESS);

        warehouseClient.shippedToDelivery(
                new ShippedToDeliveryRequest(delivery.getOrderId(), delivery.getDeliveryId())
        );

        DeliveryEntity savedDelivery = deliveryRepository.save(delivery);

        log.info(
                "Delivery picked: deliveryId={}, orderId={}, state={}",
                savedDelivery.getDeliveryId(),
                savedDelivery.getOrderId(),
                savedDelivery.getDeliveryState()
        );

        return deliveryMapper.toDto(savedDelivery);
    }

    @Override
    @Transactional
    public DeliveryDto successfulDelivery(UUID deliveryId) {
        DeliveryEntity delivery = getDeliveryOrThrow(deliveryId);

        log.info("Marking delivery as successful: deliveryId={}, orderId={}", deliveryId, delivery.getOrderId());

        delivery.setDeliveryState(DeliveryState.DELIVERED);
        DeliveryEntity savedDelivery = deliveryRepository.save(delivery);

        orderClient.delivery(savedDelivery.getOrderId());

        log.info(
                "Delivery marked as successful: deliveryId={}, orderId={}, state={}",
                savedDelivery.getDeliveryId(),
                savedDelivery.getOrderId(),
                savedDelivery.getDeliveryState()
        );

        return deliveryMapper.toDto(savedDelivery);
    }

    @Override
    @Transactional
    public DeliveryDto failedDelivery(UUID deliveryId) {
        DeliveryEntity delivery = getDeliveryOrThrow(deliveryId);

        log.info("Marking delivery as failed: deliveryId={}, orderId={}", deliveryId, delivery.getOrderId());

        delivery.setDeliveryState(DeliveryState.FAILED);
        DeliveryEntity savedDelivery = deliveryRepository.save(delivery);

        orderClient.deliveryFailed(savedDelivery.getOrderId());

        log.info(
                "Delivery marked as failed: deliveryId={}, orderId={}, state={}",
                savedDelivery.getDeliveryId(),
                savedDelivery.getOrderId(),
                savedDelivery.getDeliveryState()
        );

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

    private double getWarehouseAddressMultiplier(AddressDto fromAddress) {
        if (addressContains(fromAddress, ADDRESS_2)) {
            return ADDRESS_2_MULTIPLIER;
        }

        return DEFAULT_ADDRESS_MULTIPLIER;
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

        return Objects.equals(first.getStreet(), second.getStreet());
    }

    private double safeDouble(Double value) {
        return value == null ? 0.0 : value;
    }
}