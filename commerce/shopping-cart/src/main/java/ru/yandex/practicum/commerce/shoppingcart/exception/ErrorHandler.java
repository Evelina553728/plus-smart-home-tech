package ru.yandex.practicum.commerce.shoppingcart.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import ru.yandex.practicum.commerce.interactionapi.dto.ErrorDto;

import java.util.stream.Collectors;

@RestControllerAdvice
public class ErrorHandler {

    @ExceptionHandler(ShoppingCartDeactivatedException.class)
    public ResponseEntity<ErrorDto> handleShoppingCartDeactivatedException(
            ShoppingCartDeactivatedException exception
    ) {
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(new ErrorDto("Shopping cart is deactivated", exception.getMessage()));
    }

    @ExceptionHandler(ProductNotFoundInShoppingCartException.class)
    public ResponseEntity<ErrorDto> handleProductNotFoundInShoppingCartException(
            ProductNotFoundInShoppingCartException exception
    ) {
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(new ErrorDto("Product not found in shopping cart", exception.getMessage()));
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