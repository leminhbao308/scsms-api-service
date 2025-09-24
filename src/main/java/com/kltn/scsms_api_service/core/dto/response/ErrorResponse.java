package com.kltn.scsms_api_service.core.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ErrorResponse {

    @JsonProperty("error_code")
    private String errorCode;

    @JsonProperty("message")
    private String message;

    @JsonProperty("details")
    private String details;

    @JsonProperty("field_errors")
    private Map<String, String> fieldErrors;

    @JsonProperty("validation_errors")
    private List<String> validationErrors;

    @JsonProperty("timestamp")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime timestamp;

    @JsonProperty("path")
    private String path;

    @JsonProperty("method")
    private String method;

    @JsonProperty("trace_id")
    private String traceId;
}
