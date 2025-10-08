package com.kltn.scsms_api_service.core.dto.serviceBayManagement.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Request DTO để kiểm tra tính khả dụng của bay
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BayAvailabilityRequest {
    
    @NotNull(message = "Start time is required")
    @JsonProperty("start_time")
    private LocalDateTime startTime;
    
    @NotNull(message = "End time is required")
    @JsonProperty("end_time")
    private LocalDateTime endTime;
}
