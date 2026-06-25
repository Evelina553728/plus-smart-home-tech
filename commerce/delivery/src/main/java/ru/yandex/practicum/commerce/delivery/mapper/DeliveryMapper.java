package ru.yandex.practicum.commerce.delivery.mapper;

import org.springframework.stereotype.Component;
import ru.yandex.practicum.commerce.delivery.model.DeliveryAddress;
import ru.yandex.practicum.commerce.delivery.model.DeliveryEntity;
import ru.yandex.practicum.commerce.interactionapi.dto.delivery.DeliveryDto;
import ru.yandex.practicum.commerce.interactionapi.dto.warehouse.AddressDto;

@Component
public class DeliveryMapper {

    public DeliveryEntity toEntity(DeliveryDto dto) {
        DeliveryEntity delivery = new DeliveryEntity();

        delivery.setDeliveryId(dto.getDeliveryId());
        delivery.setOrderId(dto.getOrderId());
        delivery.setFromAddress(toAddress(dto.getFromAddress()));
        delivery.setToAddress(toAddress(dto.getToAddress()));
        delivery.setDeliveryWeight(dto.getDeliveryWeight());
        delivery.setDeliveryVolume(dto.getDeliveryVolume());
        delivery.setFragile(dto.getFragile());
        delivery.setDeliveryState(dto.getDeliveryState());

        return delivery;
    }

    public DeliveryDto toDto(DeliveryEntity delivery) {
        DeliveryDto dto = new DeliveryDto();

        dto.setDeliveryId(delivery.getDeliveryId());
        dto.setOrderId(delivery.getOrderId());
        dto.setFromAddress(toAddressDto(delivery.getFromAddress()));
        dto.setToAddress(toAddressDto(delivery.getToAddress()));
        dto.setDeliveryWeight(delivery.getDeliveryWeight());
        dto.setDeliveryVolume(delivery.getDeliveryVolume());
        dto.setFragile(delivery.getFragile());
        dto.setDeliveryState(delivery.getDeliveryState());

        return dto;
    }

    private DeliveryAddress toAddress(AddressDto dto) {
        if (dto == null) {
            return null;
        }

        return new DeliveryAddress(
                dto.getCountry(),
                dto.getCity(),
                dto.getStreet(),
                dto.getHouse(),
                dto.getFlat()
        );
    }

    private AddressDto toAddressDto(DeliveryAddress address) {
        if (address == null) {
            return null;
        }

        return new AddressDto(
                address.getCountry(),
                address.getCity(),
                address.getStreet(),
                address.getHouse(),
                address.getFlat()
        );
    }
}