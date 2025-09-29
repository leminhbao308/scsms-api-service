package com.kltn.scsms_api_service.core.dto.pricingManagement.request;

import com.kltn.scsms_api_service.core.entity.enumAttribute.ItemType;
import jakarta.validation.constraints.DecimalMin;
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
public class UpdatePriceListLineRequest {
    
    private UUID lineId;
    
    private ItemType itemType;
    
    private UUID itemId;
    
    @DecimalMin(value = "0.0", message = "Base price must be non-negative")
    private BigDecimal basePrice;
    
    @DecimalMin(value = "0.0", message = "Selling price must be non-negative")
    private BigDecimal sellingPrice;
    
    @DecimalMin(value = "0.0", message = "Discount percentage must be non-negative")
    private BigDecimal discountPercentage;
    
    @DecimalMin(value = "0.0", message = "Discount amount must be non-negative")
    private BigDecimal discountAmount;
    
    private LocalDateTime effectiveFrom;
    
    private LocalDateTime effectiveTo;
    
    private Boolean isActive;
    
    private String notes;
}
