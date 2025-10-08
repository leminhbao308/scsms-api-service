package com.kltn.scsms_api_service.core.dto.serviceManagement;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * DTO cho thông tin pricing của Service
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ServicePricingDto {
    
    private UUID serviceId;
    private String serviceName;
    private String serviceUrl;
    
    // Pricing information
    private BigDecimal basePrice; // Tổng chi phí sản phẩm (system-calculated)
    private BigDecimal laborCost; // Tiền công lao động (user input)
    private BigDecimal totalEstimatedPrice; // basePrice + laborCost
    private BigDecimal finalPrice; // totalEstimatedPrice + markup (nếu có)
    
    // Metadata
    private LocalDateTime lastPriceCalculatedAt;
    private UUID priceBookId;
    private String priceBookName;
    
    // Detailed breakdown
    private List<ProcessStepPricingDto> processSteps;
    private BigDecimal totalProductCosts;
    private Integer totalProducts;
    private Integer totalSteps;
}
