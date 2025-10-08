package com.kltn.scsms_api_service.core.dto.serviceManagement;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * DTO cho thông tin pricing summary của Service
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ServicePricingInfoDto {
    
    private UUID serviceId;
    private String serviceName;
    
    // Current pricing
    private BigDecimal currentBasePrice;
    private BigDecimal currentLaborCost;
    private BigDecimal currentTotalPrice;
    
    // Calculated pricing (real-time)
    private BigDecimal calculatedBasePrice;
    private BigDecimal calculatedTotalPrice;
    
    // Status
    private Boolean needsUpdate; // Có cần cập nhật không
    private LocalDateTime lastCalculatedAt;
    private String pricingStatus; // "UP_TO_DATE", "NEEDS_UPDATE", "ERROR"
    
    // Metadata
    private UUID serviceProcessId;
    private String serviceProcessName;
    private Integer totalSteps;
    private Integer totalProducts;
}
