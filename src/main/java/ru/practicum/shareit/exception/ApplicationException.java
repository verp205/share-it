package ru.practicum.shareit.exception;

import lombok.Getter;

@Getter
public abstract class ApplicationException extends RuntimeException {
    private final String errorCode;

    public ApplicationException(String message, String errorCode) {
        super(message);
        this.errorCode = errorCode;
    }

    public ApplicationException(String message, String errorCode, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
    }
}