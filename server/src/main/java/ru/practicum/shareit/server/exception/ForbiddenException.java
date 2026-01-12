package ru.practicum.shareit.server.exception;

public class ForbiddenException extends ApplicationException {
    public ForbiddenException(String message) {
        super(message, "FORBIDDEN");
    }

    public ForbiddenException(String action, String entity, Long id) {
        super(String.format("Пользователь не может %s %s с ID %d", action, entity, id), "FORBIDDEN");
    }
}