package ru.practicum.shareit.exception;

public class NotFoundException extends ApplicationException {
    public NotFoundException(String message) {
        super(message, "NOT_FOUND");
    }

    public NotFoundException(String entity, Long id) {
        super(String.format("%s с ID %d не найден", entity, id), "NOT_FOUND");
    }

    public NotFoundException(String entity, String identifier) {
        super(String.format("%s '%s' не найден", entity, identifier), "NOT_FOUND");
    }
}