package com.kltn.scsms_api_service.exception;

import lombok.Getter;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@Getter
public enum ErrorCode {
    BAD_REQUEST("SCS-0400", "SCS-0400", "SYSTEM", "Invalid request", "error.invalid_request"),
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
    FORBIDDEN("SCS-0403", "SCS-0403", "SYSTEM", "Forbidden", "error.forbidden");
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
