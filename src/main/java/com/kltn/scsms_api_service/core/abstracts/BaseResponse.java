package com.kltn.scsms_api_service.core.abstracts;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;

@Getter
@Setter
@SuperBuilder
public abstract class BaseResponse {

    @JsonProperty("success")
    private Boolean success;

    @JsonProperty("message")
    private String message;

    @JsonProperty("timestamp")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime timestamp;

    public BaseResponse() {
        this.timestamp = LocalDateTime.now();
    }

    public BaseResponse(Boolean success, String message) {
        this();
        this.success = success;
        this.message = message;
    }

}

