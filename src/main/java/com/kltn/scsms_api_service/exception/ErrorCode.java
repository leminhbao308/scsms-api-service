package com.kltn.scsms_api_service.exception;

import lombok.Getter;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@Getter
public enum ErrorCode {
    BAD_REQUEST("SCS-0400", "SCS-0400", "SYSTEM", "Invalid request", "error.invalid_request"),
    DUPLICATE("SCS-0409", "SCS-0409", "SYSTEM", "Duplicate resource", "error.duplicate_resource"),
    REQUEST_TIMEOUT("SCS-0408", "SCS-0408", "SYSTEM", "Request timeout", "error.request_timeout"),
    INVALID_SIGNATURE("SCS-0444", "SCS-0444", "SYSTEM", "Invalid signature", "Invalid signature"),
    TIMEOUT("SCS-0504", "SCS-0504", "SYSTEM", "Gateway timeout", "error.gateway_timeout"),
    RESPONSE_ERROR(
            "SCS-0501",
            "SCS-0501",
            "SYSTEM",
            "External service response error",
            "error.external_service_response_error"),
    SERVICE_UNAVAILABLE(
            "SCS-0503", "SCS-0503", "SYSTEM", "Service unavailable", "error.service_unavailable"),
    SYSTEM_ERROR("SCS-0500", "SCS-0500", "SYSTEM", "System error", "error.system_error"),
    NOT_FOUND("SCS-0404", "SCS-0404", "SYSTEM", "Not found", "error.not_found"),
    UNAUTHORIZED("SCS-0401", "SCS-0401", "SYSTEM", "Unauthorized", "error.unauthorized"),
    FORBIDDEN("SCS-0403", "SCS-0403", "SYSTEM", "Forbidden", "error.forbidden"),
    
    // Product related errors
    PRODUCT_NOT_FOUND("SCS-1001", "SCS-1001", "PRODUCT", "Product not found", "error.product_not_found"),
    PRODUCT_URL_EXISTS("SCS-1002", "SCS-1002", "PRODUCT", "Product URL already exists", "error.product_url_exists"),
    PRODUCT_SKU_EXISTS("SCS-1003", "SCS-1003", "PRODUCT", "Product SKU already exists", "error.product_sku_exists"),
    PRODUCT_BARCODE_EXISTS("SCS-1004", "SCS-1004", "PRODUCT", "Product barcode already exists", "error.product_barcode_exists"),
    
    // Service related errors
    SERVICE_NOT_FOUND("SCS-2001", "SCS-2001", "SERVICE", "Service not found", "error.service_not_found"),
    SERVICE_URL_EXISTS("SCS-2002", "SCS-2002", "SERVICE", "Service URL already exists", "error.service_url_exists"),
    SERVICE_PRODUCT_NOT_FOUND("SCS-2003", "SCS-2003", "SERVICE", "Service product not found", "error.service_product_not_found"),
    SERVICE_PRODUCT_ALREADY_EXISTS("SCS-2004", "SCS-2004", "SERVICE", "Service product already exists", "error.service_product_already_exists"),
    
    // Service Package related errors
    SERVICE_PACKAGE_NOT_FOUND("SCS-3001", "SCS-3001", "SERVICE_PACKAGE", "Service package not found", "error.service_package_not_found"),
    SERVICE_PACKAGE_URL_EXISTS("SCS-3002", "SCS-3002", "SERVICE_PACKAGE", "Service package URL already exists", "error.service_package_url_exists"),
    SERVICE_PACKAGE_STEP_NOT_FOUND("SCS-3003", "SCS-3003", "SERVICE_PACKAGE", "Service package step not found", "error.service_package_step_not_found"),
    SERVICE_PACKAGE_PRODUCT_NOT_FOUND("SCS-3004", "SCS-3004", "SERVICE_PACKAGE", "Service package product not found", "error.service_package_product_not_found"),
    SERVICE_PACKAGE_PRODUCT_ALREADY_EXISTS("SCS-3005", "SCS-3005", "SERVICE_PACKAGE", "Service package product already exists", "error.service_package_product_already_exists"),
    SERVICE_PACKAGE_SERVICE_NOT_FOUND("SCS-3006", "SCS-3006", "SERVICE_PACKAGE", "Service package service not found", "error.service_package_service_not_found"),
    SERVICE_PACKAGE_SERVICE_ALREADY_EXISTS("SCS-3007", "SCS-3007", "SERVICE_PACKAGE", "Service package service already exists", "error.service_package_service_already_exists"),
    
