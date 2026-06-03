package ru.yandex.practicum.commerce.interactionapi.dto.warehouse;

public class BookedProductsDto {

    private Double deliveryWeight;

    private Double deliveryVolume;

    private Boolean fragile;

    public BookedProductsDto() {
    }

    public BookedProductsDto(Double deliveryWeight, Double deliveryVolume, Boolean fragile) {
        this.deliveryWeight = deliveryWeight;
        this.deliveryVolume = deliveryVolume;
        this.fragile = fragile;
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
}