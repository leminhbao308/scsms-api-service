package com.kltn.scsms_api_service.core.dto.servicePackageTypeManagement;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * DTO for ServicePackageType information
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ServicePackageTypeInfoDto {
    
    @JsonProperty("service_package_type_id")
    private UUID servicePackageTypeId;
    
    private String code;
    private String name;
    private String description;
    
    @JsonProperty("price_policy")
    private String pricePolicy;
    
    @JsonProperty("applicable_customer_type")
    private String applicableCustomerType;
    
    @JsonProperty("is_default")
    private Boolean isDefault;
    
    @JsonProperty("is_active")
    private Boolean isActive;
    
    @JsonProperty("display_name")
    private String displayName;
    
    @JsonProperty("created_date")
    private LocalDateTime createdDate;
    
    @JsonProperty("modified_date")
    private LocalDateTime modifiedDate;
    
    @JsonProperty("created_by")
    private String createdBy;
    
    @JsonProperty("modified_by")
    private String modifiedBy;
    
    private Long version;
}
