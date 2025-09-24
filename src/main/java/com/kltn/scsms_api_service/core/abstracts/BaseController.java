package com.kltn.scsms_api_service.core.abstracts;

import com.kltn.scsms_api_service.core.utils.ResponseBuilder;
import com.kltn.scsms_api_service.core.dto.response.ApiResponse;
import com.kltn.scsms_api_service.core.dto.response.PaginatedResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

@Slf4j
public abstract class BaseController {

    // Success responses
    protected <T> ResponseEntity<ApiResponse<T>> success(T data) {
        return ResponseBuilder.success(data);
    }

    protected <T> ResponseEntity<ApiResponse<T>> success(String message, T data) {
        return ResponseBuilder.success(message, data);
    }

    protected ResponseEntity<ApiResponse<Void>> success(String message) {
        return ResponseBuilder.success(message);
    }

    // Created responses
    protected <T> ResponseEntity<ApiResponse<T>> created(T data) {
        return ResponseBuilder.created(data);
    }

    protected <T> ResponseEntity<ApiResponse<T>> created(String message, T data) {
        return ResponseBuilder.created(message, data);
    }

    // Error responses
    protected <T> ResponseEntity<ApiResponse<T>> badRequest(String message) {
        return ResponseBuilder.badRequest(message);
    }

    protected <T> ResponseEntity<ApiResponse<T>> unauthorized(String message) {
        return ResponseBuilder.unauthorized(message);
    }

    protected <T> ResponseEntity<ApiResponse<T>> forbidden(String message) {
        return ResponseBuilder.forbidden(message);
    }

    protected <T> ResponseEntity<ApiResponse<T>> notFound(String message) {
        return ResponseBuilder.notFound(message);
    }

    protected <T> ResponseEntity<ApiResponse<T>> conflict(String message) {
        return ResponseBuilder.conflict(message);
    }

    protected <T> ResponseEntity<ApiResponse<T>> internalServerError(String message) {
        return ResponseBuilder.internalServerError(message);
    }

    // Custom status
    protected <T> ResponseEntity<ApiResponse<T>> status(HttpStatus status, String message, T data) {
        return ResponseBuilder.status(status, message, data);
    }

    protected ResponseEntity<ApiResponse<?>> status(HttpStatus status, String message) {
        return ResponseBuilder.status(status, message);
    }

    // Paginated responses
    protected <T> ResponseEntity<ApiResponse<PaginatedResponse<T>>> paginated(Page<T> page) {
        return ResponseBuilder.paginated(page);
    }

    protected <T> ResponseEntity<ApiResponse<PaginatedResponse<T>>> paginated(String message, Page<T> page) {
        return ResponseBuilder.paginated(message, page);
    }
}
