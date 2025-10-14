package com.kltn.scsms_api_service.core.dto.serviceProcessTrackingManagement.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Request DTO để cập nhật tiến độ tracking
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProgressUpdateRequest {
    
    @JsonProperty("progress_percent")
    private BigDecimal progressPercent;
    
    @JsonProperty("notes")
    private String notes;
}
