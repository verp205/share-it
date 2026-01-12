package ru.practicum.shareit.server.error;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.exception.ConstraintViolationException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import ru.practicum.shareit.server.error.dto.ErrorResponse;
import ru.practicum.shareit.server.exception.*;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@RestControllerAdvice
public class ErrorHandler {

    @ExceptionHandler(NotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErrorResponse handleNotFound(NotFoundException e, HttpServletRequest request) {
        log.debug("NotFoundException: {}", e.getMessage());
        return ErrorResponse.fromException(e, HttpStatus.NOT_FOUND.value(), request.getRequestURI());
    }

    @ExceptionHandler(ValidationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleValidation(ValidationException e, HttpServletRequest request) {
        log.debug("ValidationException: {}", e.getMessage());
        return ErrorResponse.fromException(e, HttpStatus.BAD_REQUEST.value(), request.getRequestURI());
    }

    @ExceptionHandler(ConflictException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ErrorResponse handleConflict(ConflictException e, HttpServletRequest request) {
        log.debug("ConflictException: {}", e.getMessage());
        return ErrorResponse.fromException(e, HttpStatus.CONFLICT.value(), request.getRequestURI());
    }

    @ExceptionHandler(ForbiddenException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public ErrorResponse handleForbidden(ForbiddenException e, HttpServletRequest request) {
        log.debug("ForbiddenException: {}", e.getMessage());
        return ErrorResponse.fromException(e, HttpStatus.FORBIDDEN.value(), request.getRequestURI());
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ErrorResponse handleDataIntegrityViolation(DataIntegrityViolationException e,
                                                      HttpServletRequest request) {
        log.debug("DataIntegrityViolationException: {}", e.getMessage());

        return ErrorResponse.of(
                "Conflict",
                extractUserFriendlyMessage(e),
                HttpStatus.CONFLICT.value(),
                request.getRequestURI()
        );
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleMethodArgumentNotValid(MethodArgumentNotValidException e,
                                                      HttpServletRequest request) {

        Map<String, String> errors = new HashMap<>();
        e.getBindingResult().getAllErrors().forEach(error -> {
            String field = error instanceof FieldError
                    ? ((FieldError) error).getField()
                    : error.getObjectName();
            errors.put(field, error.getDefaultMessage());
        });

        String message = errors.entrySet().stream()
                .map(entry -> String.format("Поле '%s': %s", entry.getKey(), entry.getValue()))
                .collect(Collectors.joining("; "));

        log.debug("DTO validation failed: {}", message);

        return ErrorResponse.of(
                "Validation Failed",
                message,
                HttpStatus.BAD_REQUEST.value(),
                request.getRequestURI()
        );
    }

    @ExceptionHandler(ConstraintViolationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleConstraintViolation(ConstraintViolationException e,
                                                   HttpServletRequest request) {

        String message = e.getSQLException() != null ? e.getSQLException().getMessage() : e.getMessage();

        log.debug("Database constraint violation: {}", message);

        return ErrorResponse.of(
                "Invalid Parameters",
                message,
                HttpStatus.BAD_REQUEST.value(),
                request.getRequestURI()
        );
    }

    @ExceptionHandler(MissingRequestHeaderException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErrorResponse handleMissingRequestHeader(MissingRequestHeaderException e,
                                                    HttpServletRequest request) {

        log.debug("Missing header: {}", e.getHeaderName());

        return ErrorResponse.of(
                "Not Found",
                "Пользователь не найден",
                HttpStatus.NOT_FOUND.value(),
                request.getRequestURI()
        );
    }

    @ExceptionHandler({IllegalArgumentException.class, IllegalStateException.class})
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleIllegalStateOrArgument(RuntimeException e,
                                                      HttpServletRequest request) {

        log.debug("Bad request: {}", e.getMessage());

        return ErrorResponse.of(
                "Bad Request",
                e.getMessage(),
                HttpStatus.BAD_REQUEST.value(),
                request.getRequestURI()
        );
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ErrorResponse handleGenericException(Exception e, HttpServletRequest request) {
        log.error("Unexpected exception at {}: {}", request.getRequestURI(), e.getMessage(), e);

        return ErrorResponse.of(
                "Internal Server Error",
                "Произошла непредвиденная ошибка",
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                request.getRequestURI()
        );
    }

    private String extractUserFriendlyMessage(DataIntegrityViolationException e) {
        String message = e.getMessage();
        if (message == null) {
            return "Нарушение целостности данных";
        }

        if (message.contains("users_email") || message.contains("users(email)")) {
            return "Пользователь с таким email уже существует";
        }

        if (message.toLowerCase().contains("email")) {
            return "Email уже используется";
        }

        if (message.toLowerCase().contains("unique")) {
            return "Нарушение уникальности данных";
        }

        return "Нарушение целостности данных";
    }
}
