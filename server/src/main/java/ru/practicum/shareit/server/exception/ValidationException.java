package ru.practicum.shareit.server.exception;

public class ValidationException extends ApplicationException {
    public ValidationException(String message) {
        super(message, "VALIDATION_ERROR");
    }

    public ValidationException(String field, String problem) {
        super(String.format("Поле '%s': %s", field, problem), "VALIDATION_ERROR");
    }
}