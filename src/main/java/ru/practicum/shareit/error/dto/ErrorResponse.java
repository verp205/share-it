package ru.practicum.shareit.error.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.practicum.shareit.exception.ApplicationException;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ErrorResponse {
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss");

    private String error;
    private String message;
    private String errorCode;
    private String timestamp;
    private String path;
    private int status;

    public ErrorResponse(String error, String message, int status) {
        this.error = error;
        this.message = message;
        this.status = status;
        this.timestamp = LocalDateTime.now().format(FORMATTER);
    }

    public ErrorResponse(String error, String message, int status, String path) {
        this(error, message, status);
        this.path = path;
    }

    public ErrorResponse(ApplicationException exception, int status, String path) {
        this.error = exception.getClass().getSimpleName();
        this.message = exception.getMessage();
        this.errorCode = exception.getErrorCode();
        this.status = status;
        this.path = path;
        this.timestamp = LocalDateTime.now().format(FORMATTER);
    }

    public static ErrorResponse of(String error, String message, int status) {
        return new ErrorResponse(error, message, status);
    }

    public static ErrorResponse of(String error, String message, int status, String path) {
        return new ErrorResponse(error, message, status, path);
    }

    public static ErrorResponse fromException(ApplicationException exception, int status, String path) {
        return new ErrorResponse(exception, status, path);
    }
}