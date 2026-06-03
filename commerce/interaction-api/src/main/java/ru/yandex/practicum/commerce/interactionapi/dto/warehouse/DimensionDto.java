package ru.yandex.practicum.commerce.interactionapi.dto.warehouse;

import jakarta.validation.constraints.Positive;

public class DimensionDto {

    @Positive
    private Double width;

    @Positive
    private Double height;

    @Positive
    private Double depth;

    public DimensionDto() {
    }

    public DimensionDto(Double width, Double height, Double depth) {
        this.width = width;
        this.height = height;
        this.depth = depth;
    }

    public Double getWidth() {
        return width;
    }

    public void setWidth(Double width) {
        this.width = width;
    }

    public Double getHeight() {
        return height;
    }

    public void setHeight(Double height) {
        this.height = height;
    }

    public Double getDepth() {
        return depth;
    }

    public void setDepth(Double depth) {
        this.depth = depth;
    }
}