package com.kltn.scsms_api_service.core.dto.pricingManagement.response;

import com.kltn.scsms_api_service.core.entity.enumAttribute.ItemType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PriceListLineResponse {
    
    private UUID lineId;
    
    private ItemType itemType;
    
    private UUID itemId;
    
    private String itemName;
    
    private String itemCode;
    
    private BigDecimal basePrice;
    
    private BigDecimal sellingPrice;
    
    private BigDecimal discountPercentage;
    
    private BigDecimal discountAmount;
    
    private BigDecimal finalPrice;
    
    private LocalDateTime effectiveFrom;
    
    private LocalDateTime effectiveTo;
    
    private Boolean isActive;
    
    private String notes;
    
    private LocalDateTime createdAt;
    
    private LocalDateTime updatedAt;
    
    private UUID createdBy;
    
    private UUID updatedBy;
    
    private String createdByName;
    
    private String updatedByName;
}
