package ru.practicum.shareit.server.exception;

public class ConflictException extends ApplicationException {
    public ConflictException(String message) {
        super(message, "CONFLICT");
    }

    public ConflictException(String entity, String field, Object value) {
        super(String.format("%s с %s '%s' уже существует", entity, field, value), "CONFLICT");
    }
}