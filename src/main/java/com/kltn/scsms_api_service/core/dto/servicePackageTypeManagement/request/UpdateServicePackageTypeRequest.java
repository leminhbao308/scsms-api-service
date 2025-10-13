package com.kltn.scsms_api_service.core.dto.servicePackageTypeManagement.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for updating ServicePackageType
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateServicePackageTypeRequest {
    
    @Size(max = 50, message = "Service package type code must not exceed 50 characters")
    private String code;
    
    @Size(max = 100, message = "Service package type name must not exceed 100 characters")
    private String name;
    
    @Size(max = 1000, message = "Description must not exceed 1000 characters")
    private String description;
    
    @Size(max = 255, message = "Price policy must not exceed 255 characters")
    @JsonProperty("price_policy")
    private String pricePolicy;
    
    @Size(max = 100, message = "Applicable customer type must not exceed 100 characters")
    @JsonProperty("applicable_customer_type")
    private String applicableCustomerType;
    
    @JsonProperty("is_default")
    private Boolean isDefault;
    
    @JsonProperty("is_active")
    private Boolean isActive;
}
