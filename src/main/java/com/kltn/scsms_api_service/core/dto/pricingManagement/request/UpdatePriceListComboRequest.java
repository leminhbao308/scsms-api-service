package com.kltn.scsms_api_service.core.dto.pricingManagement.request;

import jakarta.validation.constraints.DecimalMin;
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
public class UpdatePriceListComboRequest {
    
    private UUID comboId;
    
    private String comboCode;
    
    private String comboName;
    
    @DecimalMin(value = "0.0", message = "Combo price must be non-negative")
    private BigDecimal comboPrice;
    
    @DecimalMin(value = "0.0", message = "Discount percentage must be non-negative")
    private BigDecimal discountPercentage;
    
    private LocalDateTime effectiveFrom;
    
    private LocalDateTime effectiveTo;
    
    private Boolean isActive;
    
    private String description;
    
    private List<UUID> comboItems;
}
