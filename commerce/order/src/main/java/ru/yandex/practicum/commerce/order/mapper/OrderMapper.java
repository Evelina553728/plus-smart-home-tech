package ru.yandex.practicum.commerce.order.mapper;

import org.springframework.stereotype.Component;
import ru.yandex.practicum.commerce.interactionapi.dto.order.OrderDto;
import ru.yandex.practicum.commerce.interactionapi.dto.warehouse.AddressDto;
import ru.yandex.practicum.commerce.order.model.OrderAddress;
import ru.yandex.practicum.commerce.order.model.OrderEntity;

import java.util.HashMap;

@Component
public class OrderMapper {

    public OrderDto toDto(OrderEntity order) {
        OrderDto dto = new OrderDto();

        dto.setOrderId(order.getOrderId());
        dto.setUsername(order.getUsername());
        dto.setShoppingCartId(order.getShoppingCartId());
        dto.setProducts(new HashMap<>(order.getProducts()));
        dto.setPaymentId(order.getPaymentId());
        dto.setDeliveryId(order.getDeliveryId());
        dto.setOrderState(order.getOrderState());
        dto.setDeliveryWeight(order.getDeliveryWeight());
        dto.setDeliveryVolume(order.getDeliveryVolume());
        dto.setFragile(order.getFragile());
        dto.setTotalPrice(order.getTotalPrice());
        dto.setProductPrice(order.getProductPrice());
        dto.setDeliveryPrice(order.getDeliveryPrice());
        dto.setDeliveryAddress(toAddressDto(order.getDeliveryAddress()));

        return dto;
    }

    public OrderAddress toAddress(AddressDto dto) {
        if (dto == null) {
            return null;
        }

        return new OrderAddress(
                dto.getCountry(),
                dto.getCity(),
                dto.getStreet(),
                dto.getHouse(),
                dto.getFlat()
        );
    }

    private AddressDto toAddressDto(OrderAddress address) {
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