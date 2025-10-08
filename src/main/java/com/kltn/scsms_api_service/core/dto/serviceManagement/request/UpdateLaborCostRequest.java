package com.kltn.scsms_api_service.core.dto.serviceManagement.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Request DTO để cập nhật labor cost cho Service
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateLaborCostRequest {
    
    @NotNull(message = "Labor cost is required")
    @DecimalMin(value = "0.0", message = "Labor cost must be non-negative")
    private BigDecimal laborCost;
    
    private String notes; // Lý do thay đổi
    private String updatedBy; // Người cập nhật
}
