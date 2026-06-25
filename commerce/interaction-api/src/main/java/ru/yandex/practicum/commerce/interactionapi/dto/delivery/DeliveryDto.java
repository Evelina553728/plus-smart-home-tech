package ru.yandex.practicum.commerce.interactionapi.dto.delivery;

import ru.yandex.practicum.commerce.interactionapi.dto.warehouse.AddressDto;

import java.util.UUID;

public class DeliveryDto {

    private UUID deliveryId;
    private UUID orderId;
    private AddressDto fromAddress;
    private AddressDto toAddress;
    private Double deliveryWeight;
    private Double deliveryVolume;
    private Boolean fragile;
    private DeliveryState deliveryState;

    public DeliveryDto() {
    }

    public UUID getDeliveryId() {
        return deliveryId;
    }

    public void setDeliveryId(UUID deliveryId) {
        this.deliveryId = deliveryId;
    }

    public UUID getOrderId() {
        return orderId;
    }

    public void setOrderId(UUID orderId) {
        this.orderId = orderId;
    }

    public AddressDto getFromAddress() {
        return fromAddress;
    }

    public void setFromAddress(AddressDto fromAddress) {
        this.fromAddress = fromAddress;
    }

    public AddressDto getToAddress() {
        return toAddress;
    }

    public void setToAddress(AddressDto toAddress) {
        this.toAddress = toAddress;
    }

    public Double getDeliveryWeight() {
        return deliveryWeight;
    }

    public void setDeliveryWeight(Double deliveryWeight) {
        this.deliveryWeight = deliveryWeight;
    }

    public Double getDeliveryVolume() {
        return deliveryVolume;
    }

    public void setDeliveryVolume(Double deliveryVolume) {
        this.deliveryVolume = deliveryVolume;
    }

    public Boolean getFragile() {
        return fragile;
    }

    public void setFragile(Boolean fragile) {
        this.fragile = fragile;
    }

    public DeliveryState getDeliveryState() {
        return deliveryState;
    }

    public void setDeliveryState(DeliveryState deliveryState) {
        this.deliveryState = deliveryState;
    }
}