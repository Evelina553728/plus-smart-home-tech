package ru.yandex.practicum.commerce.warehouse.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.bind.MissingServletRequestParameterException;
import ru.yandex.practicum.commerce.interactionapi.dto.ErrorDto;

import java.util.stream.Collectors;

@RestControllerAdvice
public class ErrorHandler {

    @ExceptionHandler(ProductAlreadyExistsInWarehouseException.class)
    public ResponseEntity<ErrorDto> handleProductAlreadyExistsInWarehouseException(
            ProductAlreadyExistsInWarehouseException exception
    ) {
        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(new ErrorDto("Product already exists in warehouse", exception.getMessage()));
    }

    @ExceptionHandler(ProductNotFoundInWarehouseException.class)
    public ResponseEntity<ErrorDto> handleProductNotFoundInWarehouseException(
            ProductNotFoundInWarehouseException exception
    ) {
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(new ErrorDto("Product not found in warehouse", exception.getMessage()));
    }

    @ExceptionHandler(ProductInShoppingCartLowQuantityInWarehouseException.class)
    public ResponseEntity<ErrorDto> handleProductInShoppingCartLowQuantityInWarehouseException(
            ProductInShoppingCartLowQuantityInWarehouseException exception
    ) {
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(new ErrorDto("Not enough product in warehouse", exception.getMessage()));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorDto> handleIllegalArgumentException(IllegalArgumentException exception) {
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(new ErrorDto("Invalid request", exception.getMessage()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorDto> handleMethodArgumentNotValidException(
            MethodArgumentNotValidException exception
    ) {
        String description = exception.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .collect(Collectors.joining("; "));

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(new ErrorDto("Validation error", description));
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ErrorDto> handleMissingServletRequestParameterException(
            MissingServletRequestParameterException exception
    ) {
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(new ErrorDto(
                        "Missing request parameter",
                        "Required parameter is missing: " + exception.getParameterName()
                ));
    }
}