    // Service Process related errors
    SERVICE_PROCESS_NOT_FOUND("SCS-4001", "SCS-4001", "SERVICE_PROCESS", "Service process not found", "error.service_process_not_found"),
    SERVICE_PROCESS_CODE_ALREADY_EXISTS("SCS-4002", "SCS-4002", "SERVICE_PROCESS", "Service process code already exists", "error.service_process_code_already_exists"),
    SERVICE_PROCESS_DEFAULT_NOT_FOUND("SCS-4003", "SCS-4003", "SERVICE_PROCESS", "Default service process not found", "error.service_process_default_not_found"),
    SERVICE_PROCESS_CANNOT_DELETE_IN_USE("SCS-4004", "SCS-4004", "SERVICE_PROCESS", "Cannot delete service process in use", "error.service_process_cannot_delete_in_use"),
    
    // Service Process Step related errors
    SERVICE_PROCESS_STEP_NOT_FOUND("SCS-5001", "SCS-5001", "SERVICE_PROCESS_STEP", "Service process step not found", "error.service_process_step_not_found"),
    SERVICE_PROCESS_STEP_ORDER_ALREADY_EXISTS("SCS-5002", "SCS-5002", "SERVICE_PROCESS_STEP", "Service process step order already exists", "error.service_process_step_order_already_exists"),
    
    // Service Process Step Product related errors
    SERVICE_PROCESS_STEP_PRODUCT_NOT_FOUND("SCS-6001", "SCS-6001", "SERVICE_PROCESS_STEP_PRODUCT", "Service process step product not found", "error.service_process_step_product_not_found"),
    SERVICE_PROCESS_STEP_PRODUCT_ALREADY_EXISTS("SCS-6002", "SCS-6002", "SERVICE_PROCESS_STEP_PRODUCT", "Service process step product already exists", "error.service_process_step_product_already_exists"),
    
    // Category related errors
    CATEGORY_NOT_FOUND("SCS-4001", "SCS-4001", "CATEGORY", "Category not found", "error.category_not_found"),
    CATEGORY_URL_EXISTS("SCS-4002", "SCS-4002", "CATEGORY", "Category URL already exists", "error.category_url_exists"),
    
    // General entity errors
    ENTITY_NOT_FOUND("SCS-5001", "SCS-5001", "ENTITY", "Entity not found", "error.entity_not_found"),
    INVALID_INPUT("SCS-5002", "SCS-5002", "ENTITY", "Invalid input", "error.invalid_input");
    private final String code;
    private final String rawCode;
    private final String category;
    private final String description;
    private final String message; // message = null nghĩa là lấy message từ nơi khác
    private static final Map<String, ErrorCode> ENUM_MAP;

    ErrorCode(String code, String rawCode, String category, String description, String message) {
        this.code = code;
        this.rawCode = rawCode;
        this.category = category;
        this.description = description;
        this.message = message;
    }

    static {
        final Map<String, ErrorCode> map = new HashMap<>();
        for (ErrorCode instance : ErrorCode.values()) {
            map.put(instance.getCode(), instance);
        }
        ENUM_MAP = Collections.unmodifiableMap(map);
    }

    public static ErrorCode get(String value) {
        return ENUM_MAP.get(value);
    }

    public static ErrorCode fromRawCodeAndCategory(String rawCode, String category) {
        if (rawCode == null || rawCode.isBlank()) {
            return null;
        }

        for (ErrorCode errorCode : values()) {
            if (errorCode.rawCode.equalsIgnoreCase(rawCode.trim())) {
                if (category != null && !category.isBlank()) {
                    if (errorCode.category.equalsIgnoreCase(category.trim())) {
                        return errorCode;
                    }
                } else {
                    return errorCode;
                }
            }
        }

        return null;
    }
}
