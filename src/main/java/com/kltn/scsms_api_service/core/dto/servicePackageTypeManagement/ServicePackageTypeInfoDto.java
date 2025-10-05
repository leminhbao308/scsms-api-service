package com.kltn.scsms_api_service.core.dto.servicePackageTypeManagement;

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
    
    private UUID servicePackageTypeId;
    private String code;
    private String name;
    private String description;
    private String pricePolicy;
    private String applicableCustomerType;
    private Boolean isDefault;
    private Boolean isActive;
    private String displayName;
    private LocalDateTime createdDate;
    private LocalDateTime modifiedDate;
    private String createdBy;
    private String modifiedBy;
    private Long version;
}
