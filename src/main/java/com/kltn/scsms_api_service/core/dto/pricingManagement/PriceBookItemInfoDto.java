package com.kltn.scsms_api_service.core.dto.pricingManagement;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.kltn.scsms_api_service.core.dto.productManagement.ProductInfoDto;
import com.kltn.scsms_api_service.core.dto.response.AuditDto;
import com.kltn.scsms_api_service.core.entity.enumAttribute.PricingPolicyType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;

@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class PriceBookItemInfoDto extends AuditDto {
    
    private String id;
    
    private ProductInfoDto product;
    
    @JsonProperty("policy_type")
    private PricingPolicyType policyType;
    
    @JsonProperty("fixed_price")
    private BigDecimal fixedPrice;
    
    @JsonProperty("markup_percent")
    private BigDecimal markupPercent;
}
