package com.kltn.scsms_api_service.exception;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.NoHandlerFoundException;

import io.jsonwebtoken.ExpiredJwtException;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {
    
    /**
     * Error Response DTO
     */
    @lombok.Data
    @lombok.Builder
    public static class ErrorResponse {
        private Instant timestamp;
        private int status;
        private String error;
        private String message;
        private String path;
        private String errorCode;
        private Object data; // Additional data for error context (e.g., alternative bay for walk-in booking)
    }
    
    /**
     * Handle ClientSideException (4xx errors)
     */
    @ExceptionHandler(ClientSideException.class)
    public ResponseEntity<ErrorResponse> handleClientSideException(
        ClientSideException ex, WebRequest request) {
        
        HttpStatus status = mapErrorCodeToHttpStatus(ex.getCode());
        
        ErrorResponse errorResponse = ErrorResponse.builder()
            .timestamp(Instant.now())
            .status(status.value())
            .error(status.getReasonPhrase())
            .message(ex.getMessage())
            .path(getRequestPath(request))
            .errorCode(ex.getCode() != null ? ex.getCode().name() : null)
            .data(ex.getData()) // Include additional data if available
            .build();
        
        // Log client errors at WARN level
        log.warn("Client side error - Code: {}, Message: {}, Path: {}",
            ex.getCode(), ex.getMessage(), getRequestPath(request));
        
        return ResponseEntity.status(status).body(errorResponse);
    }
    
    /**
     * Handle ServerSideException (5xx errors)
     */
    @ExceptionHandler(ServerSideException.class)
    public ResponseEntity<ErrorResponse> handleServerSideException(
        ServerSideException ex, WebRequest request) {
        
        HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;
        
        ErrorResponse errorResponse = ErrorResponse.builder()
            .timestamp(Instant.now())
            .status(status.value())
            .error(status.getReasonPhrase())
            .message(ex.getMessage())
            .path(getRequestPath(request))
            .errorCode(ex.getErrorCode() != null ? ex.getErrorCode().name() : null)
            .build();
        
        // Log server errors at ERROR level with full stack trace
        log.error("Server side error - Code: {}, Category: {}, Message: {}, Path: {}",
            ex.getErrorCode(), ex.getCategory(), ex.getMessage(), getRequestPath(request), ex);
        
        return ResponseEntity.status(status).body(errorResponse);
    }
    
    /**
     * Handle validation errors
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationException(
        MethodArgumentNotValidException ex, WebRequest request) {
        
        HttpStatus status = HttpStatus.BAD_REQUEST;
        Map<String, String> validationErrors = new HashMap<>();
        
        ex.getBindingResult().getFieldErrors().forEach(error ->
            validationErrors.put(error.getField(), error.getDefaultMessage())
        );
        
        ErrorResponse errorResponse = ErrorResponse.builder()
            .timestamp(Instant.now())
            .status(status.value())
            .error(status.getReasonPhrase())
            .message("Validation failed")
            .path(getRequestPath(request))
            .errorCode("VALIDATION_ERROR")
            .build();
        
        log.warn("Validation error - Path: {}, Errors: {}", getRequestPath(request), validationErrors);
        
        return ResponseEntity.status(status).body(errorResponse);
    }
    
    /**
     * Handle 404 Not Found
     */
    @ExceptionHandler(NoHandlerFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFoundException(
        NoHandlerFoundException ex, WebRequest request) {
        
        HttpStatus status = HttpStatus.NOT_FOUND;
        
        ErrorResponse errorResponse = ErrorResponse.builder()
            .timestamp(Instant.now())
            .status(status.value())
            .error(status.getReasonPhrase())
            .message("Endpoint not found")
            .path(getRequestPath(request))
            .errorCode("NOT_FOUND")
            .build();
        
        log.warn("Endpoint not found - Path: {}, Method: {}",
            getRequestPath(request), ex.getHttpMethod());
        
        return ResponseEntity.status(status).body(errorResponse);
    }
    
    /**
     * Handle expired JWT token exceptions
     */
    @ExceptionHandler(ExpiredJwtException.class)
    public ResponseEntity<ErrorResponse> handleExpiredJwtException(
        ExpiredJwtException ex, WebRequest request) {
        
        HttpStatus status = HttpStatus.UNAUTHORIZED;
        
        ErrorResponse errorResponse = ErrorResponse.builder()
            .timestamp(Instant.now())
            .status(status.value())
            .error(status.getReasonPhrase())
            .message("Token has expired. Please refresh your token or login again.")
            .path(getRequestPath(request))
            .errorCode("TOKEN_EXPIRED")
            .build();
        
        // Log at WARN level, not ERROR, as this is expected behavior
        log.warn("Token expired - Path: {}, Message: {}",
            getRequestPath(request), ex.getMessage());
        
        return ResponseEntity.status(status).body(errorResponse);
    }
    
    /**
     * Handle all other uncaught exceptions
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(
        Exception ex, WebRequest request) {
        
        HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;
        
        ErrorResponse errorResponse = ErrorResponse.builder()
            .timestamp(Instant.now())
            .status(status.value())
            .error(status.getReasonPhrase())
            .message("An unexpected error occurred")
            .path(getRequestPath(request))
            .errorCode("INTERNAL_ERROR")
            .build();
        
        // Log unexpected errors at ERROR level with full stack trace
        log.error("Unexpected error - Path: {}, Exception: {}",
            getRequestPath(request), ex.getMessage(), ex);
        
        return ResponseEntity.status(status).body(errorResponse);
    }
    
    /**
     * Map ErrorCode to HttpStatus
     */
    private HttpStatus mapErrorCodeToHttpStatus(ErrorCode errorCode) {
        if (errorCode == null) {
            return HttpStatus.BAD_REQUEST;
        }
        
        // Implement your mapping logic based on your ErrorCode enum
        // This is a basic example - adjust according to your ErrorCode definitions
        switch (errorCode.name()) {
            case "UNAUTHORIZED":
            case "ACCESS_DENIED":
                return HttpStatus.UNAUTHORIZED;
            case "FORBIDDEN":
            case "INSUFFICIENT_PERMISSIONS":
                return HttpStatus.FORBIDDEN;
            case "NOT_FOUND":
            case "RESOURCE_NOT_FOUND":
                return HttpStatus.NOT_FOUND;
            case "CONFLICT":
            case "DUPLICATE_RESOURCE":
                return HttpStatus.CONFLICT;
            default:
                return HttpStatus.BAD_REQUEST;
        }
    }
    
    /**
     * Extract request path from WebRequest
     */
    private String getRequestPath(WebRequest request) {
        String path = request.getDescription(false);
        return path.replace("uri=", "");
    }
}
