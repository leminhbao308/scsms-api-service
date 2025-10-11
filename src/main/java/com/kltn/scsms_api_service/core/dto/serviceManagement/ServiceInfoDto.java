package com.kltn.scsms_api_service.core.dto.serviceManagement;

import com.kltn.scsms_api_service.core.dto.response.AuditDto;
import com.kltn.scsms_api_service.core.entity.Service;
import lombok.*;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class ServiceInfoDto {
    
    private UUID serviceId;
    private String serviceUrl;
    private String serviceName;
    private UUID categoryId;
    private String categoryName;
    private String description;
    private Integer standardDuration;
    private Service.SkillLevel requiredSkillLevel;
    private Boolean isPackage;
    private BigDecimal basePrice; // Base price for the service
    private BigDecimal laborCost; // Tiền công lao động
    private UUID serviceTypeId;
    private String serviceTypeName;
    private Boolean isFeatured;
    private Boolean isActive;
    private UUID serviceProcessId;
    private String serviceProcessName;
    private String serviceProcessCode;
    private Boolean isDefaultProcess;
    private Integer estimatedDuration;
    private UUID branchId;
    private String branchName;
    @Builder.Default
    private AuditDto audit = AuditDto.builder().build();
}