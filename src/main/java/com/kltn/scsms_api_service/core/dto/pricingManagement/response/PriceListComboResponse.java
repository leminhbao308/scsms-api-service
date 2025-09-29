package com.kltn.scsms_api_service.core.dto.pricingManagement.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PriceListComboResponse {
    
    private UUID comboId;
    
    private String comboCode;
    
    private String comboName;
    
    private BigDecimal comboPrice;
    
    private BigDecimal originalPrice;
    
    private BigDecimal discountPercentage;
    
    private BigDecimal discountAmount;
    
    private LocalDateTime effectiveFrom;
    
    private LocalDateTime effectiveTo;
    
    private Boolean isActive;
    
    private String description;
    
    private LocalDateTime createdAt;
    
    private LocalDateTime updatedAt;
    
    private UUID createdBy;
    
    private UUID updatedBy;
    
    private String createdByName;
    
    private String updatedByName;
    
    private List<PriceListComboItemResponse> comboItems;
    
    private Integer totalItems;
}
