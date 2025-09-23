package com.kltn.scsms_api_service.core.utils;

import com.kltn.scsms_api_service.core.dto.response.ApiResponse;
import com.kltn.scsms_api_service.core.dto.response.ErrorResponse;
import com.kltn.scsms_api_service.core.dto.response.PaginatedResponse;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class ResponseBuilder {

    // Success responses
    public static <T> ResponseEntity<ApiResponse<T>> success(T data) {
        return ResponseEntity.ok(ApiResponse.success(data));
    }

    public static <T> ResponseEntity<ApiResponse<T>> success(String message, T data) {
        return ResponseEntity.ok(ApiResponse.success(message, data));
    }

    public static ResponseEntity<ApiResponse<Void>> success(String message) {
        return ResponseEntity.ok(ApiResponse.success(message));
    }

    public static <T> ResponseEntity<ApiResponse<T>> success(String message, T data, Object metadata) {
        return ResponseEntity.ok(ApiResponse.success(message, data, metadata));
    }

    // Created responses
    public static <T> ResponseEntity<ApiResponse<T>> created(String message, T data) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(message, data));
    }

    public static <T> ResponseEntity<ApiResponse<T>> created(T data) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Resource created successfully", data));
    }

    // Error responses
    public static <T> ResponseEntity<ApiResponse<T>> badRequest(String message) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error(message));
    }

    public static <T> ResponseEntity<ApiResponse<T>> unauthorized(String message) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(ApiResponse.error(message));
    }

    public static <T> ResponseEntity<ApiResponse<T>> forbidden(String message) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(ApiResponse.error(message));
    }

    public static <T> ResponseEntity<ApiResponse<T>> notFound(String message) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.error(message));
    }

    public static <T> ResponseEntity<ApiResponse<T>> conflict(String message) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(ApiResponse.error(message));
    }

    public static <T> ResponseEntity<ApiResponse<T>> internalServerError(String message) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error(message));
    }

    // Custom status responses
    public static <T> ResponseEntity<ApiResponse<T>> status(HttpStatus status, String message, T data) {
        ApiResponse<T> response = status.is2xxSuccessful()
                ? ApiResponse.success(message, data)
                : ApiResponse.error(message);
        return ResponseEntity.status(status).body(response);
    }

    public static ResponseEntity<ApiResponse<?>> status(HttpStatus status, String message) {
        ApiResponse<?> response = status.is2xxSuccessful()
                ? ApiResponse.success(message)
                : ApiResponse.error(message);
        return ResponseEntity.status(status).body(response);
    }

    // Paginated responses
    public static <T> ResponseEntity<ApiResponse<PaginatedResponse<T>>> paginated(
            String message, Page<T> page) {

        PaginatedResponse<T> paginatedData = PaginatedResponse.<T>builder()
                .content(page.getContent())
                .page(page.getNumber())
                .size(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .first(page.isFirst())
                .last(page.isLast())
                .hasNext(page.hasNext())
                .hasPrevious(page.hasPrevious())
                .build();

        return ResponseEntity.ok(ApiResponse.success(message, paginatedData));
    }

    public static <T> ResponseEntity<ApiResponse<PaginatedResponse<T>>> paginated(Page<T> page) {
        return paginated("Data retrieved successfully", page);
    }

    // Error response with details
    public static ResponseEntity<ErrorResponse> errorWithDetails(
            HttpStatus status,
            String errorCode,
            String message,
            String details,
            String path,
            String method) {

        ErrorResponse errorResponse = ErrorResponse.builder()
                .errorCode(errorCode)
                .message(message)
                .details(details)
                .timestamp(LocalDateTime.now())
                .path(path)
                .method(method)
                .traceId(UUID.randomUUID().toString())
                .build();

        return ResponseEntity.status(status).body(errorResponse);
    }

    // Validation error response
    public static ResponseEntity<ErrorResponse> validationError(
            Map<String, String> fieldErrors,
            List<String> validationErrors,
            String path,
            String method) {

        ErrorResponse errorResponse = ErrorResponse.builder()
                .errorCode("VALIDATION_ERROR")
                .message("Validation failed")
                .fieldErrors(fieldErrors)
                .validationErrors(validationErrors)
                .timestamp(LocalDateTime.now())
                .path(path)
                .method(method)
                .traceId(UUID.randomUUID().toString())
                .build();

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }
}
