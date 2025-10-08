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
    
    @NotNull(message = "Tiến độ không được để trống")
    @DecimalMin(value = "0.00", message = "Tiến độ phải từ 0% trở lên")
    @DecimalMax(value = "100.00", message = "Tiến độ không được vượt quá 100%")
    @JsonProperty("progress_percent")
    private BigDecimal progressPercent;
    
    @JsonProperty("notes")
    private String notes;
}
