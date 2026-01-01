package ru.practicum.shareit.error;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import ru.practicum.shareit.error.dto.ErrorResponse;
import ru.practicum.shareit.exception.*;

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

    // Обработка DataIntegrityViolationException (уникальные ограничения, внешние ключи и т.д.)
    @ExceptionHandler(DataIntegrityViolationException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ErrorResponse handleDataIntegrityViolation(DataIntegrityViolationException e,
                                                      HttpServletRequest request) {
        log.debug("DataIntegrityViolationException: {}", e.getMessage());

        // Анализируем сообщение для более понятной ошибки
        String errorMessage = extractUserFriendlyMessage(e);

        return ErrorResponse.of(
                "Conflict",
                errorMessage,
                HttpStatus.CONFLICT.value(),
                request.getRequestURI()
        );
    }

    // Обработка ошибок валидации DTO через @Valid
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleMethodArgumentNotValid(MethodArgumentNotValidException e,
                                                      HttpServletRequest request) {
        Map<String, String> errors = new HashMap<>();

        e.getBindingResult().getAllErrors().forEach(error -> {
            String fieldName = error instanceof FieldError
                    ? ((FieldError) error).getField()
                    : error.getObjectName();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });

        String errorMessage = errors.entrySet().stream()
                .map(entry -> String.format("Поле '%s': %s", entry.getKey(), entry.getValue()))
                .collect(Collectors.joining("; "));

        log.debug("Method argument validation failed: {}", errorMessage);

        return ErrorResponse.of(
                "Validation Failed",
                errorMessage,
                HttpStatus.BAD_REQUEST.value(),
                request.getRequestURI()
        );
    }

    @ExceptionHandler(ConstraintViolationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleConstraintViolation(ConstraintViolationException e,
                                                   HttpServletRequest request) {
        String errorMessage = e.getConstraintViolations().stream()
                .map(violation -> violation.getMessage())
                .collect(Collectors.joining("; "));

        log.debug("Constraint violation: {}", errorMessage);

        return ErrorResponse.of(
                "Invalid Parameters",
                errorMessage,
                HttpStatus.BAD_REQUEST.value(),
                request.getRequestURI()
        );
    }

    @ExceptionHandler(MissingRequestHeaderException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleMissingRequestHeader(MissingRequestHeaderException e,
                                                    HttpServletRequest request) {
        log.debug("Missing request header: {}", e.getMessage());

        return ErrorResponse.of(
                "Missing Required Header",
                String.format("Заголовок '%s' обязателен для этого запроса", e.getHeaderName()),
                HttpStatus.BAD_REQUEST.value(),
                request.getRequestURI()
        );
    }

    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ErrorResponse handleUnexpectedIllegalArgument(IllegalArgumentException e, HttpServletRequest request) {
        log.error("Unexpected IllegalArgumentException at {}: {}", request.getRequestURI(), e.getMessage(), e);
        return ErrorResponse.of(
                "Unexpected Illegal Argument",
                "Произошла непредвиденная ошибка в аргументах",
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                request.getRequestURI()
        );
    }

    @ExceptionHandler(IllegalStateException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ErrorResponse handleUnexpectedIllegalState(IllegalStateException e, HttpServletRequest request) {
        log.error("Unexpected IllegalStateException at {}: {}", request.getRequestURI(), e.getMessage(), e);
        return ErrorResponse.of(
                "Unexpected Illegal State",
                "Произошла непредвиденная ошибка состояния",
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
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

        // Для H2
        if (message.contains("users(email)")) {
            return "Пользователь с таким email уже существует";
        }

        // Для PostgreSQL
        if (message.contains("users_email_key") || message.contains("users_email_unique")) {
            return "Пользователь с таким email уже существует";
        }

        // Общие паттерны
        if (message.contains("EMAIL") || message.contains("email")) {
            return "Email уже используется другим пользователем";
        }

        if (message.contains("unique constraint") || message.contains("Unique index")) {
            return "Нарушение уникальности данных";
        }

        if (message.contains("foreign key constraint") || message.contains("REFERENCES")) {
            return "Нарушение ссылочной целостности";
        }

        if (message.contains("not-null") || message.contains("NOT NULL")) {
            return "Обязательное поле не может быть null";
        }

        return "Нарушение целостности данных";
    }
}