package ru.yandex.practicum.commerce.interactionapi.dto.warehouse;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.util.UUID;

public class NewProductInWarehouseRequest {

    @NotNull
    private UUID productId;

    private boolean fragile;

    @Valid
    @NotNull
    private DimensionDto dimension;

    @NotNull
    @Positive
    private Double weight;

    public NewProductInWarehouseRequest() {
    }

    public UUID getProductId() {
        return productId;
    }

    public void setProductId(UUID productId) {
        this.productId = productId;
    }

    public boolean isFragile() {
        return fragile;
    }

    public void setFragile(boolean fragile) {
        this.fragile = fragile;
    }

    public DimensionDto getDimension() {
        return dimension;
    }

    public void setDimension(DimensionDto dimension) {
        this.dimension = dimension;
    }

    public Double getWeight() {
        return weight;
    }

    public void setWeight(Double weight) {
        this.weight = weight;
    }
}