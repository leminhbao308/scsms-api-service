package com.kltn.scsms_api_service.core.dto.serviceTypeManagement;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * DTO for ServiceType information
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ServiceTypeInfoDto {
    
    private UUID serviceTypeId;
    private String code;
    private String name;
    private String description;
    private Integer defaultDuration;
    private Boolean isActive;
    private String displayName;
    private LocalDateTime createdDate;
    private LocalDateTime modifiedDate;
    private String createdBy;
    private String modifiedBy;
    private Long version;
}
