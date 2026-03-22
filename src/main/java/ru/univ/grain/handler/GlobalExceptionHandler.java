package ru.univ.grain.handler;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import ru.univ.grain.dto.ApiError;
import ru.univ.grain.exception.BusinessException;
import ru.univ.grain.exception.DuplicateResourceException;
import ru.univ.grain.exception.ResourceNotFoundException;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    private ApiError buildApiError(HttpStatus status, String message, HttpServletRequest request) {
        return ApiError.builder()
                .status(status.value())
                .error(status.getReasonPhrase())
                .message(message)
                .path(request.getRequestURI())
                .timestamp(LocalDateTime.now())
                .build();
    }

    private ApiError buildValidationError(HttpServletRequest request, Map<String, String> errors) {
        return ApiError.builder()
                .status(HttpStatus.BAD_REQUEST.value())
                .error(HttpStatus.BAD_REQUEST.getReasonPhrase())
                .message("Ошибка валидации")
                .path(request.getRequestURI())
                .timestamp(LocalDateTime.now())
                .validationErrors(errors)
                .build();
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiError> handleResourceNotFound(
            ResourceNotFoundException ex,
            HttpServletRequest request) {

        log.info("Ресурс не найден: {} - {}", request.getRequestURI(), ex.getMessage());

        return new ResponseEntity<>(
                buildApiError(HttpStatus.NOT_FOUND, ex.getMessage(), request),
                HttpStatus.NOT_FOUND
        );
    }

    @ExceptionHandler(DuplicateResourceException.class)
    public ResponseEntity<ApiError> handleDuplicateResource(
            DuplicateResourceException ex,
            HttpServletRequest request) {

        log.warn("Дубликат ресурса: {} - {}", request.getRequestURI(), ex.getMessage());

        return new ResponseEntity<>(
                buildApiError(HttpStatus.CONFLICT, ex.getMessage(), request),
                HttpStatus.CONFLICT
        );
    }

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ApiError> handleBusinessException(
            BusinessException ex,
            HttpServletRequest request) {

        log.warn("Бизнес-ошибка: {} - {}", request.getRequestURI(), ex.getMessage());

        return new ResponseEntity<>(
                buildApiError(HttpStatus.BAD_REQUEST, ex.getMessage(), request),
                HttpStatus.BAD_REQUEST
        );
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> handleValidationExceptions(
            MethodArgumentNotValidException ex,
            HttpServletRequest request) {

        final Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach(error -> {
            final String fieldName = ((FieldError) error).getField();
            final String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });

        log.warn("Ошибка валидации: {} - {}", request.getRequestURI(), errors);

        return new ResponseEntity<>(
                buildValidationError(request, errors),
                HttpStatus.BAD_REQUEST
        );
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiError> handleTypeMismatch(
            MethodArgumentTypeMismatchException ex,
            HttpServletRequest request) {

        final String message = String.format("Параметр '%s' имеет неверный тип. Ожидается: %s",
                ex.getName(),
                ex.getRequiredType() != null ? ex.getRequiredType().getSimpleName() : "неизвестно");

        log.warn("Ошибка типа параметра: {} - {}", request.getRequestURI(), message);

        return new ResponseEntity<>(
                buildApiError(HttpStatus.BAD_REQUEST, message, request),
                HttpStatus.BAD_REQUEST
        );
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ApiError> handleMissingParams(
            MissingServletRequestParameterException ex,
            HttpServletRequest request) {

        final String message = String.format("Отсутствует обязательный параметр: %s", ex.getParameterName());

        log.warn("Отсутствует параметр: {} - {}", request.getRequestURI(), ex.getParameterName());

        return new ResponseEntity<>(
                buildApiError(HttpStatus.BAD_REQUEST, message, request),
                HttpStatus.BAD_REQUEST
        );
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiError> handleMessageNotReadable(
            HttpMessageNotReadableException ex,
            HttpServletRequest request) {

        log.warn("Неверный формат запроса: {} - {}", request.getRequestURI(), ex.getMessage());

        return new ResponseEntity<>(
                buildApiError(HttpStatus.BAD_REQUEST, "Неверный формат запроса. Проверьте структуру JSON", request),
                HttpStatus.BAD_REQUEST
        );
    }

    @ExceptionHandler(org.springframework.dao.DataIntegrityViolationException.class)
    public ResponseEntity<ApiError> handleDataIntegrityViolation(
            org.springframework.dao.DataIntegrityViolationException ex,
            HttpServletRequest request) {

        String message = "Нарушение целостности данных";

        if (ex.getMessage() != null && ex.getMessage().contains("unique")) {
            message = "Запись с такими данными уже существует";
        }

        log.error("Нарушение целостности: {} - {}", request.getRequestURI(), ex.getMessage());

        return new ResponseEntity<>(
                buildApiError(HttpStatus.CONFLICT, message, request),
                HttpStatus.CONFLICT
        );
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> handleGenericException(
            Exception ex,
            HttpServletRequest request) {

        log.error("Внутренняя ошибка сервера: {} - {}", request.getRequestURI(), ex.getMessage(), ex);

        return new ResponseEntity<>(
                buildApiError(HttpStatus.INTERNAL_SERVER_ERROR, "Внутренняя ошибка сервера", request),
                HttpStatus.INTERNAL_SERVER_ERROR
        );
    }
}
