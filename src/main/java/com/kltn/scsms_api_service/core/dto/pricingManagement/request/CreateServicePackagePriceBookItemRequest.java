package com.kltn.scsms_api_service.core.dto.pricingManagement.request;

import com.kltn.scsms_api_service.core.entity.enumAttribute.PricingPolicyType;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Request DTO để tạo PriceBookItem cho ServicePackage
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateServicePackagePriceBookItemRequest {
    
    @NotNull(message = "Service Package ID cannot be null")
    private UUID servicePackageId;
    
    @NotNull(message = "Policy type cannot be null")
    private PricingPolicyType policyType;
    
    // Giá cố định (nếu policyType = FIXED)
    private BigDecimal fixedPrice;
    
    // Tỉ lệ markup (nếu policyType = MARKUP_ON_PEAK)
    private BigDecimal markupPercent;
}
