package com.kltn.scsms_api_service.core.dto.pricingManagement.request;

import com.fasterxml.jackson.annotation.JsonProperty;
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
    @JsonProperty("service_package_id")
    private UUID servicePackageId;
    
    @NotNull(message = "Policy type cannot be null")
    @JsonProperty("policy_type")
    private PricingPolicyType policyType;
    
    // Giá cố định (nếu policyType = FIXED)
    @JsonProperty("fixed_price")
    private BigDecimal fixedPrice;
    
    // Tỉ lệ markup (nếu policyType = MARKUP_ON_PEAK)
    @JsonProperty("markup_percent")
    private BigDecimal markupPercent;
}
