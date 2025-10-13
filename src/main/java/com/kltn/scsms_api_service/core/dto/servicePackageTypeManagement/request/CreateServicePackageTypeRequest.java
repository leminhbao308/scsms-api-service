package com.kltn.scsms_api_service.core.dto.servicePackageTypeManagement.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for creating ServicePackageType
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateServicePackageTypeRequest {
    
    @NotBlank(message = "Service package type code is required")
    @Size(max = 50, message = "Service package type code must not exceed 50 characters")
    private String code;
    
    @NotBlank(message = "Service package type name is required")
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
    @Builder.Default
    private Boolean isDefault = false;
    
    @JsonProperty("is_active")
    @Builder.Default
    private Boolean isActive = true;
}
