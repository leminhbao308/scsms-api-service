package com.kltn.scsms_api_service.core.dto.pricingManagement.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.kltn.scsms_api_service.core.entity.enumAttribute.PricingPolicyType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class CreatePriceBookItemRequest {
    
    @JsonProperty("product_id")
    private UUID productId;
    
    @JsonProperty("service_id")
    private UUID serviceId;
    
    @JsonProperty("policy_type")
    private PricingPolicyType policyType;
    
    @JsonProperty("fixed_price")
    private BigDecimal fixedPrice;
    
    @JsonProperty("markup_percent")
    private BigDecimal markupPercent;
}
