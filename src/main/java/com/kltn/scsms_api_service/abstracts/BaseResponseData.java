package com.kltn.scsms_api_service.abstracts;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class BaseResponseData<T> {

    @JsonProperty("success")
    private Boolean success;

    @JsonProperty("message")
    private String message;

    @JsonProperty("data")
    private T data;

    @JsonProperty("timestamp")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    @Builder.Default
    private LocalDateTime timestamp = LocalDateTime.now();

    public BaseResponseData(Boolean success, String message, T data) {
        this.success = success;
        this.message = message;
        this.data = data;
        this.timestamp = LocalDateTime.now();
    }

    public BaseResponseData(Boolean success, String message) {
        this.success = success;
        this.message = message;
        this.timestamp = LocalDateTime.now();
    }
}
