package ru.yandex.practicum.commerce.interactionapi.dto;

public class ErrorDto {

    private String error;
    private String description;

    public ErrorDto() {
    }

    public ErrorDto(String error, String description) {
        this.error = error;
        this.description = description;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}