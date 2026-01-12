package ru.practicum.shareit.server.exception;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import ru.practicum.shareit.server.error.dto.ErrorResponse;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(NotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErrorResponse handleNotFoundException(NotFoundException e, HttpServletRequest request) {
        log.debug("Not found error: {}", e.getMessage());
        return ErrorResponse.fromException(e, HttpStatus.NOT_FOUND.value(), request.getRequestURI());
    }

    @ExceptionHandler(ValidationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleValidationException(ValidationException e, HttpServletRequest request) {
        log.debug("Validation error: {}", e.getMessage());
        return ErrorResponse.fromException(e, HttpStatus.BAD_REQUEST.value(), request.getRequestURI());
    }

    @ExceptionHandler(ForbiddenException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public ErrorResponse handleForbiddenException(ForbiddenException e, HttpServletRequest request) {
        log.debug("Forbidden error: {}", e.getMessage());
        return ErrorResponse.fromException(e, HttpStatus.FORBIDDEN.value(), request.getRequestURI());
    }

    @ExceptionHandler(ConflictException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ErrorResponse handleConflictException(ConflictException e, HttpServletRequest request) {
        log.debug("Conflict error: {}", e.getMessage());
        return ErrorResponse.fromException(e, HttpStatus.CONFLICT.value(), request.getRequestURI());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleMethodArgumentNotValidException(MethodArgumentNotValidException e,
                                                               HttpServletRequest request) {
        String errorMessage = e.getBindingResult().getFieldErrors().stream()
                .map(fieldError -> String.format("Поле '%s': %s",
                        fieldError.getField(), fieldError.getDefaultMessage()))
                .findFirst()
                .orElse(e.getMessage());

        log.debug("Validation error: {}", errorMessage);
        return ErrorResponse.of("MethodArgumentNotValidException",
                errorMessage,
                HttpStatus.BAD_REQUEST.value(),
                request.getRequestURI());
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ErrorResponse handleDataIntegrityViolationException(DataIntegrityViolationException e,
                                                               HttpServletRequest request) {
        log.debug("Data integrity violation: {}", e.getMessage());
        String message = "Нарушение целостности данных. Возможно, дублирующееся значение уникального поля.";
        return ErrorResponse.of("DataIntegrityViolationException",
                message,
                HttpStatus.CONFLICT.value(),
                request.getRequestURI());
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ErrorResponse handleException(Exception e, HttpServletRequest request) {
        log.error("Internal server error", e);
        return ErrorResponse.of("InternalServerError",
                "Внутренняя ошибка сервера",
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                request.getRequestURI());
    }
}